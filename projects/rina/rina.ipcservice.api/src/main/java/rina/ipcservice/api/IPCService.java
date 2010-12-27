package rina.ipcservice.api;


/** 
 * Implements the IPC API for the part of the requested application
 */

public interface IPCService {
	
	/**
	 * This primitive is invoked by an Application Process to request the allocation of 
	 * IPC resources with the destination application.
	 * @param request
	 * @return
	 */
	void submitAllocateRequest(AllocateRequest request) ;
	
	/**
	 * This primitive is invoked by the Application Process when in the Transfer state to 
	 * send an SDU on the specified port-id.
	 * @param port_id
	 * @param sdu
	 * @param result
	 */
	void submitTransfer(int port_id, byte[] sdu, boolean result);
	
	/**
	 * This primitive is invoked by the Application Process in any state to deallocate the 
	 * resources allocated to this instance. 
	 * @param port_id
	 */
	void submitDeallocate(int port_id);
	
	/**
	 * This primitive is invoked at any time by the Application any time it wishes to 
	 * obtain a status on the flow.
	 * @param port_id
	 */
	void submitStatus(int port_id);
	
	/**
	 * This primitive is invoked by the requested Application Process to respond to an allocation 
	 * request from IPC. 
	 * @param port_id
	 * @param result
	 */
	void submitAllocateResponse(int port_id, boolean result) ;
}