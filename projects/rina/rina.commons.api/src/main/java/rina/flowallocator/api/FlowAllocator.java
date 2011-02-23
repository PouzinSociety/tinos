package rina.flowallocator.api;

import rina.ipcprocess.api.IPCProcessComponent;
import rina.ipcservice.api.AllocateRequest;
import rina.ipcservice.api.IPCException;

/**
 * This interface must be implemented by the class that implements the flow allocator
 * @author eduardgrasa
 */
public interface FlowAllocator extends IPCProcessComponent{

	/**
	 * The Flow Allocator is invoked when an Allocate_Request.submit is received.  The source Flow 
	 * Allocator determines if the request is well formed.  If not well-formed, an Allocate_Response.deliver 
	 * is invoked with the appropriate error code.  If the request is well-formed, a new instance of an 
	 * FlowAllocator is created and passed the parameters of this Allocate_Request to handle the allocation. 
	 * It is a matter of DIF policy (AllocateNoificationPolicy) whether an Allocate_Request.deliver is invoked 
	 * with a status of pending, or whether a response is withheld until an Allocate_Response can be delivered 
	 * with a status of success or failure.
	 * @param allocateRequest the characteristics of the flow to be allocated.
	 * @param portId the port id that will be allocated to this flow
	 * @throws IPCException if the request is not well formed or there are not enough resources
	 * to honour the request
	 */
	public void submitAllocateRequest(AllocateRequest allocateRequest, int portId) throws IPCException;
	
	/**
	 * Forward the allocate response to the Flow Allocator Instance.
	 * @param portId the portId associated to the allocate response
	 * @param success successful or unsucessful allocate request
	 */
	public void submitAllocateResponse(int portId, boolean success);
	
	/**
	 * Forward the deallocate request to the Flow Allocator Instance.
	 * @param portId
	 */
	public void submitDeallocate(int portId);
}
