package rina.flowallocator.impl;

import rina.cdap.api.message.CDAPMessage;
import rina.flowallocator.api.Connection;
import rina.flowallocator.api.FlowAllocatorInstanceService;
import rina.ipcservice.api.AllocateRequest;

public class FlowAllocatorInstance implements FlowAllocatorInstanceService
{
	/**
	 * The allocate request that this flow allocator has to try to fulfill
	 */
	private AllocateRequest allocateRequest = null;
	
	private Connection connection = null;

	public FlowAllocatorInstance(AllocateRequest allocateRequest){
		this.allocateRequest = allocateRequest;
	}
	
	private void initializeConnection(){
		connection = new Connection();
		connection.setDestinationNamingInfo(allocateRequest.getRequestedAPinfo());
		//TODO finish this
	}
	
	public void forwardAllocateRequest(AllocateRequest request) {
		//createDataTransferAEInstance(connection);
	}

	public void forwardDeallocate(int portId) {
		// TODO Auto-generated method stub
		
	}
	
	public void receivedCreateFlowRequest(CDAPMessage message) {
		// TODO Auto-generated method stub
		
	}

}
