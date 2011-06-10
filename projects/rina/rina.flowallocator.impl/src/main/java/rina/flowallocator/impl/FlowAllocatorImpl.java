package rina.flowallocator.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.AllocateRequest;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.IPCException;
import rina.ribdaemon.api.MessageSubscriber;
import rina.ribdaemon.api.MessageSubscription;
import rina.cdap.api.CDAPException;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.Opcode;
import rina.cdap.api.message.ObjectValue;
import rina.flowallocator.api.DirectoryForwardingTable;
import rina.flowallocator.api.FlowAllocator;
import rina.flowallocator.api.FlowAllocatorInstance;
import rina.flowallocator.api.message.Flow;
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
	 * Flow allocator instances, each one associated to a port_id
	 */
	private Map<Integer, FlowAllocatorInstance> flowAllocatorInstances = null;
	
	/**
	 * Validates allocate requests
	 */
	private AllocateRequestValidator allocateRequestValidator = null;
	
	/**
	 * The directory forwarding table. It will be in another place maybe (RIB Daemon),
	 * but meanwhile it is here.
	 */
	private DirectoryForwardingTable directoryForwardingTable = null;
	
	public FlowAllocatorImpl(){
		allocateRequestValidator = new AllocateRequestValidator();
		flowAllocatorInstances = new HashMap<Integer, FlowAllocatorInstance>();
		directoryForwardingTable = new DirectoryForwardingTableImpl();
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
	
	public DirectoryForwardingTable getDirectoryForwardingTable(){
		return directoryForwardingTable;
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
		ApplicationProcessNamingInfo apNamingInfo = new ApplicationProcessNamingInfo(
				cdapMessage.getDestApName(), cdapMessage.getDestApInst(), cdapMessage.getDestAEName(), cdapMessage.getDestAEInst());
		byte[] address = directoryForwardingTable.getAddress(apNamingInfo);
		
		if (address == null){
			//error, the table should have at least returned a default IPC process address to continue looking for the application process
			log.error("The directory forwarding table returned no entries when looking up " + apNamingInfo.toString());
		}else{
			if (address.equals(ipcProcess.getCurrentSynonym())){
				//TODO there is an entry and the address is this IPC Process, create a FAI, extract the Flow object from the CDAP message and
				//call the FAI
			}else{
				//The address is not this IPC process, forward the CDAP message to that address increment the hop count of the Flow object
				//extract the flow object from the CDAP message
				Flow flow = null;
				
				try{
					flow = (Flow) ipcProcess.getSerializer().deserialize(cdapMessage.getObjValue().getByteval(), Flow.class.toString());
				}catch (Exception ex){
					//Error that has to be fixed, we cannot continue, log it and return
					log.error("Fatal error when deserializing a Flow object. " +ex.getMessage());
					return;
				}
				
				flow.setHopCount(flow.getHopCount() - 1);
				if (flow.getHopCount()  == 0){
					//TODO send negative create Flow response CDAP message to the source IPC process, specifying that the application process
					//could not be found before the hop count expired
				}
				
				ObjectValue objectValue = new ObjectValue();
				
				try{
					objectValue.setByteval(ipcProcess.getSerializer().serialize(flow));
				}catch(Exception ex){
					//Error that has to be fixed, we cannot continue, log it and return
					log.error("Fatal error when serializing a Flow object. " +ex.getMessage());
					return;
				}

				cdapMessage.setObjValue(objectValue);
				byte[] serializedCDAPMesasge = null;
				try{
					serializedCDAPMesasge = ipcProcess.getCDAPSessionFactory().serializeCDAPMessage(cdapMessage);
				}catch(CDAPException ex){
					//Error that has to be fixed, we cannot continue, log it and return
					log.error("Problems serializing CDAP message to be forwarded: " +ex.getMessage() + 
					". As a consequence, the CDAP message won't be forwarded");
				}
				//TODO ipcProcess.getRmt().sendCDAPMessage(address, serializedCDAPMesasge);
			}
		}
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
		
		FlowAllocatorInstance flowAllocatorInstance = new FlowAllocatorInstanceImpl(this.ipcProcess, portId, directoryForwardingTable);
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