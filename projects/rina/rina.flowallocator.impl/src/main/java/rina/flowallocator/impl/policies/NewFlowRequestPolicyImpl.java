package rina.flowallocator.impl.policies;

import rina.flowallocator.api.message.Flow;
import rina.ipcservice.api.AllocateRequest;
import rina.ipcservice.api.IPCException;

public class NewFlowRequestPolicyImpl implements NewFlowRequestPolicy{

	public Flow generateFlowObject(AllocateRequest allocateRequest, int portId) throws IPCException {
		Flow flow = new Flow();
		flow.setDestinationNamingInfo(allocateRequest.getRequestedAPinfo());
		//flow.setSourcePortId();
		
		return flow;
	}

}
