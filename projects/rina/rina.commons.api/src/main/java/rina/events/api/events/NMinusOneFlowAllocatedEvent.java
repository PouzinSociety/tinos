package rina.events.api.events;

import rina.events.api.BaseEvent;
import rina.events.api.Event;
import rina.ipcservice.api.FlowService;

/**
 * Event that signals the deallocation of an 
 * N-1 Flow.
 * @author eduardgrasa
 *
 */
public class NMinusOneFlowAllocatedEvent extends BaseEvent{

	/** The portId of the allocated flow **/
	private int portId = 0;
	
	/** The FlowService object describing the flow **/
	private FlowService flowService = null;
	
	public NMinusOneFlowAllocatedEvent(int portId, FlowService flowService) {
		super(Event.N_MINUS_1_FLOW_ALLOCATED);
		this.portId = portId;
		this.flowService = flowService;
	}

	public int getPortId() {
		return this.portId;
	}
	
	public FlowService getFlowService() {
		return this.flowService;
	}
	
	@Override
	public String toString(){
		String result = "Event id: "+this.getId()+" \n";
		result = result + "Port id: "+this.getPortId() + "\n";
		result = result + "Flow description: " + this.getFlowService() + "\n";
		
		return result;
	}

}
