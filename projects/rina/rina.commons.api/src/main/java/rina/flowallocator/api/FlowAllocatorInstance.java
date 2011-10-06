package rina.flowallocator.api;

import rina.flowallocator.api.message.Flow;
import rina.ipcservice.api.AllocateRequest;
import rina.ipcservice.api.IPCException;

/**
 * The interface between the FA and the FAI
 * @author elenitrouva
 *
 */
public interface FlowAllocatorInstance{

	/**
	 * Called by the FA to forward an Allocate request to a FAI
	 * @param request
	 * @param portId the local port Id associated to this flow
	 */
	public void submitAllocateRequest(AllocateRequest request, int portId) throws IPCException;
	
	/**
	 * Called by the Flow Allocator when an M_CREATE CDAP PDU with a Flow object 
	 * is received by the Flow Allocator
	 * @param flow
	 */
	public void createFlowRequestMessageReceived(Flow flow, int portId);
	
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
	public void submitAllocateResponse(int portId, boolean success);
	
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
	public void createFlowResponseMessageReceived(Flow flow, boolean success, String reason);
	
	/**
	 * When a deallocate primitive is invoked, it is passed to the FAI responsible for that port-id.  
	 * The FAI sends a Delete_Request referencing the destination port-id, deletes the local 
	 * binding between the Application and the DTP-instance and waits for a response.  (Note that 
	 * the DTP and DTCP if it exists will be deleted automatically after 2MPL)
	 * @param portId
	 */
	public void submitDeallocate(int portId);
	
	/**
	 * When this PDU is received by the FAI with this port-id, the FAI invokes a Deallocate.deliver to notify the local Application, 
	 * deletes the binding between the Application and the local DTP-instance, and sends a Delete_Response indicating the result.
	 */
	public void deleteFlowRequestMessageReceived();
	
	/**
	 * When a Delete_Response PDU is received, the FAI completes any housekeeping and terminates.
	 */
	public void deleteFlowResponseMessageReceived();
}