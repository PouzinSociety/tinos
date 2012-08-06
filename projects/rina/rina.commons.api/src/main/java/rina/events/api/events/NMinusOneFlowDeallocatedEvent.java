package rina.events.api.events;

import rina.events.api.BaseEvent;
import rina.events.api.Event;

/**
 * Event that signals the deallocation of an 
 * N-1 Flow.
 * @author eduardgrasa
 *
 */
public class NMinusOneFlowDeallocatedEvent extends BaseEvent{

	/** The portId of the deallocated flow **/
	private int portId = 0;
	
	public NMinusOneFlowDeallocatedEvent(int portId) {
		super(Event.N_MINUS_1_FLOW_DEALLOCATED);
		this.portId = portId;
	}

	public int getPortId() {
		return this.portId;
	}
	
	@Override
	public String toString(){
		String result = "Event id: "+this.getId()+" \n";
		result = result + "Port id: "+this.getPortId() + "\n";
		
		return result;
	}

}
