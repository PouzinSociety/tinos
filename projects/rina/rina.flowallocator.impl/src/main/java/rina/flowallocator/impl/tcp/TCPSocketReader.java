package rina.flowallocator.impl.tcp;

import java.net.Socket;

import rina.delimiting.api.Delimiter;
import rina.delimiting.api.ZeroLengthSDUBaseSocketReader;
import rina.efcp.api.DataTransferAE;
import rina.flowallocator.api.FlowAllocatorInstance;

/**
 * Continuously reads data from a socket. When an amount of data has been received
 * (either delimited or just an undelimited amount of bytes, depending on the 
 * operation mode) it delivers them to TBD
 * @author eduardgrasa
 *
 */
public class TCPSocketReader extends ZeroLengthSDUBaseSocketReader{
	
	/**
	 * A reference to the Flow Allocator instance that owns this flow
	 */
	private FlowAllocatorInstance flowAllocatorInstance = null;
	
	/**
	 * The class that interacts with the local application
	 */
	private DataTransferAE dataTransferAE = null;
	
	public TCPSocketReader(Socket socket, Delimiter delimiter, DataTransferAE dataTransferAE, 
			FlowAllocatorInstance flowAllocatorInstance){
		super(socket, delimiter);
		this.dataTransferAE = dataTransferAE;
		this.flowAllocatorInstance = flowAllocatorInstance;
	}

	@Override
	public void processPDU(byte[] pdu) {
		this.dataTransferAE.pduDelivered(pdu);
	}

	@Override
	public void socketDisconnected() {
		this.flowAllocatorInstance.socketClosed();
	}

	@Override
	public void process0LengthSDU() {
		this.flowAllocatorInstance.lastSDUReceived();
	}
}
