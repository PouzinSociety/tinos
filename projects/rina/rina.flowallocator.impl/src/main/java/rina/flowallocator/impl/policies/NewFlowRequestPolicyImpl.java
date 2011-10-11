package rina.flowallocator.impl.policies;

import rina.flowallocator.api.message.Flow;
import rina.ipcservice.api.AllocateRequest;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.IPCException;

public class NewFlowRequestPolicyImpl implements NewFlowRequestPolicy{

	public Flow generateFlowObject(AllocateRequest allocateRequest) throws IPCException {
		Flow flow = new Flow();
		flow.setDestinationNamingInfo(allocateRequest.getRequestedAPinfo());
		flow.setSourceNamingInfo(new ApplicationProcessNamingInfo("test", "1")); //change this for the real source application naming information
		flow.setHopCount(3);
		flow.setMaxCreateFlowRetries(1);
		
		return flow;
	}

}
