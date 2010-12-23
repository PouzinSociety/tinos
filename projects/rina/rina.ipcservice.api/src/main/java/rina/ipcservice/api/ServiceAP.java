package rina.ipcservice.api;

/** 
 * Implements the IPC API for the part of the requesting application
 */

public interface ServiceAP {
	

	
	/**
	 * This primitive is invoked by the IPC process to the requested Application Process
	 * to indicate the success or failure of the request associated with this port-id. 
	 * @param requestedAPinfo
	 * @param port_id
	 * @param result
	 */
	void deliverAllocateResponse(ApplicationProcessNamingInfo requestedAPinfo, int port_id, boolean result, String resultReason) ;

	
	/**
	 * Invoked when in the Transfer state to deliver an SDU on this port-id
	 * @param port_id
	 * @param sdu
	 * @param result
	 */
	void deliverTransfer(int port_id, byte[] sdu, boolean result);
	
	
	
	/**
	 * Invoked in any state by an AAEI to notify the local application process that the release 
	 * of all the resources allocated to this instance are released 
	 * @param port_id
	 * @param result
	 */
	void deliverDeallocate(int port_id, boolean result);
	
	
	/**
	 * This primitive is invoked in response to a sumbitStatus to report the current status of 
	 * an allocation-instance
	 * @param port_id
	 * @param result
	 */
	void deliverStatus(int port_id, boolean result);
	
	
	
	/**
	 * Invoked when a Create_Request primitive is received at the requested IPC process
	 * @param request
	 */
	void deliverAllocateRequest(AllocateRequest request) ;
	

	
}
