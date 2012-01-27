package rina.ipcservice.api;


/** 
 * Implements the IPC API for the part of the requested application
 */

public interface IPCService {
	
	/**
	 * This primitive is invoked by an Application Process to request the allocation of 
	 * IPC resources with the destination application.
	 * @param request the characteristics of the requested flow
	 * @param apService the application process, required to deliver the response as a callback
	 * @return int the portId for the flow allocation
	 * @throw IPCException if the allocate request is not well formed or there are 
	 * no resources to honour the request
	 */
	public int submitAllocateRequest(FlowService request, APService apService) throws IPCException;
	
	/**
	 * This primitive is invoked by the Application Process when in the Transfer state to 
	 * send an SDU on the specified port-id.
	 * @param port_id
	 * @param sdu
	 * @throws IPCException
	 */
	public void submitTransfer(int port_id, byte[] sdu) throws IPCException;
	
	/**
	 * This primitive is invoked by the Application Process in any state to deallocate the 
	 * resources allocated to this instance. 
	 * @param port_id
	 */
	public void submitDeallocateRequest(int portId, APService applicationProcess);
	
	/**
	 * This primitive is invoked at any time by the Application any time it wishes to 
	 * obtain a status on the flow.
	 * @param port_id
	 */
	public void submitStatus(int port_id);
	
	/**
	 * This primitive is invoked by the requested Application Process to respond to an allocation 
	 * request from IPC. 
	 * @param port_id
	 * @param result
	 * @param reason
	 */
	public void submitAllocateResponse(int portId, boolean result, String reason) throws IPCException;
	
	/**
	 * This primitive is invoked by the requested Application Process to respond to a deallocation 
	 * request from IPC. 
	 * @param port_id
	 * @param result
	 * @param reason
	 */
	public void submitDeallocateResponse(int portId, boolean result, String reason) throws IPCException;
	
	/**
	 * Used by an application process to specify that it is available through this IPC process
	 * @param applicationProcessNamingInfo
	 */
	public void register(ApplicationProcessNamingInfo applicationProcessNamingInfo);
	
	/**
	 * Used by an application process to specify that it is no longer available through this IPC process
	 * @param applicationProcessNamingInfo
	 */
	public void unregister(ApplicationProcessNamingInfo applicationProcessNamingInfo);
}