package rina.efcp.impl.events;

public class IncomingNPortQueueReadyToBeWrittenEvent implements EFCPEvent{
	
	private int portId = 0;

	public IncomingNPortQueueReadyToBeWrittenEvent(int portId){
		this.portId = portId;
	}
	
	public byte getId() {
		return EFCPEvent.INCOMING_N_PORT_QUEUE_READY_TO_BE_WRITTEN_EVENT;
	}

	public int getPortId() {
		return portId;
	}
}
