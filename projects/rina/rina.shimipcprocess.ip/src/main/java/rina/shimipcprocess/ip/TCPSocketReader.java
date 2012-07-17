package rina.shimipcprocess.ip;

import java.net.Socket;

import rina.delimiting.api.BaseSocketReader;
import rina.delimiting.api.Delimiter;
import rina.ipcservice.api.APService;
import rina.shimipcprocess.ip.flowallocator.FlowAllocatorImpl;

public class TCPSocketReader extends BaseSocketReader{

	private APService applicationCallback = null;
	private int portId = -1;
	private FlowAllocatorImpl flowAllocator = null;
	
	public TCPSocketReader(Socket socket, Delimiter delimiter, APService applicationCallback, int portId, FlowAllocatorImpl flowAllocator) {
		super(socket, delimiter);
		this.applicationCallback = applicationCallback;
		this.portId = portId;
		this.flowAllocator = flowAllocator;
	}

	@Override
	public void processPDU(byte[] sdu) {
		this.applicationCallback.deliverTransfer(this.portId, sdu);
	}

	@Override
	public void socketDisconnected() {
		flowAllocator.socketClosed(this.portId);
	}

}
