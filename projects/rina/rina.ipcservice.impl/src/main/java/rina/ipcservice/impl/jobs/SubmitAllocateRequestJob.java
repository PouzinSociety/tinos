package rina.ipcservice.impl.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.flowallocator.api.FlowAllocator;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.FlowService;

public class SubmitAllocateRequestJob implements Runnable {
	private static final Log log = LogFactory.getLog(SubmitAllocateRequestJob.class);
	
	FlowAllocator flowAllocator = null;
	APService applicationProcess = null;
	FlowService flowService = null;
	
	public SubmitAllocateRequestJob(FlowService flowService, FlowAllocator flowAllocator, APService applicationProcess){
		this.flowAllocator = flowAllocator;
		this.applicationProcess = applicationProcess;
		this.flowService = flowService;
	}

	public void run() {
		log.debug("Executing..." + flowService.toString());
		flowAllocator.submitAllocateRequest(flowService, applicationProcess);
	}

}
