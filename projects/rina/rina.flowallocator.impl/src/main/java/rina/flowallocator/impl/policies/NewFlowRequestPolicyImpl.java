package rina.flowallocator.impl.policies;

import rina.flowallocator.api.message.Flow;
import rina.ipcservice.api.AllocateRequest;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.IPCException;

public class NewFlowRequestPolicyImpl implements NewFlowRequestPolicy{

	public Flow generateFlowObject(AllocateRequest allocateRequest) throws IPCException {
		Flow flow = new Flow();
		flow.setDestinationNamingInfo(allocateRequest.getDestinationAPNamingInfo());
		flow.setSourceNamingInfo(allocateRequest.getSourceAPNamingInfo());
		flow.setHopCount(3);
		flow.setMaxCreateFlowRetries(1);
		
		return flow;
	}

}
