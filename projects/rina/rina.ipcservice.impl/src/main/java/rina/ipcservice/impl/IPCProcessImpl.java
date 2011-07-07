package rina.ipcservice.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.WhatevercastName;
import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.Opcode;
import rina.cdap.api.message.ObjectValue;
import rina.efcp.api.DataTransferAE;
import rina.efcp.api.DataTransferAEInstance;
import rina.encoding.api.BaseEncoder;
import rina.encoding.api.Encoder;
import rina.flowallocator.api.FlowAllocator;
import rina.ipcprocess.api.BaseIPCProcess;
import rina.ipcservice.api.AllocateRequest;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.IPCException;
import rina.ipcservice.api.IPCService;
import rina.ipcservice.api.APService;
import rina.ipcservice.impl.jobs.DeliverAllocateResponseJob;
import rina.ipcservice.impl.jobs.DeliverDeallocateJob;
import rina.ipcservice.impl.jobs.DeliverSDUJob;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBHandler;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * Point of entry to the IPC process for the application process. It is in charge 
 * of orchestrating the calls provided by the IPCService interface (by delegating to 
 * the FlowAllocator, RIBDaemon, ...). It also contains the pointers to the client 
 * application processes
 * @author eduardgrasa
 *
 */
public class IPCProcessImpl extends BaseIPCProcess implements IPCService, RIBHandler{
	
	private static final Log log = LogFactory.getLog(IPCProcessImpl.class);
	
	/**
	 * The maximum number of worker threads in the IPC Process thread pool
	 */
	private static int MAXWORKERTHREADS = 5;
	
	/**
	 * Stores the applications that have a port Id in allocation pending state
	 */
	private Map<Integer, APService> allocationPendingApplicationProcesses = null;
	
	/**
	 * Stores the applications that have a port Id in transfer state
	 */
	private Map<Integer, APService> transferApplicationProcesses = null;
	
	/**
	 * The thread pool implementation
	 */
	private ExecutorService executorService = null;
	
	public IPCProcessImpl(String applicationProcessName, String applicationProcessInstance, RIBDaemon ribDaemon){
		this.executorService = Executors.newFixedThreadPool(MAXWORKERTHREADS);
		this.allocationPendingApplicationProcesses = new HashMap<Integer, APService>();
		this.transferApplicationProcesses = new HashMap<Integer, APService>();
		this.setApplicationProcessName(applicationProcessName);
		this.setApplicationProcessInstance(applicationProcessInstance);
		subscribeToRIBDaemon(ribDaemon);
	}
	
	/**
	 * Tell the ribDaemon the portions of the RIB space that the IPC process will manage
	 * @param ribDaemon
	 */
	private void subscribeToRIBDaemon(RIBDaemon ribDaemon){
		try{
			ribDaemon.addRIBHandler(this, RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.APNAME);
			ribDaemon.addRIBHandler(this, RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.CURRENT_SYNONYM);
			ribDaemon.addRIBHandler(this, RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.SYNONYMS);
			ribDaemon.addRIBHandler(this, RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.WHATEVERCAST_NAMES);
		}catch(RIBDaemonException ex){
			ex.printStackTrace();
			log.error("Could not subscribe to RIB Daemon:" +ex.getMessage());
		}
	}
	
	/**
	 * A CDAP Message has been received. The opcode must be M_READ, M_WRITE, M_CANCELREAD, M_CREATE, M_DELETE, M_START or M_STOP
	 */
	public void processOperation(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException {
		CDAPMessage responseMessage = null;
		Encoder encoder = (Encoder) this.getIPCProcessComponent(BaseEncoder.getComponentName());
		RIBDaemon ribDaemon = (RIBDaemon) this.getIPCProcessComponent(BaseRIBDaemon.getComponentName());

		switch(cdapMessage.getOpCode()){
		case M_READ:
			responseMessage = handleRemoteRead(cdapMessage, encoder);
			break;
		case M_WRITE:
			//TODO
			break;
		case M_CREATE:
			//TODO
			break;
		case M_DELETE:
			//TODO
			break;
		case M_START:
			//TODO
			break;
		case M_STOP:
			//TODO
			break;
		case M_CANCELREAD:
			//TODO
			break;
		default:
			throw new RIBDaemonException(RIBDaemonException.OPERATION_NOT_ALLOWED_AT_THIS_OBJECT, 
					"Operation "+cdapMessage.getOpCode()+" not allowed for objectName "+cdapMessage.getObjName());

		}

		if (responseMessage != null){
			ribDaemon.sendMessage(responseMessage, cdapSessionDescriptor.getPortId(), null);
		}
	}
	
	/**
	 * Contains the logics to handle a remote M_READ message received by this task
	 * @param cdapMessage
	 * @param encoder
	 * @return
	 */
	private CDAPMessage handleRemoteRead(CDAPMessage cdapMessage, Encoder encoder){
		CDAPMessage responseMessage = null;
		try{
			Object object = this.handleRead(cdapMessage.getObjName());
			ObjectValue objectValue = new ObjectValue();
			objectValue.setByteval(encoder.encode(object));
			responseMessage = CDAPMessage.getReadObjectResponseMessage(null, cdapMessage.getInvokeID(), cdapMessage.getObjClass(), 
					cdapMessage.getObjInst(), cdapMessage.getObjName(), objectValue, 0, null);
		}catch(RIBDaemonException ex){
			try{
				responseMessage = CDAPMessage.getReadObjectResponseMessage(null, cdapMessage.getInvokeID(), cdapMessage.getObjClass(), 
						cdapMessage.getObjInst(), cdapMessage.getObjName(), null, 1, ex.getMessage());
			}catch(CDAPException cdapEx){
				log.error(cdapEx);
			}
		}catch(Exception ex){
			log.error(ex);
		}
		
		return responseMessage;
	}

	/**
	 * Called intern
	 */
	public Object processOperation(Opcode opcode, String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		switch(opcode){
		case M_READ:
			return handleRead(objectName);
		case M_WRITE:
			handleWrite(objectName, object);
			break;
		case M_CANCELREAD:
			break;
		case M_CREATE:
			handleCreate(objectName, object);
			break;
		case M_DELETE:
			handleDelete(objectName, object);
			break;
		default:
			throw new RIBDaemonException(RIBDaemonException.OPERATION_NOT_ALLOWED_AT_THIS_OBJECT, "Operation "+opcode+" not allowed for objectName "+objectName);
		}
		
		return null;
	}
	
	/**
	 * Takes care of the M_READ operations for the objectnames that this class owns;
	 * @param objectName
	 * @return
	 * @throws RIBDaemonException
	 */
	private Object handleRead(String objectName) throws RIBDaemonException {
		if (objectName.equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.APNAME)){
			return this.getApplicationProcessNamingInfo();
		}else if (objectName.equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.CURRENT_SYNONYM)){
			return this.getCurrentSynonym();
		}else if (objectName.equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.SYNONYMS)){
			List<byte[]> result = new ArrayList<byte[]>();
			result.add(this.getCurrentSynonym());
			return result;
		}else if (objectName.equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.WHATEVERCAST_NAMES)){
			return this.getWhatevercastNames();
		}
		
		throw new RIBDaemonException(RIBDaemonException.UNRECOGNIZED_OBJECT_NAME, "Unrecognized object name");
	}
	
	/**
	 * Takes care of the M_WRITE operations for the objectnames that this class owns;
	 * @param objectName
	 * @param object
	 * @throws RIBDaemonException
	 */
	private void handleWrite(String objectName, Object object) throws RIBDaemonException{
		if (objectName.equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.APNAME)){
			if (!(object instanceof ApplicationProcessNamingInfo)){
				throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
						"Object class ("+object.getClass().getName()+") does not match object name "+objectName);
			}
			this.setApplicationProcessNamingInfo((ApplicationProcessNamingInfo)object);
		}else if (objectName.equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.CURRENT_SYNONYM)){
			if (!(object instanceof byte[])){
				throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
						"Object class ("+object.getClass().getName()+") does not match object name "+objectName);
			}
			this.setCurrentSynonym((byte[])object);
		}else if (objectName.equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.SYNONYMS)){
			//TODO don't have synonym list yet
		}else if (objectName.equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.WHATEVERCAST_NAMES)){
			throw new RIBDaemonException(RIBDaemonException.OPERATION_NOT_ALLOWED_AT_THIS_OBJECT, "Operation M_WRITE not allowed for objectName "+objectName);
		}

		throw new RIBDaemonException(RIBDaemonException.UNRECOGNIZED_OBJECT_NAME, "Unrecognized object name");
	}
	
	/**
	 * Takes care of the M_CREATE operations for the objectnames that this class owns;
	 * @param objectName
	 * @param object
	 * @throws RIBDaemonException
	 */
	private void handleCreate(String objectName, Object object) throws RIBDaemonException{
		if (objectName.equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.APNAME)){
			throw new RIBDaemonException(RIBDaemonException.OPERATION_NOT_ALLOWED_AT_THIS_OBJECT, "Operation M_WRITE not allowed for objectName "+objectName);
		}else if (objectName.equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.CURRENT_SYNONYM)){
			throw new RIBDaemonException(RIBDaemonException.OPERATION_NOT_ALLOWED_AT_THIS_OBJECT, "Operation M_WRITE not allowed for objectName "+objectName);
		}else if (objectName.equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.SYNONYMS)){
			//TODO don't have synonym list yet
		}else if (objectName.equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.WHATEVERCAST_NAMES)){
			if (!(object instanceof WhatevercastName)){
				throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
						"Object class ("+object.getClass().getName()+") does not match object name "+objectName);
			}
			this.getWhatevercastNames().add((WhatevercastName) object);
		}

		throw new RIBDaemonException(RIBDaemonException.UNRECOGNIZED_OBJECT_NAME, "Unrecognized object name");
	}
	
	/**
	 * Takes care of the M_DELETE operations for the objectnames that this class owns;
	 * @param objectName
	 * @param object
	 * @throws RIBDaemonException
	 */
	private void handleDelete(String objectName, Object object) throws RIBDaemonException{
		if (objectName.equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.APNAME)){
			this.setApplicationProcessNamingInfo(null);
		}else if (objectName.equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.CURRENT_SYNONYM)){
			this.setCurrentSynonym(null);
		}else if (objectName.equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.SYNONYMS)){
			//TODO don't have synonym list yet
		}else if (objectName.equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.WHATEVERCAST_NAMES)){
			this.getWhatevercastNames().removeAll(this.getWhatevercastNames());
		}

		throw new RIBDaemonException(RIBDaemonException.UNRECOGNIZED_OBJECT_NAME, "Unrecognized object name");
	}
	

	public synchronized void deliverSDUsToApplicationProcess(List<byte[]> sdus, int portId) {
		APService applicationProcess = transferApplicationProcesses.get(new Integer(portId));
		
		if (applicationProcess == null ){
			//TODO, log, throw Exception?
			return;
		}
		
		if (sdus == null){
			//TODO, log, throw Exception?
			return;
		}
		
		executorService.execute(new DeliverSDUJob(applicationProcess, sdus, portId));
	}

	/**
	 * Forward the allocate request to the Flow Allocator. Before, choose an available portId
	 * @param allocateRequest
	 * @param applicationProcess
	 */
	public synchronized void submitAllocateRequest(AllocateRequest allocateRequest, APService applicationProcess){
		int portId = choosePortId();
		allocationPendingApplicationProcesses.put(new Integer(portId), applicationProcess);
		try{
			FlowAllocator flowAllocator = (FlowAllocator) this.getIPCProcessComponent(FlowAllocator.class.getName());
			flowAllocator.submitAllocateRequest(allocateRequest, portId);
		}catch(IPCException ex){
			//Something in the validation request was not valid or there are not enough 
			//resources to honour the request. Notify the application process
			allocationPendingApplicationProcesses.remove(new Integer(portId));
			executorService.execute(new DeliverAllocateResponseJob(
					applicationProcess, allocateRequest.getRequestedAPinfo(), -1, ex.getErrorCode(), ex.getMessage()));
		}
	}
	
	/**
	 * Select a portId that is available
	 * @return
	 */
	private int choosePortId(){
		for(int i=1; i<Integer.MAX_VALUE; i++){
			Integer candidate = new Integer(i);
			if (!allocationPendingApplicationProcesses.keySet().contains(candidate) && 
					!transferApplicationProcesses.keySet().contains(candidate)){
				return i;
			}
		}
		
		return 0;
	}

	/**
	 * Forward the allocate response to the Flow Allocator.
	 * @param portId
	 * @param success
	 */
	public synchronized void submitAllocateResponse(int portId, boolean success) throws IPCException{
		Integer key = new Integer(portId);
		
		if (!allocationPendingApplicationProcesses.keySet().contains(key)){
			throw new IPCException(IPCException.PORTID_NOT_IN_ALLOCATION_PENDING_STATE);
		}
		
		APService apService = allocationPendingApplicationProcesses.remove(key);
		if (success){
			transferApplicationProcesses.put(key, apService);
		}
		
		FlowAllocator flowAllocator = (FlowAllocator) this.getIPCProcessComponent(FlowAllocator.class.getName());
		flowAllocator.submitAllocateResponse(portId, success);
	}

	/**
	 * Forward the deallocate call to the Flow Allocator
	 * @param portId 
	 */
	public synchronized void submitDeallocate(int portId) throws IPCException{
		Integer key = new Integer(portId);
		
		if (!transferApplicationProcesses.keySet().contains(key)){
			throw new IPCException(IPCException.PORTID_NOT_IN_TRANSFER_STATE);
		}
		
		transferApplicationProcesses.remove(key);
		
		FlowAllocator flowAllocator = (FlowAllocator) this.getIPCProcessComponent(FlowAllocator.class.getName());
		flowAllocator.submitDeallocate(portId);
	}
	
	/**
	 * Call the applicationProcess deallocate.deliver operation
	 * @param portId
	 */
	public void deliverDeallocateRequestToApplicationProcess(int portId) {
		Integer key = new Integer(portId);
		
		if (!transferApplicationProcesses.keySet().contains(key)){
			log.error("Could not find an application process with portId "+portId+" in transfer state");
			return;
		}
		
		APService apService = transferApplicationProcesses.remove(key);
		executorService.execute(new DeliverDeallocateJob(apService, portId));
	}

	public synchronized void submitStatus(int arg0) {
		// TODO Auto-generated method stub
	}

	public synchronized  void submitTransfer(int portId, byte[] sdu) throws IPCException{
		Integer key = new Integer(portId);
		
		if (!transferApplicationProcesses.keySet().contains(key)){
			throw new IPCException(IPCException.PORTID_NOT_IN_TRANSFER_STATE);
		}
		
		List<byte[]> sdus = new ArrayList<byte[]>();
		sdus.add(sdu);
		DataTransferAE dataTransferAE = (DataTransferAE) this.getIPCProcessComponent(DataTransferAE.class.getName());
		DataTransferAEInstance dataTransferAEInstance = dataTransferAE.getDataTransferAEInstance(portId);
		dataTransferAEInstance.sdusDelivered(sdus);
	}

	/**
	 * An application says it is no longer available through this DIF
	 */
	public synchronized void unregister(ApplicationProcessNamingInfo apNamingInfo) {
		FlowAllocator flowAllocator = (FlowAllocator) this.getIPCProcessComponent(FlowAllocator.class.getName());
		flowAllocator.getDirectoryForwardingTable().removeEntry(apNamingInfo);
		//TODO tell the RIB Daemon to disseminate this
	}
	
	/**
	 * An application process says it is available through this DIF
	 */
	public synchronized void register(ApplicationProcessNamingInfo apNamingInfo) {
		FlowAllocator flowAllocator = (FlowAllocator) this.getIPCProcessComponent(FlowAllocator.class.getName());
		flowAllocator.getDirectoryForwardingTable().addEntry(apNamingInfo, this.getCurrentSynonym());
		//TODO tell the RIB Daemon to disseminate this
	}
	
	public void destroy(){
		executorService.shutdown();
	}
}