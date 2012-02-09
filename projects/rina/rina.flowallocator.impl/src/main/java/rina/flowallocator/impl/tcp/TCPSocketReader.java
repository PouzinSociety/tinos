package rina.flowallocator.impl.tcp;

import java.net.Socket;

import rina.delimiting.api.BaseSocketReader;
import rina.delimiting.api.Delimiter;
import rina.flowallocator.api.FlowAllocatorInstance;
import rina.ipcservice.api.APService;

/**
 * Continuously reads data from a socket. When an amount of data has been received
 * (either delimited or just an undelimited amount of bytes, depending on the 
 * operation mode) it delivers them to TBD
 * @author eduardgrasa
 *
 */
public class TCPSocketReader extends BaseSocketReader{
	
	/**
	 * A reference to the Flow Allocator instance that owns this flow
	 */
	private FlowAllocatorInstance flowAllocatorInstance = null;
	
	/**
	 * The class that interacts with the local application
	 */
	private APService apService = null;
	
	/**
	 * The portId associated to this flow
	 */
	private int portId = 0;
	
	public TCPSocketReader(Socket socket, Delimiter delimiter, APService apService, int portId){
		super(socket, delimiter);
		this.apService = apService;
		this.portId = portId;
	}

	@Override
	public void processPDU(byte[] sdu) {
		apService.deliverTransfer(portId, sdu);
	}

	@Override
	public void socketDisconnected() {
		flowAllocatorInstance.socketClosed();
	}
}
