package rina.ipcservice.api;

/** 
 * Implements the IPC API for the part of the requesting application
 */

public interface APService {
	
	/**
	 * This primitive is invoked by the IPC process to the IPC Manager
	 * to indicate the success or failure of the request associated with this port-id. 
	 * @param requestedAPinfo
	 * @param port_id -1 if error, portId otherwise
	 * @param result errorCode if result > 0, ok otherwise
	 * @param resultReason null if no error, error description otherwise
	 */
	public void deliverAllocateResponse(ApplicationProcessNamingInfo requestedAPinfo, int port_id, int result, String resultReason) ;

	/**
	 * Invoked when in the Transfer state to deliver an SDU on this port-id
	 * @param port_id
	 * @param sdu
	 * @return result
	 */
	public void deliverTransfer(int port_id, byte[] sdu, boolean result);
	
	/**
	 * Invoked in any state by an AAEI to notify the local application process that the release 
	 * of all the resources allocated to this instance are released 
	 * @param portId
	 * @param result
	 * @param resultReason
	 */
	public void deliverDeallocateResponse(int portId, int result, String resultReason);
	
	/**
	 * This primitive is invoked in response to a sumbitStatus to report the current status of 
	 * an allocation-instance
	 * @param port_id
	 * @param result
	 */
	public void deliverStatus(int port_id, boolean result);
	
	/**
	 * Invoked when a Create_Request primitive is received at the requested IPC process
	 * @param request
	 */
	public void deliverAllocateRequest(FlowService request) ;
}