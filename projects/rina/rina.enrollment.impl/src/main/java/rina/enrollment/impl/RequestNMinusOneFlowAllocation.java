package rina.enrollment.impl;

import java.util.TimerTask;

import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.enrollment.api.Neighbor;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.QualityOfServiceSpecification;
import rina.resourceallocator.api.NMinus1FlowManager;

public class RequestNMinusOneFlowAllocation extends TimerTask{
	
	private Neighbor dafMember = null;
	private ApplicationProcessNamingInfo myNamingInfo = null;
	private NMinus1FlowManager nMinus1FlowManager = null;
	
	public RequestNMinusOneFlowAllocation(Neighbor dafMember, ApplicationProcessNamingInfo myNamingInfo, 
			NMinus1FlowManager nMinus1FlowManager){
		this.dafMember = dafMember;
		this.myNamingInfo = myNamingInfo;
		this.nMinus1FlowManager = nMinus1FlowManager;
	}

	@Override
	public void run() {
		 //Request the allocation of an N-1 reliable flow to be used by data transfer
		 ApplicationProcessNamingInfo neighbourNamingInfo = new ApplicationProcessNamingInfo(dafMember.getApplicationProcessName(), 
				 dafMember.getApplicationProcessInstance());
		 FlowService flowService = new FlowService();
		 flowService.setDestinationAPNamingInfo(neighbourNamingInfo);
		 flowService.setSourceAPNamingInfo(myNamingInfo);
		 QualityOfServiceSpecification qosSpec = new QualityOfServiceSpecification();
		 qosSpec.setQosCubeId(2);
		 flowService.setQoSSpecification(qosSpec);
		 nMinus1FlowManager.allocateNMinus1Flow(flowService, false);
		
	}

}
