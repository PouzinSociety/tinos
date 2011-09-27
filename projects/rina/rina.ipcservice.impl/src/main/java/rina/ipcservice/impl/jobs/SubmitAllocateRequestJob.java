package rina.ipcservice.impl.jobs;

import rina.flowallocator.api.FlowAllocator;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.AllocateRequest;

public class SubmitAllocateRequestJob implements Runnable {
	
	FlowAllocator flowAllocator = null;
	APService applicationProcess = null;
	AllocateRequest allocateRequest = null;
	
	public SubmitAllocateRequestJob(AllocateRequest allocateRequest, FlowAllocator flowAllocator, APService applicationProcess){
		this.flowAllocator = flowAllocator;
		this.applicationProcess = applicationProcess;
		this.allocateRequest = allocateRequest;
	}

	public void run() {
		flowAllocator.submitAllocateRequest(allocateRequest, applicationProcess);
	}

}
