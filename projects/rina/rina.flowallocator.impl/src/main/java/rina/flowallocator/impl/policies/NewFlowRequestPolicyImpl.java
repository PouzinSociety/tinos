package rina.flowallocator.impl.policies;

import rina.flowallocator.api.Flow;
import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCException;

public class NewFlowRequestPolicyImpl implements NewFlowRequestPolicy{

	public Flow generateFlowObject(FlowService flowService) throws IPCException {
		Flow flow = new Flow();
		flow.setDestinationNamingInfo(flowService.getDestinationAPNamingInfo());
		flow.setSourceNamingInfo(flowService.getSourceAPNamingInfo());
		flow.setHopCount(3);
		flow.setMaxCreateFlowRetries(1);
		
		return flow;
	}

}
