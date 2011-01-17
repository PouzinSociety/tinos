package rina.flowallocator.impl;


import rina.cdap.api.message.CDAPMessage;
import rina.ipcservice.api.AllocateRequest;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.IPCService;
import rina.ipcservice.api.QoSCube;


/** 
 * Implements the Flow Allocator
 */

public class FlowAllocator implements IPCService {
	
	private ApplicationProcessNamingInfo requestedAPinfo = null;
	private int portId = 0;
	private QoSCube cube = null;
	private boolean result = false;
	private int FAid = 0;
	
	private boolean validAllocateRequest = false;
	private boolean AllocateNotifyPolicy = false;



	public FlowAllocator() {

	}

	public void submitAllocateRequest(AllocateRequest request) {
		if(validateRequest(request))
		{
			//if translate request into policies ok
			request.setPort_id(assignPortId());
			FlowAllocatorInstance FAI = new FlowAllocatorInstance();
			//subscription could be to the FAI constructor?
			//subscribeToMessages(MessageSubscription messageSubscription, MessageSubscriber messageSubscriber);
			
//			if(AllocateNotifyPolicy)
//				deliverAllocateResponse(request.getRequestedAPinfo(), request.getPort_id(), true, "");
	}
//		else
//			deliverAllocateResponse(request.getRequestedAPinfo(), request.getPort_id(), false, "The format of request was not valid");
	}

	/**
	 * 
	 * Assigns a port-id for the specific AllocateRequest
	 */
	private int assignPortId() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void submitAllocateResponse(int portId, boolean result) {
		// TODO Auto-generated method stub
		
	}

	public void submitDeallocate(int portId) {
		// TODO Auto-generated method stub
		
	}

	
	public void submitStatus(int portId) {
		// TODO Auto-generated method stub
		
	}

	public void submitTransfer(int portId, byte[] sdu, boolean result) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Validates the format of an AllocateRequest
	 * @param request
	 * @return yes if valid, no otherwise
	 */
	public boolean validateRequest(AllocateRequest request){
		
		return validAllocateRequest;
	}

	public void register(
			ApplicationProcessNamingInfo applicationProcessNamingInfo) {
		// TODO Auto-generated method stub
		
	}

	public boolean submitTransfer(int portId, byte[] sdu) {
		// TODO Auto-generated method stub
		return false;
	}

	public void unregister(
			ApplicationProcessNamingInfo applicationProcessNamingInfo) {
		// TODO Auto-generated method stub
		
	}

	

}
