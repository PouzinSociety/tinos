package rina.efcp.impl.events;

public class SDUDeliveredFromNPortEvent implements EFCPEvent{
	
	private int portId = 0;

	public SDUDeliveredFromNPortEvent(int portId){
		this.portId = portId;
	}
	
	public byte getId() {
		return EFCPEvent.SDU_DELIVERED_FROM_N_PORT;
	}

	public int getPortId() {
		return portId;
	}
}
