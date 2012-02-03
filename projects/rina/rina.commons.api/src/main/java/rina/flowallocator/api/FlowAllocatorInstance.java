package rina.flowallocator.api;

import java.net.Socket;

import rina.cdap.api.message.CDAPMessage;
import rina.flowallocator.api.message.Flow;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCException;

/**
 * The interface between the FA and the FAI
 * @author elenitrouva
 *
 */
public interface FlowAllocatorInstance{
	
	/**
	 * Returns the portId associated to this Flow Allocator Instance
	 * @return
	 */
	public int getPortId();
	
	/**
	 * Return the Flow object associated to this Flow Allocator Instance
	 * @return
	 */
	public Flow getFlow();
	
	
	/**
	 * Set the socket associated to this Flow Allocator Instance, will make the 
	 * Flow Allocation process continue
	 * @param Socket
	 */
	public void setSocket(Socket socket);
	
	/**
	 * When the TCP Socket Reader detects that the socket is closed, 
	 * it will notify the Flow Allocator instance
	 */
	public void socketClosed();

	/**
	 * Called by the FA to forward an Allocate request to a FAI
	 * @param request
	 * @param portId the local port Id associated to this flow
	 */
	public void submitAllocateRequest(FlowService request) throws IPCException;
	
	/**
	 * Called by the Flow Allocator when an M_CREATE CDAP PDU with a Flow object 
	 * is received by the Flow Allocator
	 * @param flow
	 * @param portId the destination portid as decided by the Flow allocator
	 * @param requestMessate the CDAP request message
	 * @param underlyingPortId the port id to reply later on
	 */
	public void createFlowRequestMessageReceived(Flow flow, int portId, CDAPMessage requestMessage, int underlyingPortId);
	
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
	 * @param reason
	 */
	public void submitAllocateResponse(int portId, boolean success, String reason);
	
	/**
	 * When a deallocate primitive is invoked, it is passed to the FAI responsible for that port-id.  
	 * The FAI sends an M_DELETE request CDAP PDU on the Flow object referencing the destination port-id, deletes the local 
	 * binding between the Application and the DTP-instance and waits for a response.  (Note that 
	 * the DTP and DTCP if it exists will be deleted automatically after 2MPL)
	 * @param portId
	 * @param applicationProcess
	 */
	public void submitDeallocateRequest(int portId);
	
	/**
	 * When this PDU is received by the FAI with this port-id, the FAI invokes a Deallocate.deliver to notify the local Application, 
	 * deletes the binding between the Application and the local DTP-instance, and sends a Delete_Response indicating the result.
	 */
	public void deleteFlowRequestMessageReceived(CDAPMessage requestMessage, int underlyingPortId);
}