package rina.efcp.impl.events;

public class OutgoingEFCPQueueReadyToBeWrittenEvent implements EFCPEvent{

	private int connectionId = 0;
	
	public OutgoingEFCPQueueReadyToBeWrittenEvent(int connectionId){
		this.connectionId = connectionId;
	}
	
	public byte getId() {
		return EFCPEvent.OUTGOING_EFCP_QUEUE_READY_TO_BE_WRITTEN_EVENT;
	}

	public int getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(int connectionId) {
		this.connectionId = connectionId;
	}
}
