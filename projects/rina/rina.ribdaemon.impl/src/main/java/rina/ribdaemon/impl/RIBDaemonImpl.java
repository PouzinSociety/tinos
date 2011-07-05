package rina.ribdaemon.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.BaseCDAPSessionManager;
import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.Opcode;
import rina.enrollment.api.BaseEnrollmentTask;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBHandler;
import rina.ribdaemon.api.UpdateStrategy;
import rina.ribdaemon.impl.rib.RIB;
import rina.ribdaemon.impl.rib.RIBNode;

/**
 * RIBDaemon that stores the objects in memory
 * @author eduardgrasa
 *
 */
public class RIBDaemonImpl extends BaseRIBDaemon{
	
	private static final Log log = LogFactory.getLog(RIBDaemonImpl.class);
	
	/** Create, retrieve and delete CDAP sessions **/
	private CDAPSessionManager cdapSessionManager = null;
	
	/** The RIB **/
	private RIB rib = null;
	
	public RIBDaemonImpl(){
		rib = new RIB();
	}
	
	private CDAPSessionManager getCDAPSessionManager(){
		if (this.cdapSessionManager == null){
			this.cdapSessionManager = (CDAPSessionManager) getIPCProcess().getIPCProcessComponent(BaseCDAPSessionManager.getComponentName());
		}
		
		return this.cdapSessionManager;
	}

	/**
	 * Invoked by the RMT when it detects a CDAP message. The RIB Daemon has to process the CDAP message and, 
	 * if valid, it will either pass it to interested subscribers and/or write to storage and/or modify other 
	 * tasks data structures. It may be the case that the CDAP message is not addressed to an application 
	 * entity within this IPC process, then the RMT may decide to rely the message to the right destination 
	 * (after consulting an adequate forwarding table).
	 * @param cdapMessage
	 */
	public void cdapMessageDelivered(byte[] encodedCDAPMessage, int portId){
		CDAPMessage cdapMessage = null;
		CDAPSessionDescriptor cdapSessionDescriptor = null;
		
		log.debug("Got an encoded CDAP message from portId "+portId);
		
		//1 Decode the message and obtain the CDAP session descriptor
		try{
			cdapMessage = getCDAPSessionManager().messageReceived(encodedCDAPMessage, portId);
			cdapSessionDescriptor = getCDAPSessionManager().getCDAPSession(portId).getSessionDescriptor();
		}catch(CDAPException ex){
			log.error("Error decoding CDAP message: " + ex.getMessage());
			ex.printStackTrace();
			return;
		}
		
		Opcode opcode = cdapMessage.getOpCode();
		
		RIBHandler ribHandler = null;
		
		if (opcode == Opcode.M_CONNECT || opcode == Opcode.M_CONNECT_R || opcode == Opcode.M_RELEASE || opcode == Opcode.M_RELEASE_R){
			ribHandler = (RIBHandler) this.getIPCProcess().getIPCProcessComponent(BaseEnrollmentTask.getComponentName());
		}else if (opcode == Opcode.M_READ  || opcode == Opcode.M_WRITE || opcode == Opcode.M_CREATE || 
				opcode == Opcode.M_DELETE || opcode == Opcode.M_START || opcode == Opcode.M_STOP || opcode == Opcode.M_CANCELREAD){
			ribHandler = this;
		}else{
			//TODO what to do with the Response messages
		}
		
		try{
			ribHandler.processOperation(cdapMessage, cdapSessionDescriptor);
		}catch(RIBDaemonException ex){
			ex.printStackTrace();
			log.error(ex);
		}
	}
	
	/**
	 * Invoked by the RMT when it detects that a certain flow has been deallocated, and therefore any CDAP sessions 
	 * over it should be terminated.
	 * @param portId identifies the flow that has been deallocated
	 */
	public void flowDeallocated(int portId) {
		getCDAPSessionManager().removeCDAPSession(portId);
		//TODO inform all the subscribers about this?
	}
	
	/**
	 * Store an object to the RIB. This may cause a new object to be created in the RIB, or an existing object to be updated.
	 * @param objectClass optional if objectInstance specified, mandatory otherwise. A string identifying the class of the object
	 * @param objectInstance objectInstance optional if objectClass specified, mandatory otherwise. An id that uniquely identifies the object within a RIB
	 * @param objectName optional if objectClass specified, ignored otherwise. An id that uniquely identifies an object within an objectClass
	 * @param objectToWrite the object to be written to the RIB
	 * @throws RIBDaemonException if there are problems performing the "write" operation to the RIB
	 */
	public synchronized void write(String objectClass, long objectInstance, String objectName, Object objectToWrite) throws RIBDaemonException{
		validateObjectArguments(objectClass, objectName, objectInstance);

	}

	/**
	 * Remove an object from the RIB
	 * @param objectClass optional if objectInstance specified, mandatory otherwise. A string identifying the class of the object
	 * @param objectInstance objectInstance optional if objectClass specified, mandatory otherwise. An id that uniquely identifies the object within a RIB
	 * @param objectName optional if objectClass specified, ignored otherwise. An id that uniquely identifies an object within an objectClass
	 * @throws RIBDaemonException if there are problems removinb the objects from the RIB
	 */
	public synchronized void remove(String objectClass, long objectInstance, String objectName) throws RIBDaemonException{
		validateObjectArguments(objectClass, objectName, objectInstance);
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
	 * Add a ribHandler for a certain object name
	 * @param ribHandler
	 * @param objectName
	 * @throws RIBDaemonException
	 */
	public void addRIBHandler(RIBHandler ribHandler, String objectName) throws RIBDaemonException{
		RIBNode ribNode = rib.getRIBNode(objectName);
		ribNode.setRIBHandler(ribHandler);
	}
	
	/**
	 * Remove a ribHandler from a certain object name
	 * @param objectName
	 * @throws RIBDaemonException
	 */
	public void removeRIBHandler(RIBHandler ribHandler, String objectName) throws RIBDaemonException{
		RIBNode ribNode = rib.getRIBNode(objectName);
		ribNode.setRIBHandler(null);
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
		RIBNode ribNode = getRIBNode(cdapMessage.getObjName(), cdapMessage.getObjClass(), cdapMessage.getObjInst());
		ribNode.getRIBHandler().processOperation(cdapMessage, cdapSessionDescriptor);
	}
	
	/**
	 * Reads/writes/created/deletes/starts/stops one or more objects at the RIB, matching the information specified by objectId + objectClass or objectInstance.
	 * At least objectName or objectInstance have to be not null. This method is invoked by tasks that are internal to the IPC process.
	 * @param opcode the operation (can only be read/write/create/delete/start/stop)
	 * @param objectClass
	 * @param objectName
	 * @param objectInstance
	 * @param objectToWrite the object that will be written if required by the operation
	 * @return may return an object (or a collection of objects) depending on the operation, null otherwise
	 * @throws RIBDaemonException on a number of circumstances
	 */
	public Object processOperation(Opcode opcode, String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException{
		RIBNode ribNode = getRIBNode(objectName, objectClass, objectInstance);
		return ribNode.getRIBHandler().processOperation(opcode, objectClass, objectName, objectInstance, object);
	}
	
	private RIBNode getRIBNode(String objectName, String objectClass, long objectInstance) throws RIBDaemonException{
		validateObjectArguments(objectClass, objectName, objectInstance);
		if(objectName != null){
			return rib.getRIBNode(objectName);
		}else{
			return rib.getRIBNode(objectInstance);
		}
	}
	
	/**
	 * At least objectclass must not be null or objectInstance != from -1
	 * @param objectClass
	 * @param objectName
	 * @param objectInstance
	 * @throws RIBDaemonException
	 */
	private void validateObjectArguments(String objectClass, String objectName, long objectInstance) throws RIBDaemonException{
		if (objectClass == null && objectInstance == -1){
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_AND_OBJECT_NAME_OR_OBJECT_INSTANCE_NOT_SPECIFIED);
		}
	}
}
