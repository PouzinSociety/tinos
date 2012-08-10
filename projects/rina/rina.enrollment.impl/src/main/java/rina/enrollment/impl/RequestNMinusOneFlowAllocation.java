package rina.enrollment.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.configuration.DIFConfiguration;
import rina.configuration.NMinusOneFlowsConfiguration;
import rina.enrollment.api.Neighbor;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.QualityOfServiceSpecification;
import rina.resourceallocator.api.NMinus1FlowManager;

public class RequestNMinusOneFlowAllocation extends TimerTask{

	private Neighbor dafMember = null;
	private ApplicationProcessNamingInfo myNamingInfo = null;
	private NMinus1FlowManager nMinus1FlowManager = null;
	private DIFConfiguration difConfiguration = null;

	public RequestNMinusOneFlowAllocation(Neighbor dafMember, ApplicationProcessNamingInfo myNamingInfo, 
			NMinus1FlowManager nMinus1FlowManager, DIFConfiguration difConfiguration){
		this.dafMember = dafMember;
		this.myNamingInfo = myNamingInfo;
		this.nMinus1FlowManager = nMinus1FlowManager;
		this.difConfiguration = difConfiguration;
	}

	@Override
	public void run() {
		List<Integer> qosIds = new ArrayList<Integer>();

		if (difConfiguration != null){
			NMinusOneFlowsConfiguration nMinusOneFlowConfiguration = difConfiguration.getnMinusOneFlowsConfiguration();
			if (nMinusOneFlowConfiguration != null){
				qosIds = nMinusOneFlowConfiguration.getDataFlowsQoSIds();
			}
		}

		if (qosIds.size() == 0){
			qosIds.add(new Integer(2));
		}

		//Request the allocation of a number of N-1 flows to be used by data transfer
		QualityOfServiceSpecification qosSpec = null;
		FlowService flowService = null;
		ApplicationProcessNamingInfo neighbourNamingInfo = new ApplicationProcessNamingInfo(dafMember.getApplicationProcessName(), 
				dafMember.getApplicationProcessInstance());
		for(int i=0; i<qosIds.size(); i++){
			qosSpec = new QualityOfServiceSpecification();
			qosSpec.setQosCubeId(qosIds.get(i));
			switch(qosSpec.getQosCubeId()){
			case 1:
				qosSpec.setMaxAllowableGapSDU(-1);
				qosSpec.setPartialDelivery(true);
				qosSpec.setOrder(false);
				break;
			case 2:
				qosSpec.setMaxAllowableGapSDU(0);
				qosSpec.setPartialDelivery(false);
				qosSpec.setOrder(true);
			}

			flowService = new FlowService();
			flowService.setDestinationAPNamingInfo(neighbourNamingInfo);
			flowService.setSourceAPNamingInfo(myNamingInfo);
			flowService.setQoSSpecification(qosSpec);
			nMinus1FlowManager.allocateNMinus1Flow(flowService, false);
		}
	}

}
