package rina.efcp.impl.events;

public class CreditExtendedEvent implements EFCPEvent{
	
	private int portId = 0;

	public CreditExtendedEvent(int portId){
		this.portId = portId;
	}
	
	public byte getId() {
		return EFCPEvent.CREDIT_EXTENDED_EVENT;
	}

	public int getPortId() {
		return portId;
	}
}
