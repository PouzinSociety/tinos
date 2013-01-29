package rina.efcp.impl.events;

public class IPCProcessStoppedEvent implements EFCPEvent{

	public byte getId() {
		return EFCPEvent.IPC_PROCESS_STOPPED_EVENT;
	}

}
