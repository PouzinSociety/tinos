package rina.ribdaemon.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.BaseCDAPSessionManager;
import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPMessageHandler;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.Flags;
import rina.cdap.api.message.CDAPMessage.Opcode;
import rina.cdap.api.message.ObjectValue;
import rina.encoding.api.BaseEncoder;
import rina.encoding.api.Encoder;
import rina.enrollment.api.BaseEnrollmentTask;
import rina.enrollment.api.EnrollmentTask;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.NotificationPolicy;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;
import rina.ribdaemon.api.UpdateStrategy;
import rina.ribdaemon.impl.rib.RIB;
import rina.rmt.api.BaseRMT;
import rina.rmt.api.RMT;

/**
 * RIBDaemon that stores the objects in memory
 * @author eduardgrasa
 *
 */
public class RIBDaemonImpl extends BaseRIBDaemon{
	
	private static final Log log = LogFactory.getLog(RIBDaemonImpl.class);
	
	/** Create, retrieve and delete CDAP sessions **/
	private CDAPSessionManager cdapSessionManager = null;
	
	/** Encode and decode objects **/
	private Encoder encoder = null;
	
	/** The RIB **/
	private RIB rib = null;
	
	/** CDAP Message handlers that have sent a CDAP message and are waiting for a reply **/
	private Map<String, CDAPMessageHandler> messageHandlersWaitingForReply = null;
	
	public RIBDaemonImpl(){
		rib = new RIB();
		messageHandlersWaitingForReply = new Hashtable<String, CDAPMessageHandler>();
	}
	
	@Override
	public void setIPCProcess(IPCProcess ipcProcess){
		super.setIPCProcess(ipcProcess);
		this.encoder = (Encoder) getIPCProcess().getIPCProcessComponent(BaseEncoder.getComponentName());
		this.cdapSessionManager = (CDAPSessionManager) getIPCProcess().getIPCProcessComponent(BaseCDAPSessionManager.getComponentName());
	}

	/**
	 * Invoked by the RMT when it detects a CDAP message. The RIB Daemon has to process the CDAP message and, 
	 * if valid, it will either pass it to interested subscribers and/or write to storage and/or modify other 
	 * tasks data structures. It may be the case that the CDAP message is not addressed to an application 
	 * entity within this IPC process, then the RMT may decide to rely the message to the right destination 
	 * (after consulting an adequate forwarding table).
	 * @param cdapMessage
	 */
	public synchronized void cdapMessageDelivered(byte[] encodedCDAPMessage, int portId){
		CDAPMessage cdapMessage = null;
		CDAPSessionDescriptor cdapSessionDescriptor = null;
		
		log.debug("Got an encoded CDAP message from portId "+portId);
		
		//1 Decode the message and obtain the CDAP session descriptor
		try{
			cdapMessage = cdapSessionManager.messageReceived(encodedCDAPMessage, portId);
			cdapSessionDescriptor = cdapSessionManager.getCDAPSession(portId).getSessionDescriptor();
		}catch(CDAPException ex){
			log.error("Error decoding CDAP message: " + ex.getMessage());
			ex.printStackTrace();
			if (ex.getCDAPMessage().getInvokeID() != 0){
				this.sendErrorMessage(ex, portId);
			}
			return;
		}
		
		Opcode opcode = cdapMessage.getOpCode();
		
		//2 Find the destination of the message and call it
		try{
			CDAPMessageHandler cdapMessageHandler = null;

			//M_CONNECT, M_CONNECT_R, M_RELEASE and M_RELEASE_R are handled by the Enrollment task
			if (opcode.equals(Opcode.M_CONNECT) || opcode.equals(Opcode.M_CONNECT_R) || opcode.equals(Opcode.M_RELEASE) || opcode.equals(Opcode.M_RELEASE_R)){
				EnrollmentTask enrollmentTask = (EnrollmentTask) this.getIPCProcess().getIPCProcessComponent(BaseEnrollmentTask.getComponentName());
				switch(opcode){
				case M_CONNECT:
					enrollmentTask.connect(cdapMessage, cdapSessionDescriptor);
					break;
				case M_CONNECT_R:
					enrollmentTask.connectResponse(cdapMessage, cdapSessionDescriptor);
					break;
				case M_RELEASE:
					enrollmentTask.release(cdapMessage, cdapSessionDescriptor);
					break;
				case M_RELEASE_R:
					enrollmentTask.releaseResponse(cdapMessage, cdapSessionDescriptor);
					break;
				}
			}
			//All the other request messages (M_READ, M_WRITE, M_CREATE, M_DELETE, M_START, M_STOP, M_CANCELREAD) are 
			//handled by the RIB
			else if (opcode.equals(Opcode.M_READ) || opcode.equals(Opcode.M_WRITE) || opcode.equals(Opcode.M_CREATE) || 
					opcode.equals(Opcode.M_DELETE) || opcode.equals(Opcode.M_START) || opcode.equals(Opcode.M_STOP) || 
					opcode.equals(Opcode.M_CANCELREAD)){
				this.processOperation(cdapMessage, cdapSessionDescriptor);
			}
			//All the response messages must be handled by the entities that are waiting for the reply
			else{
				if (cdapMessage.getFlags() != null && cdapMessage.getFlags().equals(Flags.F_RD_INCOMPLETE)){
					cdapMessageHandler = messageHandlersWaitingForReply.get(cdapSessionDescriptor.getPortId()+"-"+cdapMessage.getInvokeID());
				}else{
					cdapMessageHandler = messageHandlersWaitingForReply.remove(cdapSessionDescriptor.getPortId()+"-"+cdapMessage.getInvokeID());
				}
				if (cdapMessageHandler == null){
					log.error("Nobody was waiting for this response message "+cdapMessage.toString());
				}else{
					switch(opcode){
					case M_READ_R:
						cdapMessageHandler.readResponse(cdapMessage, cdapSessionDescriptor);
						break;
					case M_WRITE_R:
						cdapMessageHandler.writeResponse(cdapMessage, cdapSessionDescriptor);
						break;
					case M_CREATE_R:
						cdapMessageHandler.createResponse(cdapMessage, cdapSessionDescriptor);
						break;
					case M_DELETE_R:
						cdapMessageHandler.deleteResponse(cdapMessage, cdapSessionDescriptor);
						break;
					case M_START_R:
						cdapMessageHandler.startResponse(cdapMessage, cdapSessionDescriptor);
						break;
					case M_STOP_R:
						cdapMessageHandler.stopResponse(cdapMessage, cdapSessionDescriptor);
						break;
					case M_CANCELREAD_R:
						cdapMessageHandler.cancelReadResponse(cdapMessage, cdapSessionDescriptor);
						break;
					}
				}
			}
		}catch(RIBDaemonException ex){
			ex.printStackTrace();
			log.error(ex);
		}
	}
	
	/**
	 * Invoked by the RIB Daemon when there has been an error decoding the contents of a 
	 * CDAP Message
	 * @param cdapException
	 * @param portId
	 */
	private synchronized void sendErrorMessage(CDAPException cdapException, int portId){
		CDAPMessage wrongMessage = cdapException.getCDAPMessage();
		CDAPMessage returnMessage = null;

		switch(wrongMessage.getOpCode()){
		case M_CONNECT:
			try{
				returnMessage = cdapSessionManager.getOpenConnectionResponseMessage(portId, wrongMessage.getAuthMech(), wrongMessage.getAuthValue(), wrongMessage.getSrcAEInst(), 
						wrongMessage.getSrcAEName(), wrongMessage.getSrcApInst(), wrongMessage.getSrcApName(), cdapException.getResult(), 
						cdapException.getResultReason(), wrongMessage.getDestAEInst(), wrongMessage.getDestAEName(), wrongMessage.getDestApInst(), 
						wrongMessage.getDestApName(), wrongMessage.getInvokeID());
			}catch(CDAPException ex){
				ex.printStackTrace();
			}
			break;
		case M_CREATE:
			try{
				returnMessage = cdapSessionManager.getCreateObjectResponseMessage(portId, wrongMessage.getFlags(), 
						wrongMessage.getObjClass(), wrongMessage.getObjInst(), wrongMessage.getObjName(), wrongMessage.getObjValue(), 
						cdapException.getResult(), cdapException.getResultReason(), wrongMessage.getInvokeID());
			}catch(CDAPException ex){
				ex.printStackTrace();
			}
			break;
		case M_DELETE:
			try{
				returnMessage = cdapSessionManager.getDeleteObjectResponseMessage(portId, wrongMessage.getFlags(), wrongMessage.getObjClass(), 
						wrongMessage.getObjInst(), wrongMessage.getObjName(), cdapException.getResult(), cdapException.getResultReason(), wrongMessage.getInvokeID());
			}catch(CDAPException ex){
				ex.printStackTrace();
			}
			break;
		case M_READ:
			try{
				returnMessage = cdapSessionManager.getReadObjectResponseMessage(portId, wrongMessage.getFlags(), wrongMessage.getObjClass(), 
						wrongMessage.getObjInst(), wrongMessage.getObjName(), wrongMessage.getObjValue(), cdapException.getResult(), 
						cdapException.getResultReason(), wrongMessage.getInvokeID());
			}catch(CDAPException ex){
				ex.printStackTrace();
			}
			break;
		case M_WRITE:
			try{
				returnMessage = cdapSessionManager.getWriteObjectResponseMessage(portId, wrongMessage.getFlags(), cdapException.getResult(), 
						cdapException.getResultReason(), wrongMessage.getInvokeID());
			}catch(CDAPException ex){
				ex.printStackTrace();
			}
			break;
		case M_CANCELREAD:
			try{
				returnMessage = cdapSessionManager.getCancelReadResponseMessage(portId, wrongMessage.getFlags(), wrongMessage.getInvokeID(), cdapException.getResult(), 
						cdapException.getResultReason());
			}catch(CDAPException ex){
				ex.printStackTrace();
			}
			break;
		case M_START:
			try{
				returnMessage = cdapSessionManager.getStartObjectResponseMessage(portId, wrongMessage.getFlags(), cdapException.getResult(), 
						cdapException.getResultReason(), wrongMessage.getInvokeID());
			}catch(CDAPException ex){
				ex.printStackTrace();
			}
			break;
		case M_STOP:
			try{
				returnMessage = cdapSessionManager.getStopObjectResponseMessage(portId, wrongMessage.getFlags(), cdapException.getResult(), 
						cdapException.getResultReason(), wrongMessage.getInvokeID());
			}catch(CDAPException ex){
				ex.printStackTrace();
			}
			break;
		case M_RELEASE:
			try{
				returnMessage = cdapSessionManager.getReleaseConnectionResponseMessage(portId, wrongMessage.getFlags(), cdapException.getResult(), 
						cdapException.getResultReason(), wrongMessage.getInvokeID());
			}catch(CDAPException ex){
				ex.printStackTrace();
			}
			break;
		}

		if (returnMessage != null){
			try{
				this.sendMessage(returnMessage, portId, null);
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Invoked by the RMT when it detects that a certain flow has been deallocated, and therefore any CDAP sessions 
	 * over it should be terminated.
	 * @param portId identifies the flow that has been deallocated
	 */
	public void flowDeallocated(int portId) {
		CDAPSessionDescriptor cdapSessionDescriptor = cdapSessionManager.getCDAPSession(portId).getSessionDescriptor();
		//Remove the CDAP session
		cdapSessionManager.removeCDAPSession(portId);
		//Clean the messageHandlersWaitingForReply queue
		cleanMessageHandlersWaitingForReply(portId);
		//Inform the enrollment task
		EnrollmentTask enrollmentTask = (EnrollmentTask) this.getIPCProcess().getIPCProcessComponent(BaseEnrollmentTask.getComponentName());
		enrollmentTask.flowDeallocated(cdapSessionDescriptor);
	}
	
	/**
	 * Remove the CDAP Message handlers that were waiting for a reply message coming 
	 * from a certain portId (because the underlying flow has been deallocated)
	 * @param portId
	 */
	private void cleanMessageHandlersWaitingForReply(int portId){
		Iterator<String> iterator = messageHandlersWaitingForReply.keySet().iterator();
		String key = null;
		List<String> toBeRemoved = new ArrayList<String>();
		while(iterator.hasNext()){
			key = iterator.next();
			if (key.startsWith(""+portId)){
				toBeRemoved.add(key);
			}
		}
		
		for(int i=0; i<toBeRemoved.size(); i++){
			messageHandlersWaitingForReply.remove(toBeRemoved.get(i));
		}
	}
	
	/**
	 * Causes a CDAP message to be sent
	 * @param cdapMessage the message to be sent
	 * @param sessionId the CDAP session id
	 * @param cdapMessageHandler the class to be called when the response message is received (if required)
	 * @throws RIBDaemonException
	 */
	public synchronized void sendMessage(CDAPMessage cdapMessage, int portId, CDAPMessageHandler cdapMessageHandler) throws RIBDaemonException{
		byte[] serializedCDAPMessageToBeSend = null;
		RMT rmt = (RMT) this.getIPCProcess().getIPCProcessComponent(BaseRMT.getComponentName());
		
		if (cdapMessage.getInvokeID() != 0 && !cdapMessage.getOpCode().equals(Opcode.M_CONNECT) 
				&& !cdapMessage.getOpCode().equals(Opcode.M_RELEASE) 
				&& !cdapMessage.getOpCode().equals(Opcode.M_CANCELREAD_R)
				&& !cdapMessage.getOpCode().equals(Opcode.M_CONNECT_R)
				&& !cdapMessage.getOpCode().equals(Opcode.M_CREATE_R) 
				&& !cdapMessage.getOpCode().equals(Opcode.M_READ_R)
				&& !cdapMessage.getOpCode().equals(Opcode.M_DELETE_R)
				&& !cdapMessage.getOpCode().equals(Opcode.M_RELEASE_R)
				&& !cdapMessage.getOpCode().equals(Opcode.M_START_R)
				&& !cdapMessage.getOpCode().equals(Opcode.M_STOP_R)
				&& !cdapMessage.getOpCode().equals(Opcode.M_WRITE_R)
				&& cdapMessageHandler == null){
			throw new RIBDaemonException(RIBDaemonException.RESPONSE_REQUIRED_BUT_MESSAGE_HANDLER_IS_NULL);
		}
		
		try{
			serializedCDAPMessageToBeSend = cdapSessionManager.encodeNextMessageToBeSent(cdapMessage, portId);
			rmt.sendCDAPMessage(portId, serializedCDAPMessageToBeSend);
			cdapSessionManager.messageSent(cdapMessage, portId);
			log.debug("Sent CDAP Message: "+ cdapMessage.toString());
		}catch(Exception ex){
			ex.printStackTrace();
			if (ex.getMessage().equals("Flow closed")){
				cdapSessionManager.removeCDAPSession(portId);
			}
			throw new RIBDaemonException(RIBDaemonException.PROBLEMS_SENDING_CDAP_MESSAGE, ex);
		}
		
		if (cdapMessage.getInvokeID() != 0 && !cdapMessage.getOpCode().equals(Opcode.M_CONNECT) 
				&& !cdapMessage.getOpCode().equals(Opcode.M_RELEASE)
				&& !cdapMessage.getOpCode().equals(Opcode.M_CANCELREAD_R)
				&& !cdapMessage.getOpCode().equals(Opcode.M_CONNECT_R)
				&& !cdapMessage.getOpCode().equals(Opcode.M_CREATE_R) 
				&& !cdapMessage.getOpCode().equals(Opcode.M_READ_R)
				&& !cdapMessage.getOpCode().equals(Opcode.M_DELETE_R)
				&& !cdapMessage.getOpCode().equals(Opcode.M_RELEASE_R)
				&& !cdapMessage.getOpCode().equals(Opcode.M_START_R)
				&& !cdapMessage.getOpCode().equals(Opcode.M_STOP_R)
				&& !cdapMessage.getOpCode().equals(Opcode.M_WRITE_R)){
			messageHandlersWaitingForReply.put(portId+"-"+cdapMessage.getInvokeID(), cdapMessageHandler);
		}
	}
	
	/**
	 * Send an information update, consisting on a set of CDAP messages, using the updateStrategy update strategy
	 * (on demand, scheduled)
	 * @param cdapMessages
	 * @param updateStrategy
	 */
	public synchronized void sendMessages(CDAPMessage[] cdapMessage, UpdateStrategy arg1) {
		// TODO Auto-generated method stub
	}
	
	/**
	 * Add a RIB object to the RIB
	 * @param ribHandler
	 * @param objectName
	 * @throws RIBDaemonException
	 */
	public void addRIBObject(RIBObject ribObject) throws RIBDaemonException{
		rib.addRIBObject(ribObject);
		log.info("RIBObject with objectname "+ribObject.getObjectName()+", objectClass "+ribObject.getObjectClass()+", " +
				"objectInstance "+ribObject.getObjectInstance()+" added to the RIB");
	}
	
	/**
	 * Remove a ribObject from the RIB
	 * @param objectName
	 * @throws RIBDaemonException
	 */
	public void removeRIBObject(RIBObject ribObject) throws RIBDaemonException{
		if (ribObject != null){
			removeRIBObject(ribObject.getObjectName());
		}else{
			throw new RIBDaemonException(RIBDaemonException.RIB_OBJECT_AND_OBJECT_NAME_NULL, 
					"Both the RIBObject and objectname parameters are null");
		}
		
		
	}
	
	public void removeRIBObject(String objectName) throws RIBDaemonException{
		RIBObject ribObject = rib.removeRIBObject(objectName);
		if (ribObject == null){
			throw new RIBDaemonException(RIBDaemonException.OBJECTNAME_NOT_PRESENT_IN_THE_RIB, 
					"Could not find "+objectName+ " in the RIB");
		}
		
		log.info("RIBObject with objectname "+ribObject.getObjectName()+", objectClass "+ribObject.getObjectClass()+", " +
				"objectInstance "+ribObject.getObjectInstance()+" removed from the RIB");
	}
	
	/**
	 * Reads/writes/created/deletes/starts/stops one or more objects at the RIB, matching the information specified by objectId + objectClass or objectInstance.
	 * At least objectName or objectInstance have to be not null. This operation is invoked because the RIB Daemon has received a CDAP message from another 
	 * IPC process
	 * @param cdapMessage The CDAP message received
	 * @param cdapSessionDescriptor Describes the CDAP session to where the CDAP message belongs
	 * @throws RIBDaemonException on a number of circumstances
	 */
	public void processOperation(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException{
		log.debug("Remote operation "+cdapMessage.getOpCode()+" called on object "+cdapMessage.getObjName());
		RIBObject ribObject = null;
		
		switch(cdapMessage.getOpCode()){
		case M_CREATE:
			/* Creation is delegated to the parent objects if the object doesn't exist. Create semantics are CREATE or UPDATE. If the 
			 * object exists it is an update, therefore the message is handled to the object. If the object doesn't exist it is a CREATE, 
			 * therefore it is handled to the parent object*/
			try{
				ribObject = getRIBObject(cdapMessage.getObjName(), cdapMessage.getObjClass(), cdapMessage.getObjInst());
			}catch(RIBDaemonException ex){
				if(ex.getErrorCode() == RIBDaemonException.OBJECTNAME_NOT_PRESENT_IN_THE_RIB){
					//The object does not exist, call the parent object
					String parentObjectName = cdapMessage.getObjName().substring(0, cdapMessage.getObjName().lastIndexOf(RIBObjectNames.SEPARATOR));
					ribObject = getRIBObject(parentObjectName, null, 0);
				}else{
					throw ex;
				}
			}
			ribObject.create(cdapMessage, cdapSessionDescriptor);
			break;
		case M_DELETE:
			ribObject =  getRIBObject(cdapMessage.getObjName(), cdapMessage.getObjClass(), cdapMessage.getObjInst());
			ribObject.delete(cdapMessage, cdapSessionDescriptor);
			break;
		case M_READ:
			ribObject =  getRIBObject(cdapMessage.getObjName(), cdapMessage.getObjClass(), cdapMessage.getObjInst());
			ribObject.read(cdapMessage, cdapSessionDescriptor);
			break;
		case M_CANCELREAD:
			ribObject =  getRIBObject(cdapMessage.getObjName(), cdapMessage.getObjClass(), cdapMessage.getObjInst());
			ribObject.cancelRead(cdapMessage, cdapSessionDescriptor);
			break;
		case M_WRITE:
			ribObject =  getRIBObject(cdapMessage.getObjName(), cdapMessage.getObjClass(), cdapMessage.getObjInst());
			ribObject.write(cdapMessage, cdapSessionDescriptor);
			break;
		case M_START:
			ribObject =  getRIBObject(cdapMessage.getObjName(), cdapMessage.getObjClass(), cdapMessage.getObjInst());
			ribObject.start(cdapMessage, cdapSessionDescriptor);
			break;
		case M_STOP:
			ribObject =  getRIBObject(cdapMessage.getObjName(), cdapMessage.getObjClass(), cdapMessage.getObjInst());
			ribObject.stop(cdapMessage, cdapSessionDescriptor);
			break;
		default:
			throw new RIBDaemonException(RIBDaemonException.OPERATION_NOT_ALLOWED_AT_THIS_OBJECT);
		}
	}
	
	private RIBObject getRIBObject(String objectName, String objectClass, long objectInstance) throws RIBDaemonException{
		validateObjectArguments(objectClass, objectName, objectInstance);
		return rib.getRIBObject(objectName);
	}

	/**
	 * Create or update an object in the RIB
	 * @param objectClass the class of the object
	 * @param objectName the name of the object
	 * @param objectInstance the instance of the object
	 * @param objectValue the value of the object
	 * @param notify if not null notify some of the neighbors about the change
	 * @throws RIBDaemonException
	 */
	public synchronized void create(String objectClass, String objectName, long objectInstance, 
			Object objectValue, NotificationPolicy notificationPolicy) throws RIBDaemonException {
		log.debug("Local operation create called on object "+objectName);
		validateObjectArguments(objectClass, objectName, objectInstance);

		RIBObject ribObject = null;
		try{
			ribObject = getRIBObject(objectName, objectClass, objectInstance);
		}catch(RIBDaemonException ex){
			/* Creation is delegated to the parent objects if the object doesn't exist*/
			String parentObjectName = objectName.substring(0, objectName.lastIndexOf(RIBObjectNames.SEPARATOR));
			ribObject = getRIBObject(parentObjectName, null, 0);
		}

		//Create the object
		ribObject.create(objectClass, objectName, objectInstance, objectValue);
		
		//If we don't need to notify, we're done
		if (notificationPolicy == null){
			return;
		}
		
		//We need to notify, find out to whom the notifications must be sent to, and do it
		int[] peersNotToNotify = notificationPolicy.getCdapSessionIds();
		int[] peers = cdapSessionManager.getAllCDAPSessionIds();
		ObjectValue cdapObjectValue = new ObjectValue();
		
		try{
			cdapObjectValue.setByteval(encoder.encode(objectValue));
		}catch(Exception ex){
			log.error("Could not send notification of create object to remote peers because: "+ex.getMessage());
			return;
		}
		
		CDAPMessage cdapMessage = null;
		for(int i=0; i<peers.length; i++){
			if (!isOnList(peers[i], peersNotToNotify)){
				try{
					cdapMessage = cdapSessionManager.getCreateObjectRequestMessage(peers[i], null, null, 
							objectClass, objectInstance, objectName, cdapObjectValue, 0, false);
					this.sendMessage(cdapMessage, peers[i], null);
				}catch(Exception ex){
					log.error(ex);
				}
			}
		}
	}
	
	/**
	 * Finds out of the candidate number is on the list
	 * @param candidate
	 * @param list
	 * @return
	 */
	private boolean isOnList(int candidate, int[] list){
		for(int i=0; i<list.length; i++){
			if (list[i] == candidate){
				return true;
			}
		}
		
		return false;
	}

	public synchronized void delete(String objectClass, String objectName, long objectInstance, 
			Object objectValue, NotificationPolicy notificationPolicy) throws RIBDaemonException {
		log.debug("Local operation delete called on object "+objectName);
		validateObjectArguments(objectClass, objectName, objectInstance);
		RIBObject ribObject = getRIBObject(objectName, objectClass, objectInstance);
		ribObject.delete(objectClass, objectName, objectInstance, objectValue);
		
		//Create the object
		ribObject.create(objectClass, objectName, objectInstance, objectValue);
		
		//If we don't need to notify, we're done
		if (notificationPolicy == null){
			return;
		}
		
		//We need to notify, find out to whom the notifications must be sent to, and do it
		int[] peersNotToNotify = notificationPolicy.getCdapSessionIds();
		int[] peers = cdapSessionManager.getAllCDAPSessionIds();
		ObjectValue cdapObjectValue = null;
		
		if (objectValue != null){
			try{
				cdapObjectValue = new ObjectValue();
				cdapObjectValue.setByteval(encoder.encode(objectValue));
			}catch(Exception ex){
				log.error("Could not send notification of create object to remote peers because: "+ex.getMessage());
				return;
			}
		}
		
		CDAPMessage cdapMessage = null;
		for(int i=0; i<peers.length; i++){
			if (!isOnList(peers[i], peersNotToNotify)){
				try{
					cdapMessage = cdapSessionManager.getDeleteObjectRequestMessage(peers[i], null, null, 
							objectClass, objectInstance, objectName, cdapObjectValue, 0, false);
					this.sendMessage(cdapMessage, peers[i], null);
				}catch(Exception ex){
					log.error(ex);
				}
			}
		}
	}

	public synchronized RIBObject read(String objectClass, String objectName, long objectInstance) throws RIBDaemonException {
		log.debug("Local operation read called on object "+objectName);
		validateObjectArguments(objectClass, objectName, objectInstance);
		return getRIBObject(objectName, objectClass, objectInstance);
	}

	public synchronized void start(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		log.debug("Local operation start called on object "+objectName);
		validateObjectArguments(objectClass, objectName, objectInstance);
		RIBObject ribObject = getRIBObject(objectName, objectClass, objectInstance);
		ribObject.start(objectClass, objectName, objectInstance, object);
	}

	public synchronized void stop(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		log.debug("Local operation stop called on object "+objectName);
		validateObjectArguments(objectClass, objectName, objectInstance);
		RIBObject ribObject = getRIBObject(objectName, objectClass, objectInstance);
		ribObject.stop(objectClass, objectName, objectInstance, object);
	}

	/**
	 * Store an object to the RIB. This may cause a new object to be created in the RIB, or an existing object to be updated.
	 * @param objectClass optional if objectInstance specified, mandatory otherwise. A string identifying the class of the object
	 * @param objectInstance objectInstance optional if objectClass specified, mandatory otherwise. An id that uniquely identifies the object within a RIB
	 * @param objectName optional if objectClass specified, ignored otherwise. An id that uniquely identifies an object within an objectClass
	 * @param objectToWrite the object to be written to the RIB
	 * @throws RIBDaemonException if there are problems performing the "write" operation to the RIB
	 */
	public synchronized void write(String objectClass, String objectName, long objectInstance, 
			Object object, NotificationPolicy notification) throws RIBDaemonException {
		log.debug("Local operation write called on object "+objectName);
		validateObjectArguments(objectClass, objectName, objectInstance);
		RIBObject ribObject = getRIBObject(objectName, objectClass, objectInstance);
		ribObject.write(objectClass, objectName, objectInstance, object);
	}
	
	/**
	 * At least objectName must not be null or objectInstance != from -1
	 * @param objectClass
	 * @param objectName
	 * @param objectInstance
	 * @throws RIBDaemonException
	 */
	private void validateObjectArguments(String objectClass, String objectName, long objectInstance) throws RIBDaemonException{
		if (objectName == null){
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_AND_OBJECT_NAME_OR_OBJECT_INSTANCE_NOT_SPECIFIED);
		}
	}
	
	public List<RIBObject> getRIBObjects(){
		return this.rib.getRIBObjects();
	}
}
