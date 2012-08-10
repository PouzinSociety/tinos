package rina.shimipcprocess.ip;

import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.delimiting.api.BaseSocketReader;
import rina.delimiting.api.Delimiter;
import rina.ipcmanager.api.IPCManager;
import rina.ipcservice.api.IPCException;
import rina.shimipcprocess.ip.flowallocator.FlowAllocatorImpl;

public class TCPSocketReader extends BaseSocketReader{

	private static final Log log = LogFactory.getLog(TCPSocketReader.class);
	private IPCManager ipcManager = null;
	private int portId = -1;
	private FlowAllocatorImpl flowAllocator = null;
	
	public TCPSocketReader(Socket socket, Delimiter delimiter, IPCManager ipcManager, int portId, FlowAllocatorImpl flowAllocator) {
		super(socket, delimiter);
		this.ipcManager = ipcManager;
		this.portId = portId;
		this.flowAllocator = flowAllocator;
	}

	@Override
	public void processPDU(byte[] sdu) {
		try {
			this.ipcManager.getIncomingFlowQueue(this.portId).writeDataToQueue(sdu);
		} catch (IPCException e) {
			log.error(e);
		}
		
	}

	@Override
	public void socketDisconnected() {
		flowAllocator.socketClosed(this.portId);
	}

}
