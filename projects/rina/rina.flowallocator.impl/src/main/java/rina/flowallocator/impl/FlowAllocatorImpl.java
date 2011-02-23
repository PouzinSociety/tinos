package rina.flowallocator.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.AllocateRequest;
import rina.ipcservice.api.IPCException;
import rina.ribdaemon.api.MessageSubscriber;
import rina.ribdaemon.api.MessageSubscription;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.Opcode;
import rina.efcp.api.DataTransferAEFactory;
import rina.flowallocator.api.FlowAllocator;
import rina.flowallocator.api.FlowAllocatorInstance;
import rina.flowallocator.impl.FlowAllocatorInstanceImpl;
import rina.flowallocator.impl.validation.AllocateRequestValidator;

/** 
 * Implements the Flow Allocator
 */

public class FlowAllocatorImpl implements FlowAllocator, MessageSubscriber {
	
	private static final Log log = LogFactory.getLog(FlowAllocatorImpl.class);

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
	 * Flow allocator instances, each one associated to a port_id
	 */
	private Map<Integer, FlowAllocatorInstance> flowAllocatorInstances = null;
	
	/**
	 * Validates allocate requests
	 */
	private AllocateRequestValidator allocateRequestValidator = null;
	
	public FlowAllocatorImpl(){
		allocateRequestValidator = new AllocateRequestValidator();
		flowAllocatorInstances = new HashMap<Integer, FlowAllocatorInstance>();
		subscribeToFlowMessages();
		subscribeToEvents();
	}
	
	/**
	 * Subscribe to create flow and delete flow requests and responses
	 */
	private void subscribeToFlowMessages(){
		MessageSubscription subscription = new MessageSubscription();
		subscription.setObjClass("Flowobject");
		subscription.setOpCode(Opcode.M_CREATE);
		try{
			ipcProcess.getRibDaemon().subscribeToMessages(subscription, this);
		}catch(Exception ex){
			log.warn("Problems subscribing to Create Flow object request messages. "+ex.getMessage());
		}
		
		subscription = new MessageSubscription();
		subscription.setObjClass("Flowobject");
		subscription.setOpCode(Opcode.M_CREATE_R);
		try{
			ipcProcess.getRibDaemon().subscribeToMessages(subscription, this);
		}catch(Exception ex){
			log.warn("Problems subscribing to Create Flow object response messages. "+ex.getMessage());
		}
		
		subscription = new MessageSubscription();
		subscription.setObjClass("Flowobject");
		subscription.setOpCode(Opcode.M_DELETE);
		try{
			ipcProcess.getRibDaemon().subscribeToMessages(subscription, this);
		}catch(Exception ex){
			log.warn("Problems subscribing to Delete Flow object request messages. "+ex.getMessage());
		}
		
		subscription = new MessageSubscription();
		subscription.setObjClass("Flowobject");
		subscription.setOpCode(Opcode.M_DELETE_R);
		try{
			ipcProcess.getRibDaemon().subscribeToMessages(subscription, this);
		}catch(Exception ex){
			log.warn("Problems subscribing to Delete Flow object response messages. "+ex.getMessage());
		}
	}
	
	/**
	 * subscribe to SequenceNumberRollOverThreshold Events and any other required events
	 */
	private void subscribeToEvents(){
		//TODO implement this
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
			//received a create flow request from another IPC process, we have to process it
			//and deliver an M_CREATE_R
			createFlowRequestMessageReceived(cdapMessage);
			break;
		case M_CREATE_R:
			//received a create flow object response from another IPC process, we have to process it
			//and deliver and call the applicationProcess deliverAllocateResponse
			createFlowResponseMessageReceived(cdapMessage);
			break;
		case M_DELETE:
			//received a delete flow request from another IPC process, we have to process it
			//and deliver an M_DELETE_R
			deleteFlowRequestMessageReceived(cdapMessage);
			break;
		case M_DELETE_R:
			//received a delete flow response from another IPC process, we have to process it 
			//and call the applicationProcess deliverDeallocate
			deleteFlowResponseMessageReceived(cdapMessage);
			break;
		default:
			//Error, we should not have received this message, just log it
			log.error("Received a message that was not for me; "+cdapMessage.toString());
			break;
		}
	}
	
	/**
	 * When an Flow Allocator receives a Create_Request PDU for a Flow object, it consults its local Directory to see if it has an entry.
	 * If there is an entry and the address is this IPC Process, it creates an FAI and passes the Create_request to it.If there is an 
	 * entry and the address is not this IPC Process, it forwards the Create_Request to the IPC Process designated by the address.
	 * @param cdapMessage
	 */
	private void createFlowRequestMessageReceived(CDAPMessage cdapMessage){
		//TODO Check in the directory if the destination application process is registered to this IPC process
		
		//TODO if there is an entry and the address is not this IPC process, forward the CDAP message to that address
		// increment the hop count of the Flow object
		
		//TODO if there is an entry and the address is this IPC Process, create a FAI, extract the Flow object from the CDAP message and
		//call the FAI
		
		//TODO if there is not an entry, search the Directory Forwarding table to see to what IPC process it has to be forwarded
		//and forward the CDAP message to it. Increment the hop count of the Flow object
	}
	
	/**
	 * When a Create_Response PDU is received the InvokeID is used to deliver to the appropriate FAI.
	 * If the response was negative remove the flow allocator instance from the list of active
	 * flow allocator instances
	 * @param cdapMessage
	 */
	private void createFlowResponseMessageReceived(CDAPMessage cdapMessage){
		//TODO implement this
	}
	
	/**
	 * Forward to the FAI. When it completes, remove the flow allocator instance from the list of active
	 * flow allocator instances
	 * @param cdapMessage
	 */
	private void deleteFlowRequestMessageReceived(CDAPMessage cdapMessage){
		//TODO implement this
	}
	
	/**
	 * Forward to the FAI.When it completes, remove the flow allocator instance from the list of active
	 * flow allocator instances.
	 * @param cdapMessage
	 */
	private void deleteFlowResponseMessageReceived(CDAPMessage cdapMessage){
		//TODO implement this
	}
	
	/**
	 * Validate the request, create a Flow Allocator Instance and forward it the request for further processing
	 * @param allocateRequest
	 * @param portId
	 * @throws IPCException
	 */
	public void submitAllocateRequest(AllocateRequest allocateRequest, int portId) throws IPCException{
		allocateRequestValidator.validateAllocateRequest(allocateRequest);
		
		FlowAllocatorInstance flowAllocatorInstance = new FlowAllocatorInstanceImpl(this.ipcProcess, portId);
		flowAllocatorInstance.submitAllocateRequest(allocateRequest, portId);
		flowAllocatorInstances.put(new Integer(portId), flowAllocatorInstance);
	}

	/**
	 * Forward the call to the right FlowAllocator Instance. If the application process 
	 * rejected the flow request, remove the flow allocator instance from the list of 
	 * active flow allocator instances
	 * @param portId
	 * @param success
	 */
	public void submitAllocateResponse(int portId, boolean success) {
		FlowAllocatorInstance flowAllocatorInstance = flowAllocatorInstances.get(portId);
		if (flowAllocatorInstance == null){
			log.error("Could not find the Flow Allocator Instance associated to the portId "+portId);
			return;
		}
		
		flowAllocatorInstance.submitAllocateResponse(portId, success);
		if (!success){
			flowAllocatorInstances.remove(portId);
		}
	}

	/**
	 * Forward the deallocate request to the Flow Allocator Instance.
	 * @param portId
	 */
	public void submitDeallocate(int portId) {
		FlowAllocatorInstance flowAllocatorInstance = flowAllocatorInstances.get(portId);
		if (flowAllocatorInstance == null){
			log.error("Could not find the Flow Allocator Instance associated to the portId "+portId);
			return;
		}
		
		flowAllocatorInstance.submitDeallocate(portId);
	}
	
}