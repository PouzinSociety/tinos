package rina.rmt.impl.tcp;

import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.delimiting.api.BaseSocketReader;
import rina.delimiting.api.Delimiter;
import rina.ribdaemon.api.RIBDaemon;

/**
 * Reads a TCP socket, and gets delimited messages out of it.
 * Then it calls the RIB Daemon and delivers the message to it
 * @author eduardgrasa
 *
 */
public class TCPSocketReader extends BaseSocketReader{
	
	private static final Log log = LogFactory.getLog(TCPSocketReader.class);
	
	private TCPRMTImpl rmt = null;
	
	private RIBDaemon ribdaemon = null;
	
	private int portId = 0;

	public TCPSocketReader(Socket socket, int portId, RIBDaemon ribdaemon, Delimiter delimiter, TCPRMTImpl rmt){
		super(socket, delimiter);
		this.portId = portId;
		this.ribdaemon = ribdaemon;
		this.rmt = rmt;
	}
	
	/**
	 * process the pdu that has been found
	 * @param pdu
	 */
	public void processPDU(byte[] pdu){
		log.debug("Passing the PDU to the RIB Daemon");
		ribdaemon.cdapMessageDelivered(pdu, portId);
	}
	
	/**
	 * Invoked when the socket is disconnected
	 */
	public void socketDisconnected(){
		log.debug("Notifying the RMT and the RIB Daemon");
		this.rmt.connectionEnded(portId);
		this.ribdaemon.flowDeallocated(portId);
	}
}
