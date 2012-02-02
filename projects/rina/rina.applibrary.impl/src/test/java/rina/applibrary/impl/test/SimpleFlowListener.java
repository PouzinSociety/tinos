package rina.applibrary.impl.test;

import rina.applibrary.api.Flow;
import rina.applibrary.api.FlowListener;
import rina.applibrary.api.SDUListener;

public class SimpleFlowListener implements FlowListener{

	private Flow flow = null;
	private SDUListener sduListener = null;
	
	public SimpleFlowListener(SDUListener sduListener){
		this.sduListener = sduListener;
	}
	
	public Flow getFlow(){
		return flow;
	}
	
	public void flowAccepted(Flow flow) {
		flow.setSDUListener(sduListener);
		this.flow = flow;
		System.out.println("Flow accepted! PortId: "+flow.getPortId());
	}

}
