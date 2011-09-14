package rina.flowallocator.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.ApplicationProcessNameSynonym;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.ObjectValue;
import rina.encoding.api.BaseEncoder;
import rina.encoding.api.Encoder;
import rina.flowallocator.api.BaseFlowAllocator;
import rina.flowallocator.api.DirectoryForwardingTable;
import rina.flowallocator.api.FlowAllocatorInstance;
import rina.flowallocator.api.message.Flow;
import rina.flowallocator.impl.ribobjects.QoSCubesSetRIBObject;
import rina.flowallocator.impl.validation.AllocateRequestValidator;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.AllocateRequest;
import rina.ipcservice.api.IPCException;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;

/** 
 * Implements the Flow Allocator
 */
public class FlowAllocatorImpl extends BaseFlowAllocator{
	
	private static final Log log = LogFactory.getLog(FlowAllocatorImpl.class);

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
	
	/**
	 * The RIB Daemon
	 */
	private RIBDaemon ribDaemon = null;
	
	/**
	 * The Encoder
	 */
	private Encoder encoder = null;
	
	public FlowAllocatorImpl(){
		allocateRequestValidator = new AllocateRequestValidator();
		flowAllocatorInstances = new HashMap<Integer, FlowAllocatorInstance>();
		directoryForwardingTable = new DirectoryForwardingTableImpl();
	}
	
	@Override
	public void setIPCProcess(IPCProcess ipcProcess){
		super.setIPCProcess(ipcProcess);
		this.ribDaemon = (RIBDaemon) getIPCProcess().getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		this.encoder = (Encoder) getIPCProcess().getIPCProcessComponent(BaseEncoder.getComponentName());
		populateRIB(ipcProcess);
	}
	
	private void populateRIB(IPCProcess ipcProcess){
		try{
			RIBObject ribObject = new QoSCubesSetRIBObject(ipcProcess);
			ribDaemon.addRIBObject(ribObject);
		}catch(RIBDaemonException ex){
			ex.printStackTrace();
			log.error("Could not subscribe to RIB Daemon:" +ex.getMessage());
		}
	}

	public DirectoryForwardingTable getDirectoryForwardingTable(){
		return directoryForwardingTable;
	}
	
	/**
	 * Invoked by the RIB Daemon when it has a CDAP message for the flow allocator
	 */
	public void messageReceived(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) {
		switch (cdapMessage.getOpCode()){
		case M_CREATE:
			//received a create flow request from another IPC process, we have to process it
			//and deliver an M_CREATE_R
			//createFlowRequestMessageReceived(cdapMessage);
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
	public void createFlowRequestMessageReceived(CDAPMessage cdapMessage, int portId){
		Flow flow = null;
		byte[] myAddress = null;
		
		try{
			flow = (Flow) encoder.decode(cdapMessage.getObjValue().getByteval(), Flow.class.toString());
		}catch (Exception ex){
			//Error that has to be fixed, we cannot continue, log it and return
			log.error("Fatal error when deserializing a Flow object. " +ex.getMessage());
			return;
		}
		
		byte[] address = directoryForwardingTable.getAddress(flow.getDestinationNamingInfo());
		try{
			myAddress = ((ApplicationProcessNameSynonym) ribDaemon.read(null, RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.CURRENT_SYNONYM, 0)).getSynonym();
		}catch(RIBDaemonException ex){
			log.error(ex);
			//TODO
		}
		
		if (address == null){
			//error, the table should have at least returned a default IPC process address to continue looking for the application process
			log.error("The directory forwarding table returned no entries when looking up " + flow.getDestinationNamingInfo().toString());
		}else{
			if (address.equals(myAddress)){
				//TODO there is an entry and the address is this IPC Process, create a FAI, extract the Flow object from the CDAP message and
				//call the FAI
			}else{
				//The address is not this IPC process, forward the CDAP message to that address increment the hop count of the Flow object
				//extract the flow object from the CDAP message
				flow.setHopCount(flow.getHopCount() - 1);
				if (flow.getHopCount()  == 0){
					//TODO send negative create Flow response CDAP message to the source IPC process, specifying that the application process
					//could not be found before the hop count expired
				}
				
				ObjectValue objectValue = new ObjectValue();
				
				try{
					objectValue.setByteval(encoder.encode(flow));
				}catch(Exception ex){
					//Error that has to be fixed, we cannot continue, log it and return
					log.error("Fatal error when serializing a Flow object. " +ex.getMessage());
					return;
				}
				//TODO
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
		
		FlowAllocatorInstance flowAllocatorInstance = new FlowAllocatorInstanceImpl(this.getIPCProcess(), portId, directoryForwardingTable);
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