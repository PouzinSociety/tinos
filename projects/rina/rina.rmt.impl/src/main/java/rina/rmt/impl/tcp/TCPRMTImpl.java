package rina.rmt.impl.tcp;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.BaseCDAPSessionManager;
import rina.cdap.api.CDAPSessionManager;
import rina.configuration.KnownIPCProcessConfiguration;
import rina.configuration.RINAConfiguration;
import rina.delimiting.api.BaseDelimiter;
import rina.delimiting.api.Delimiter;
import rina.events.api.Event;
import rina.events.api.EventListener;
import rina.events.api.events.NMinusOneFlowDeallocatedEvent;
import rina.events.api.events.NeighborDeclaredDeadEvent;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.IPCException;
import rina.ipcservice.api.QualityOfServiceSpecification;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemon;
import rina.rmt.api.BaseRMT;

/**
 * Specifies the interface of the Relaying and Multiplexing task. Mediates the access to one or more (N-1) DIFs 
 * or physical media
 * @author eduardgrasa
 */
public class TCPRMTImpl extends BaseRMT implements EventListener{
	private static final Log log = LogFactory.getLog(TCPRMTImpl.class);
	
	/**
	 * Contains the open TCP flows to other IPC processes, indexed by portId
	 */
	private Map<Integer, Socket> flowTable = null;
	
	/**
	 * The server that will listen for incoming connections to this RMT
	 */
	private RMTServer rmtServer = null;
	
	/**
	 * The lock to get before writing data to the socket
	 */
	private Object socketWriteLock = null;
	
	public TCPRMTImpl(){
		this.flowTable = new ConcurrentHashMap<Integer, Socket>();
		this.socketWriteLock = new Object();
	}
	
	@Override
	public void setIPCProcess(IPCProcess ipcProcess){
		super.setIPCProcess(ipcProcess);
		
		//Start RMT Server
		this.rmtServer = new RMTServer(this);
		
		//Subscribe to N-1 Flow deallocated events
		RIBDaemon ribDaemon = (RIBDaemon) this.getIPCProcess().getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		ribDaemon.subscribeToEvent(Event.N_MINUS_1_FLOW_DEALLOCATED, this);
		ribDaemon.subscribeToEvent(Event.NEIGHBOR_DECLARED_DEAD, this);
	}
	
	/**
	 * Start listening for connections from remote IPC Processes
	 */
	public void startListening(){
		if (rmtServer.isListening()){
			return;
		}
		
		this.getIPCProcess().execute(rmtServer);
	}
	
	/**
	 * Returns the IP address of the IPC process identified by the tuple ipcProcessName, ipcProcessInstance.
	 * It will read the configuration file every time this operation is called (to get updates)
	 * @param ipcProcessName
	 * @param ipcProcessInstance
	 * @return
	 */
	public String getIPAddressFromApplicationNamingInformation(String ipcProcessName){
		KnownIPCProcessConfiguration ipcConf = RINAConfiguration.getInstance().getIPCProcessConfiguration(ipcProcessName);
		if (ipcConf != null){
			return ipcConf.getHostName();
		}else{
			return null;
		}
	}
	
	/**
	 * Close all the sockets and stop
	 */
	@Override
	public void stop(){
		Iterator<Integer> iterator = flowTable.keySet().iterator();
		Socket socket = null;
		
		while(iterator.hasNext()){
			try{
				socket = getSocket(iterator.next().intValue());
				socket.close();
			}catch(Exception ex){
				log.error(ex.getMessage());
			}
		}
		
		this.rmtServer.setEnd(true);
	}

	/**
	 * When the RMT receives an EFCP PDU via a send primitive, it inspects the destination 
	 * address field and the connection-id field of the PDU. Using the FIB, it determines 
	 * which queue, the PDU should be placed on
	 * @param pdu
	 */
	public void sendEFCPPDU(byte[] pdu) {
		//It will never be called by this implementation since DTP is not implemented yet and 
		//each flow allocation triggers a new TCP connection
	}
	
	/**
	 * Cause the RMT to allocate a new flow through an N-1 DIF or the underlying
	 * physical media
	 * @param apNamingInfo the destination application process naming information 
	 * @param qosparams the quality of service requested by the flow
	 * @return int the portId allocated to the flow
	 * @throws Exception if there was an issue allocating the flow
	 */
	public int allocateFlow(String ipcProcessName, QualityOfServiceSpecification qosparams) throws Exception{
		KnownIPCProcessConfiguration ipcConf = RINAConfiguration.getInstance().getIPCProcessConfiguration(ipcProcessName);
		if(ipcConf == null){
			throw new Exception("Unrecognized IPC Process: "+ipcProcessName);
		}
		
		Socket socket = new Socket(ipcConf.getHostName(), ipcConf.getRmtPortNumber());
		int portId = socket.getLocalPort();
		newConnectionAccepted(socket, portId);
		return portId;
	}
	
	/**
	 * Cause the RMT to deallocate a flow through an N-1 DIF or the underlying physical media
	 * @param portId the identifier of the flow
	 * @throws Exception if the flow is not allocated or there are problems deallocating the flow
	 */
	public void deallocateFlow(int portId) throws Exception{
		Socket socket = getSocket(portId);
		socket.close();
	}

	/**
	 * Send a CDAP message to other end of the flow identified by the "port id". 
	 * This operation is invoked by the management tasks of the IPC process, usually to 
	 * send CDAP messages to the nearest neighbors. The RMT will lookup the 'portId' 
	 * parameter in the flow table, and send the capMessage using the management flow 
	 * that was established when this IPC process joined the DIF.
	 * @param portId
	 * @param cdapMessage
	 * @throws IPCException
	 */
	public void sendCDAPMessage(int portId, byte[] cdapMessage) throws Exception{
		Socket socket = getSocket(portId);
		Delimiter delimiter = (Delimiter) getIPCProcess().getIPCProcessComponent(BaseDelimiter.getComponentName());
		byte[] delimitedSdu = delimiter.getDelimitedSdu(cdapMessage);
		
		try{
			//Writing a PDU on the socket must be an atomic operation
			synchronized(socketWriteLock){
				socket.getOutputStream().write(delimitedSdu);
			}
		}catch(IOException ex){
			log.error("Problems sending a PDU through flow "+portId+": "+ex.getMessage());
			this.connectionEnded(portId);
			throw new Exception("Flow closed", ex);
		}
	}
	
	private Socket getSocket(int portId) throws Exception{
		Socket socket = null;
		synchronized(flowTable){
			socket = flowTable.get(new Integer(portId));
		}
		
		if (socket == null){
			throw new Exception("Flow closed");
		}
		
		return socket;
	}
	
	/**
	 * When a remote IPC process connects to the RMT of this IPC Process this operation is called.
	 * It will add the socket to the forwarding table and start a new Thread to read data from the
	 * socket
	 * @param socket
	 */
	public void newConnectionAccepted(Socket socket, int portId){
		synchronized(flowTable){
			flowTable.put(new Integer(portId), socket);	
		}
		Delimiter delimiter = (Delimiter) getIPCProcess().getIPCProcessComponent(BaseDelimiter.getComponentName());
		RIBDaemon ribdaemon = (RIBDaemon) getIPCProcess().getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		CDAPSessionManager cdapSessionManager = (CDAPSessionManager) getIPCProcess().getIPCProcessComponent(BaseCDAPSessionManager.getComponentName());
		TCPSocketReader tcpSocketReader = new TCPSocketReader(socket, portId, ribdaemon, delimiter, cdapSessionManager);
		this.getIPCProcess().execute(tcpSocketReader);
	}
	
	/**
	 * Called when a new event has happened
	 * @param event
	 */
	public void eventHappened(Event event) {
		if (event.getId().equals(Event.N_MINUS_1_FLOW_DEALLOCATED)){
			NMinusOneFlowDeallocatedEvent flowEvent = (NMinusOneFlowDeallocatedEvent) event;
			this.connectionEnded(flowEvent.getPortId());
		}else if (event.getId().equals(Event.NEIGHBOR_DECLARED_DEAD)){
			NeighborDeclaredDeadEvent neighborEvent = (NeighborDeclaredDeadEvent) event;
			Socket socket = flowTable.get(new Integer(neighborEvent.getNeighbor().getUnderlyingPortId()));
			try{
				socket.close();
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Called when the socket identified by portId is no longer connected
	 * @param portId
	 */
	private void connectionEnded(int portId){
		synchronized(flowTable){
			flowTable.remove(new Integer(portId));
		}
	}
	
	private String printBytes(byte[] message){
		String result = "";
		for(int i=0; i<message.length; i++){
			result = result + String.format("%02X", message[i]) + " ";
		}
		
		return result;
	}
}
