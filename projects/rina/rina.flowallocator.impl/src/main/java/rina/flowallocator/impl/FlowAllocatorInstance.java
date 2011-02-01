package rina.flowallocator.impl;

import rina.cdap.api.message.CDAPMessage;
import rina.flowallocator.api.Connection;
import rina.flowallocator.api.FlowAllocatorInstanceService;
import rina.ipcservice.api.AllocateRequest;

public class FlowAllocatorInstance implements FlowAllocatorInstanceService
{
	private Connection connection;

	public FlowAllocatorInstance(){
		
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
