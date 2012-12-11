package rina.flowallocator.impl.policies;

import java.util.ArrayList;
import java.util.List;

import rina.configuration.DIFConfiguration;
import rina.configuration.Property;
import rina.configuration.RINAConfiguration;
import rina.efcp.api.EFCPPolicyConstants;
import rina.flowallocator.api.ConnectionId;
import rina.flowallocator.api.Flow;
import rina.flowallocator.api.Flow.State;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCException;

public class NewFlowRequestPolicyImpl implements NewFlowRequestPolicy{

	public Flow generateFlowObject(FlowService flowService, String difName) throws IPCException {
		Flow flow = new Flow();
		flow.setDestinationNamingInfo(flowService.getDestinationAPNamingInfo());
		flow.setSourceNamingInfo(flowService.getSourceAPNamingInfo());
		flow.setHopCount(3);
		flow.setMaxCreateFlowRetries(1);
		flow.setSource(true);
		flow.setState(State.ALLOCATION_IN_PROGRESS);
		List<ConnectionId> connectionIds = new ArrayList<ConnectionId>();
		
		int qosId = 2;
		if (flowService.getQoSSpecification() != null){
			qosId = flowService.getQoSSpecification().getQosCubeId();
		}
		ConnectionId connectionId = new ConnectionId();
		connectionId.setQosId(qosId);
		connectionIds.add(connectionId);
		connectionId = new ConnectionId();
		connectionId.setQosId(qosId);
		connectionIds.add(connectionId);
		flow.setConnectionIds(connectionIds);
		
		/*flow.getPolicies().put(EFCPPolicyConstants.DTCP_FLOW_CONTROL, EFCPPolicyConstants.CREDIT);
		flow.getPolicies().put(EFCPPolicyConstants.DTCP_FLOW_CONTROL_FLOW_CONTROL_OVERRUN_POLICY, EFCPPolicyConstants.DISCARD);
		flow.getPolicies().put(EFCPPolicyConstants.DTCP_FLOW_CONTROL_INITIAL_CREDIT_POLICY, EFCPPolicyConstants.CONSTANT);
		flow.getPolicies().put(EFCPPolicyConstants.DTCP_FLOW_CONTROL_RECEIVING_FLOW_CONTROL_POLICY, EFCPPolicyConstants.EXHAUSTED);
		flow.getPolicies().put(EFCPPolicyConstants.DTCP_FLOW_CONTROL_UPDATE_CREDIT_POLICY, EFCPPolicyConstants.CONSTANT);
		flow.getPolicyParameters().put(EFCPPolicyConstants.DTCP_FLOW_CONTROL_INITIAL_CREDIT_POLICY_CONSTANT_VALUE, "50");
		flow.getPolicyParameters().put(EFCPPolicyConstants.DTCP_FLOW_CONTROL_UPDATE_CREDIT_POLICY_CONSTANT_VALUE, "50");*/
		
		DIFConfiguration difConfiguration = RINAConfiguration.getInstance().getDIFConfiguration(difName);
		List<Property> properties = difConfiguration.getPolicies();
		for(int i=0; i<properties.size(); i++){
			flow.getPolicies().put(properties.get(i).getName(), properties.get(i).getValue());
		}
		
		properties = difConfiguration.getPolicyParameters();
		for(int i=0; i<properties.size(); i++){
			flow.getPolicyParameters().put(properties.get(i).getName(), properties.get(i).getValue());
		}
		
		return flow;
	}

}
