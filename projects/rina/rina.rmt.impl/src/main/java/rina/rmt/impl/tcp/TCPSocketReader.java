package rina.rmt.impl.tcp;

import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.CDAPSessionManager;
import rina.delimiting.api.BaseSocketReader;
import rina.delimiting.api.Delimiter;
import rina.events.api.Event;
import rina.events.api.events.NMinusOneFlowDeallocatedEvent;
import rina.ribdaemon.api.RIBDaemon;

/**
 * Reads a TCP socket, and gets delimited messages out of it.
 * Then it calls the RIB Daemon and delivers the message to it
 * @author eduardgrasa
 *
 */
public class TCPSocketReader extends BaseSocketReader{
	
	private static final Log log = LogFactory.getLog(TCPSocketReader.class);
	
	private CDAPSessionManager cdapSessionManager = null;
	private RIBDaemon ribdaemon = null;
	private int portId = 0;
	
	public TCPSocketReader(Socket socket, int portId, RIBDaemon ribdaemon, Delimiter delimiter, CDAPSessionManager cdapSessionManager){
		super(socket, delimiter);
		this.portId = portId;
		this.ribdaemon = ribdaemon;
		this.cdapSessionManager = cdapSessionManager;
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
		CDAPSessionDescriptor cdapSessionDescriptor = this.cdapSessionManager.
			getCDAPSession(this.portId).getSessionDescriptor();
		Event event = new NMinusOneFlowDeallocatedEvent(this.portId, cdapSessionDescriptor);
		log.debug("Notifying the Event Manager about a new event.");
		log.debug(event.toString());
		this.ribdaemon.deliverEvent(event);
	}
}
