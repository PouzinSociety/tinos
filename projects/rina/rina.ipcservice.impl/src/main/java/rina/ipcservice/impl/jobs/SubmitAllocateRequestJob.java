package rina.ipcservice.impl.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.flowallocator.api.FlowAllocator;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.AllocateRequest;

public class SubmitAllocateRequestJob implements Runnable {
	private static final Log log = LogFactory.getLog(SubmitAllocateRequestJob.class);
	
	FlowAllocator flowAllocator = null;
	APService applicationProcess = null;
	AllocateRequest allocateRequest = null;
	
	public SubmitAllocateRequestJob(AllocateRequest allocateRequest, FlowAllocator flowAllocator, APService applicationProcess){
		this.flowAllocator = flowAllocator;
		this.applicationProcess = applicationProcess;
		this.allocateRequest = allocateRequest;
	}

	public void run() {
		log.debug("Executing..." + allocateRequest.toString());
		flowAllocator.submitAllocateRequest(allocateRequest, applicationProcess);
	}

}
