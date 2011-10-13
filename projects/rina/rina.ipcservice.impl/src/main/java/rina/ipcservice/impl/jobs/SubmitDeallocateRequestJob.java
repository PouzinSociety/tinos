package rina.ipcservice.impl.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.flowallocator.api.FlowAllocator;
import rina.ipcservice.api.APService;

public class SubmitDeallocateRequestJob implements Runnable {
	private static final Log log = LogFactory.getLog(SubmitDeallocateRequestJob.class);
	
	FlowAllocator flowAllocator = null;
	APService applicationProcess = null;
	int portId = 0;
	
	public SubmitDeallocateRequestJob(int portId, FlowAllocator flowAllocator, APService applicationProcess){
		this.flowAllocator = flowAllocator;
		this.applicationProcess = applicationProcess;
		this.portId = portId;
	}

	public void run() {
		log.debug("Executing..." + portId);
		flowAllocator.submitDeallocateRequest(portId, applicationProcess);
	}

}
