package rina.flowallocator.impl;

import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.AllocateRequest;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.IPCException;
import rina.ipcservice.api.IPCService;
import rina.ribdaemon.api.MessageSubscriber;
import rina.ribdaemon.api.MessageSubscription;
import rina.ribdaemon.api.RIBDaemon;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.Opcode;
import rina.efcp.api.DataTransferAEFactory;
import rina.flowallocator.api.FlowAllocator;
import rina.flowallocator.impl.FlowAllocatorInstance;

/** 
 * Implements the Flow Allocator
 */

public class FlowAllocatorImpl implements FlowAllocator, MessageSubscriber {

	/**
	 * A pointer to the IPC Process
	 */
	private IPCProcess ipcProcess = null;

	/**
	 * A pointer to the Data Transfer Application Entity Factory, so that 
	 * the Flow Allocator can create Data Transfer AE Instances
	 */
	private DataTransferAEFactory dataTransferAEFactory = null;

	/**
	 * A pointer to the flow allocator instance factory, which creates 
	 * and manages flow allocator instances
	 */
	private FlowAllocatorInstanceFactory faiFactory = null;

	private ApplicationProcessNamingInfo requestedAPinfo = null;
	private int portId = 0;
	//private QoSCube cube = null;
	private boolean result = false;
	private int FAid = 0;
	private FlowAllocatorInstance FAI = null;
	private boolean validAllocateRequest = false;
	private boolean validApplicationProcessNamingInfo = false;
	private MessageSubscription subscription = null;
	
	public FlowAllocatorImpl() throws Exception{
		//Subscribe to create flow and delete flow requests and responses
		subscription = new MessageSubscription();
		subscription.setObjClass("Flowobject");
		subscription.setOpCode(Opcode.M_CREATE);
		ipcProcess.getRibDaemon().subscribeToMessages(subscription, this);
		
		subscription = new MessageSubscription();
		subscription.setObjClass("Flowobject");
		subscription.setOpCode(Opcode.M_CREATE_R);
		ipcProcess.getRibDaemon().subscribeToMessages(subscription, this);
		
		subscription = new MessageSubscription();
		subscription.setObjClass("Flowobject");
		subscription.setOpCode(Opcode.M_DELETE);
		ipcProcess.getRibDaemon().subscribeToMessages(subscription, this);
		
		subscription = new MessageSubscription();
		subscription.setObjClass("Flowobject");
		subscription.setOpCode(Opcode.M_DELETE_R);
		ipcProcess.getRibDaemon().subscribeToMessages(subscription, this);
	}
	
	public void setIPCProcess(IPCProcess ipcProcess) {
		this.ipcProcess = ipcProcess;
	}

	public DataTransferAEFactory getDataTransferAEFactory() {
		return dataTransferAEFactory;
	}

	public void setDataTransferAEFactory(DataTransferAEFactory dataTransferAEFactory) {
		this.dataTransferAEFactory = dataTransferAEFactory;
	}
	
	/**
	 * Invoked by the RIB Daemon when it has a CDAP message for the flow allocator
	 */
	public void messageReceived(CDAPMessage cdapMessage) {
		switch (cdapMessage.getOpCode()){
		case M_CREATE:
			//TODO received a create flow request from another IPC process, we have to process it
			//and deliver an M_CREATE_R
			createFlowRequestReceived(cdapMessage);
			break;
		case M_CREATE_R:
			//TODO received a create flow object response from another IPC process, we have to process it
			//and deliver and call the applicationProcess deliverAllocateResponse
			createFlowResponseReceived(cdapMessage);
			break;
		case M_DELETE:
			//TODO received a delete flow request from another IPC process, we have to process it
			//and deliver an M_DELETE_R
			break;
		case M_DELETE_R:
			//TODO received a delete flow response from another IPC process, we have to process it 
			//and call the applicationProcess deliverDeallocate
			break;
		default:
			//TODO Error, we should not have received this message, just log it
			break;
		}
	}
	
	/**
	 * When an Flow Allocator receives a Create_Request PDU for a Flow object, it consults its local Directory to see if it has an entry.
	 * If there is an entry and the address is this IPC Process, it creates an FAI and passes the Create_request to it.If there is an 
	 * entry and the address is not this IPC Process, it forwards the Create_Request to the IPC Process designated by the address.
	 * @param cdapMessage
	 */
	private void createFlowRequestReceived(CDAPMessage cdapMessage){
		//TODO
	}
	
	/**
	 * 
	 * @param cdapMessage
	 */
	private void createFlowResponseReceived(CDAPMessage cdapMessage){
		//TODO
	}
	
	public void submitAllocateRequest(AllocateRequest allocateRequest, APService applicationProcess, int portId) throws IPCException{
		// TODO Auto-generated method stub
		
	}

	public void submitAllocateRequest(AllocateRequest allocateRequest) {
		try {
			validateRequest(allocateRequest);

			allocateRequest.setPort_id(assignPortId());
			FAI = faiFactory.createFlowAllocatorInstance(allocateRequest);
			FAI.forwardAllocateRequest(allocateRequest);
			
			//subscribeToMessages(subscription, subscriber);

			// TODO check AllocateNotifyPolicy to see if you should return AllocateResponse 
			// if(AllocateNotifyPolicy)
			// deliverAllocateResponse(request.getRequestedAPinfo(), request.getPort_id(), true, "");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//		else
		//			deliverAllocateResponse(request.getRequestedAPinfo(), request.getPort_id(), false, "The format of request was not valid");
	}

	/**
	 * 
	 * Assigns a port-id for the specific AllocateRequest
	 */
	private int assignPortId() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void submitAllocateResponse(int portId, boolean result) {
		// TODO Auto-generated method stub

	}

	public void submitDeallocate(int portId) {
		// TODO Auto-generated method stub

	}


	public void submitStatus(int portId) {
		// TODO Auto-generated method stub

	}

	public void submitTransfer(int portId, byte[] sdu, boolean result) {
		// TODO Auto-generated method stub

	}

	/**
	 * Validates an AllocateRequest: 
	 * 		- ApplicationProcessNamingInfo requestedAPinfo
	 * 		- int port_id
	 * 		- QoSCube cube
	 * 		- boolean result
	 * @param request
	 * @return yes if valid, no otherwise
	 * @throws Exception 
	 */
	public static void validateRequest(AllocateRequest request) throws Exception{
		validateApplicationProcessNamingInfo(request.getRequestedAPinfo());
		//validateQoScube(request.getCube());

	}


	public boolean submitTransfer(int portId, byte[] sdu) {
		// TODO Auto-generated method stub
		return false;
	}


	public void register(
			ApplicationProcessNamingInfo applicationProcessNamingInfo) {
		// TODO Auto-generated method stub

	}

	public void unregister(
			ApplicationProcessNamingInfo applicationProcessNamingInfo) {
		// TODO Auto-generated method stub

	}


	/** 
	 * Validates the AP naming info (applicationProcessName, applicationProcessInstance, applicationEntityName and applicationEntityInstance)
	 * @param APnamingInfo
	 * @throws Exception
	 */
	public static void validateApplicationProcessNamingInfo(ApplicationProcessNamingInfo APnamingInfo) throws Exception{
		validateApplicationProcessName(APnamingInfo.getApplicationProcessName());
		validateApplicationProcessInstance(APnamingInfo.getApplicationProcessInstance());
		validateApplicationEntityName(APnamingInfo.getApplicationEntityName());
		validateApplicationEntityInstance(APnamingInfo.getApplicationEntityInstance());
	}

	/**
	 * Validates the applicationProcessName
	 * @param applicationProcessName
	 * @throws Exception
	 */
	private static void validateApplicationProcessName(String applicationProcessName) throws Exception
	{
		if (applicationProcessName.equals(null))
			throw new Exception("Application process name is empty");
	}


	/**
	 * Validates the applicationProcessInstance
	 * @param applicationProcessInstance
	 * @throws Exception
	 */
	private static void validateApplicationProcessInstance(String applicationProcessInstance) throws Exception
	{

	}



	/**
	 * Validates the applicationEntityName
	 * @param ApplicationEntityName
	 * @throws Exception
	 */
	private static void validateApplicationEntityName(String ApplicationEntityName) throws Exception
	{

	}



	/**
	 * Validates the applicationEntityInstance
	 * @param applicationEntityInstance
	 * @throws Exception
	 */
	private static void validateApplicationEntityInstance(String applicationEntityInstance) throws Exception
	{

	}


}
