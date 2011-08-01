package rina.rmt.impl.tcp;

import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.delimiting.api.BaseDelimiter;
import rina.delimiting.api.Delimiter;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.IPCException;
import rina.ipcservice.api.QoSParameters;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemon;
import rina.rmt.api.BaseRMT;

/**
 * Specifies the interface of the Relaying and Multiplexing task. Mediates the access to one or more (N-1) DIFs 
 * or physical media
 * @author eduardgrasa
 */
public class TCPRMTImpl extends BaseRMT{
	private static final Log log = LogFactory.getLog(TCPRMTImpl.class);
	
	/**
	 * Contains the open TCP flows to other IPC processes, indexed by portId
	 */
	private Map<Integer, Socket> flowTable = null;
	
	/**
	 * The thread pool implementation
	 */
	private ExecutorService executorService = null;
	
	/**
	 * The maximum number of worker threads in the RMT Thread pool
	 * (1 for listening to incoming connections + MAX-1 for reading 
	 * data from sockets)
	 */
	private static int MAXWORKERTHREADS = 10;
	
	/**
	 * The server that will listen for incoming connections to this RMT
	 */
	private RMTServer rmtServer = null;

	public TCPRMTImpl(){
		this(RMTServer.DEFAULT_PORT);
	}
	
	public TCPRMTImpl(int port){
		this.flowTable = new Hashtable<Integer, Socket>();
		this.executorService = Executors.newFixedThreadPool(MAXWORKERTHREADS);
		this.rmtServer = new RMTServer(this, port);
		executorService.execute(rmtServer);
	}

	/**
	 * When the RMT receives an EFCP PDU via a send primitive, it inspects the destination 
	 * address field and the connection-id field of the PDU. Using the FIB, it determines 
	 * which queue, the PDU should be placed on
	 * @param pdu
	 */
	public synchronized void sendEFCPPDU(byte[] pdu) {
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
	public int allocateFlow(ApplicationProcessNamingInfo apNamingInfo, QoSParameters qosparams) throws Exception{
		String host = apNamingInfo.getApplicationProcessName();
		int port = RMTServer.DEFAULT_PORT;
		
		//TODO need to map the application naming information to the IP address of the IPC process
		//right one assuming that the application name is the IP address 
		if (apNamingInfo.getApplicationProcessInstance() != null){
			try{
				port = Integer.parseInt(apNamingInfo.getApplicationProcessInstance());
			}catch(NumberFormatException ex){
				ex.printStackTrace();
				port = RMTServer.DEFAULT_PORT;
			}
		}
		
		Socket socket = new Socket(host, port);
		newConnectionAccepted(socket);
		return socket.getPort();
	}
	
	/**
	 * Cause the RMT to deallocate a flow through an N-1 DIF or the underlying physical media
	 * @param portId the identifier of the flow
	 * @throws Exception if the flow is not allocated or there are problems deallocating the flow
	 */
	public void deallocateFlow(int portId) throws Exception{
		Socket socket = flowTable.get(new Integer(portId));
		if (socket == null){
			throw new Exception("Unexisting flow");
		}
		
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
	public synchronized void sendCDAPMessage(int portId, byte[] cdapMessage) throws Exception{
		Socket socket = flowTable.get(new Integer(portId));
		if (socket == null){
			throw new Exception("Flow closed");
		}
		
		Delimiter delimiter = (Delimiter) getIPCProcess().getIPCProcessComponent(BaseDelimiter.getComponentName());
		byte[] delimitedSdu = delimiter.getDelimitedSdu(cdapMessage);
		try{
			socket.getOutputStream().write(delimitedSdu);
			log.debug("Sent PDU through flow "+portId+": "+printBytes(delimitedSdu));
		}catch(IOException ex){
			log.error("Problems sending a PDU through flow "+portId+": "+ex.getMessage());
			this.connectionEnded(portId);
			throw new Exception("Flow closed", ex);
		}
	}
	
	/**
	 * When a remote IPC process connects to the RMT of this IPC Process this operation is called.
	 * It will add the socket to the forwarding table and start a new Thread to read data from the
	 * socket
	 * @param socket
	 */
	public void newConnectionAccepted(Socket socket){
		flowTable.put(new Integer(socket.getPort()), socket);
		Delimiter delimiter = (Delimiter) getIPCProcess().getIPCProcessComponent(BaseDelimiter.getComponentName());
		RIBDaemon ribdaemon = (RIBDaemon) getIPCProcess().getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		TCPSocketReader tcpSocketReader = new TCPSocketReader(socket, ribdaemon, delimiter, this);
		executorService.execute(tcpSocketReader);
	}
	
	/**
	 * Called when the socket identified by portId is no longer connected
	 * @param portId
	 */
	public synchronized void connectionEnded(int portId){
		flowTable.remove(new Integer(portId));
	}
	
	private String printBytes(byte[] message){
		String result = "";
		for(int i=0; i<message.length; i++){
			result = result + String.format("%02X", message[i]) + " ";
		}
		
		return result;
	}
}