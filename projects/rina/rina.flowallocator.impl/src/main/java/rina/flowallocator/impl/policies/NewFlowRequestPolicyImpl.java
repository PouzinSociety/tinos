package rina.flowallocator.impl.policies;

import java.util.ArrayList;
import java.util.List;

import rina.flowallocator.api.ConnectionId;
import rina.flowallocator.api.Flow;
import rina.flowallocator.api.Flow.State;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCException;

public class NewFlowRequestPolicyImpl implements NewFlowRequestPolicy{

	public Flow generateFlowObject(FlowService flowService) throws IPCException {
		Flow flow = new Flow();
		flow.setDestinationNamingInfo(flowService.getDestinationAPNamingInfo());
		flow.setSourceNamingInfo(flowService.getSourceAPNamingInfo());
		flow.setHopCount(3);
		flow.setMaxCreateFlowRetries(1);
		flow.setSource(true);
		flow.setState(State.ALLOCATION_IN_PROGRESS);
		List<ConnectionId> connectionIds = new ArrayList<ConnectionId>();
		ConnectionId connectionId = new ConnectionId();
		connectionId.setQosId(0x02);
		connectionIds.add(connectionId);
		connectionId = new ConnectionId();
		connectionId.setQosId((byte)1);
		connectionIds.add(connectionId);
		flow.setConnectionIds(connectionIds);
		
		return flow;
	}

}
