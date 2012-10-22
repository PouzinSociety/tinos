package rina.efcp.impl.events;

public class PDUDeliveredFromRMTEvent implements EFCPEvent{
	
	private long connectionEndpointId = 0;

	public PDUDeliveredFromRMTEvent(long connectionEndpointId){
		this.connectionEndpointId = connectionEndpointId;
	}
	
	public byte getId() {
		return EFCPEvent.PDU_DELIVERED_FROM_RMT;
	}

	public long getConnectionEndpointId() {
		return connectionEndpointId;
	}
}
