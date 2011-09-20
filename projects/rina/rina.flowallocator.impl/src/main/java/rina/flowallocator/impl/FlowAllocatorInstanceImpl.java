package rina.flowallocator.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.efcp.api.DataTransferAE;
import rina.efcp.api.DataTransferAEInstance;
import rina.flowallocator.api.Connection;
import rina.flowallocator.api.DirectoryForwardingTable;
import rina.flowallocator.api.FlowAllocatorInstance;
import rina.flowallocator.api.message.Flow;
import rina.flowallocator.impl.policies.NewFlowRequestPolicy;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.AllocateRequest;
import rina.ipcservice.api.IPCException;

/**
 * A flow allocator instance implementation. Its task is to manage the 
 * lifecycle of an EFCP connection
 * @author eduardgrasa
 *
 */
public class FlowAllocatorInstanceImpl implements FlowAllocatorInstance{
	
	private static final Log log = LogFactory.getLog(FlowAllocatorInstanceImpl.class);
	
	/**
	 * The portId associated to this Flow allocator instance
	 */
	private int portId = 0;
	
	/**
	 * A reference to the wrapping IPC Process
	 */
	private IPCProcess ipcProcess = null;
	
	/**
	 * The new flow request policy, will translate the allocate request into 
	 * a flow object
	 */
	private NewFlowRequestPolicy newFlowRequestPolicy = null;
	
	/**
	 * The current active connection
	 */
	private Connection activeConnection = null;
	
	/**
	 * All the connections associated to this flow
	 */
	private List<Connection> connections = null;
	
	/**
	 * The data transfer AE instance associated to this flow allocator instance
	 */
	private DataTransferAEInstance dataTransferAEInstance = null;
	
	/**
	 * The directory forwarding table
	 */
	private DirectoryForwardingTable directoryForwardingTable = null;
	
	public FlowAllocatorInstanceImpl(IPCProcess ipcProcess, int portId, DirectoryForwardingTable directoryForwardingTable){
		this.ipcProcess = ipcProcess;
		this.portId = portId;
		this.directoryForwardingTable = directoryForwardingTable;
		connections = new ArrayList<Connection>();
		//TODO initialize the newFlowRequestPolicy
	}

	/**
	 * Generate the flow object, create the local DTP and optionally DTCP instances, generate a CDAP 
	 * M_CREATE request with the flow object and send it to the appropriate IPC process (search the 
	 * directory and the directory forwarding table if needed)
	 * @param allocateRequest the allocate request
	 * @param portId the local port id associated to this flow
	 * @throws IPCException if there are not enough resources to fulfill the allocate request
	 */
	public void submitAllocateRequest(AllocateRequest allocateRequest, int portId) throws IPCException {
		Flow flow = newFlowRequestPolicy.generateFlowObject(allocateRequest, portId);
		createDataTransferAEInstance(flow);
		
		//Check directory to see to what IPC process the CDAP M_CREATE request has to be delivered
		long address = directoryForwardingTable.getAddress(allocateRequest.getRequestedAPinfo());
		if (address == 0){
			//error, the table should have at least returned a default IPC process address to continue looking for the application process
			log.error("The directory forwarding table returned no entries when looking up " + allocateRequest.getRequestedAPinfo().toString());
		}
		
		//TODO once the destination IPC process is known, create the CDAP message
		//TODO Now create the EFCP PDU and pass it to the appropriated EFCP instance
	}
	
	/**
	 * Create the local instances of DTP and optionally DTCP
	 * @param flow
	 */
	private void createDataTransferAEInstance(Flow flow){
		Connection connection = new Connection(flow);
		DataTransferAE dataTransferAE = (DataTransferAE) ipcProcess.getIPCProcessComponent(DataTransferAE.class.getName());
		dataTransferAEInstance = dataTransferAE.createDataTransferAEInstance(connection);
		activeConnection = connection;
		connections.add(connection);
	}

	/**
	 * When an FAI is created with a Create_Request(Flow) as input, it will inspect the parameters 
	 * first to determine if the requesting Application (Source_Naming_Info) has access to the requested 
	 * Application (Destination_Naming_Info) by inspecting the Access Control parameter.  If not, a 
	 * negative Create_Response primitive will be returned to the requesting FAI. If it does have access, 
	 * the FAI will determine if the policies proposed are acceptable, invoking the NewFlowRequestPolicy.  
	 * If not, a negative Create_Response is sent.  If they are acceptable, the FAI will invoke a 
	 * Allocate_Request.deliver primitive to notify the requested Application that it has an outstanding 
	 * allocation request.  (If the application is not executing, the FAI will cause the application
	 * to be instantiated.)
	 * @param flow
	 */
	public void createFlowRequestMessageReceived(Flow flow) {
		// TODO implement this
	}

	/**
	 * When the FAI gets a Allocate_Response from the destination application, it formulates a Create_Response 
	 * on the flow object requested.If the response was positive, the FAI will cause DTP and if required DTCP 
	 * instances to be created to support this allocation. A positive Create_Response Flow is sent to the 
	 * requesting FAI with the connection-endpoint-id and other information provided by the destination FAI. 
	 * The Create_Response is sent to requesting FAI with the necessary information reflecting the existing flow, 
	 * or an indication as to why the flow was refused.  
	 * If the response was negative, the FAI does any necessary housekeeping and terminates.
	 * @param portId
	 * @param success
	 */
	public void submitAllocateResponse(int portId, boolean success){
		// TODO implement this
	}
	
	/**
	 * If the response to the allocate request is negative 
	 * the Allocation invokes the AllocateRetryPolicy. If the AllocateRetryPolicy returns a positive result, a new Create_Flow Request 
	 * is sent and the CreateFlowTimer is reset.  Otherwise, if the AllocateRetryPolicy returns a negative result or the MaxCreateRetries has been exceeded, 
	 * an Allocate_Request.deliver primitive to notify the Application that the flow could not be created. (If the reason was 
	 * ÒApplication Not Found,Ó the primitive will be delivered to the Inter-DIF Directory to search elsewhere.The FAI deletes the DTP and DTCP instances 
	 * it created and does any other housekeeping necessary, before terminating.  If the response is positive, it completes the binding of the DTP-instance 
	 * with this connection-endpoint-id to the requesting Application and invokes a Allocate_Request.submit primitive to notify the requesting Application 
	 * that its allocation request has been satisfied.
	 * @param flow
	 * @param success
	 * @param reason
	 */
	public void createFlowResponseMessageReceived(Flow flow, boolean success, String reason){
		//TODO implement this
	}

	/**
	 * When a deallocate primitive is invoked, it is passed to the FAI responsible for that port-id.  
	 * The FAI sends a Delete_Request referencing the destination port-id, deletes the local 
	 * binding between the Application and the DTP-instance and waits for a response.  (Note that 
	 * the DTP and DTCP if it exists will be deleted automatically after 2MPL)
	 * @param portId
	 */
	public void submitDeallocate(int portId){
		// TODO implement this
	}

	/**
	 * When this PDU is received by the FAI with this port-id, the FAI invokes a Deallocate.deliver to notify the local Application, 
	 * deletes the binding between the Application and the local DTP-instance, and sends a Delete_Response indicating the result.
	 */
	public void deleteFlowRequestMessageReceived(){
		DataTransferAE dataTransferAE = (DataTransferAE) ipcProcess.getIPCProcessComponent(DataTransferAE.class.getName());
		dataTransferAE.destroyDataTransferAEInstance(activeConnection);
		ipcProcess.deliverDeallocateRequestToApplicationProcess(portId);
		//TODO create and send delete flow response message
	}

	/**
	 * When a Delete_Response PDU is received, the FAI completes any housekeeping and terminates.
	 */
	public void deleteFlowResponseMessageReceived(){
		// TODO implement this
	}
}
