package rina.applibrary.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applibrary.api.Flow;
import rina.applibrary.api.FlowAcceptor;
import rina.applibrary.api.FlowImpl;
import rina.applibrary.api.FlowListener;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.Opcode;
import rina.delimiting.api.Delimiter;
import rina.encoding.api.Encoder;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCException;

/**
 * Processes the incoming requests that try to create 
 * a new flow to the application
 * @author eduardgrasa
 *
 */
public class FlowRequestsServer implements Runnable{
	
	private static final Log log = LogFactory.getLog(FlowRequestsServer.class);

	/**
	 * The server socket that listens for incoming connections
	 */
	private ServerSocket serverSocket = null;
	
	private FlowAcceptor flowAcceptor = null;
	
	private FlowListener flowListener = null;
	
	private BlockingQueue<Flow> acceptedFlowsQueue = null;
	
	private Delimiter delimiter = null;
	
	private CDAPSessionManager cdapSessionManager = null;
	
	private Encoder encoder = null;
	
	private boolean end = false;
	
	public void setEnd(boolean end){
		this.end = end;
		if (end){
			try{
				this.serverSocket.close();
			}catch(IOException ex){
				ex.printStackTrace();
			}
		}
	}
	
	public FlowRequestsServer(ServerSocket serverSocket, FlowAcceptor flowAcceptor, FlowListener flowListener){
		this(serverSocket, flowAcceptor);
		this.flowListener = flowListener;
	}
	
	public FlowRequestsServer(ServerSocket serverSocket, FlowAcceptor flowAcceptor, BlockingQueue<Flow> acceptedFlowsQueue){
		this(serverSocket, flowAcceptor);
		this.acceptedFlowsQueue = acceptedFlowsQueue;
	}
	
	private FlowRequestsServer(ServerSocket serverSocket, FlowAcceptor flowAcceptor){
		this.serverSocket = serverSocket;
		this.flowAcceptor = flowAcceptor;
	}
	
	public void setDelimiter(Delimiter delimiter){
		this.delimiter = delimiter;
	}
	
	public void setCDAPSessionManager(CDAPSessionManager cdapSessionManager){
		this.cdapSessionManager = cdapSessionManager;
	}
	
	public void setEncoder(Encoder encoder){
		this.encoder = encoder;
	}

	public void run() {
		try{
			log.info("Registered application waiting for incoming TCP connections from the local RINA software at port "+serverSocket.getLocalPort());
			while (!end){
				Socket socket = serverSocket.accept();
				String address = socket.getInetAddress().getHostAddress();
				String hostname = socket.getInetAddress().getHostName();
				
				//Accept only local connections
				if (!address.equals("127.0.0.1") && !hostname.equals("localhost")){
					log.info("Connection attempt from "+address+" blocked");
					socket.close();
					continue;
				}
				
				log.info("Got a new request from "+socket.getInetAddress().getHostAddress() + 
						". Local port: "+socket.getLocalPort()+"; Remote port: "+socket.getPort());
				
				processFlowEstablishmentAttempt(socket);
			}
		}catch(IOException e){
			log.info(e.getMessage());
		}
	}
	
	/**
	 * Get the M_CREATE message with the FlowService object, see if the flow 
	 * can be accepted (flow acceptor). If it is accepted, create a new Flow object 
	 * and notify the listener or put it in the queue; then reply back to the RINA 
	 * software notifying the acceptance of the flow. If not, reply back notifying 
	 * the flow rejection.
	 * @param socket
	 */
	private void processFlowEstablishmentAttempt(Socket socket){
		byte[] pdu = null;
		CDAPMessage cdapMessage = null;
		FlowService flowService = null;
		String result = null;
		Flow flow = null;
		
		//1 Get the first PDU from the socket
		pdu = getFirstPDUFromSocket(socket);
		if (pdu == null){
			log.error("Problems getting the first PDU from the socket, closing it");
			try{
				socket.close();
			}catch(IOException ex){
			}
		}
		 
		try{
			//2 Parse it and make sure it is an M_CREATE message
			cdapMessage = cdapSessionManager.decodeCDAPMessage(pdu);
			if (cdapMessage.getOpCode() != Opcode.M_CREATE){
				throw new Exception("The opcode of the CDAP message is "+cdapMessage.getOpCode()+" instead of M_CREATE");
			}
			
			//3 Parse the flowService
			flowService = (FlowService) encoder.decode(cdapMessage.getObjValue().getByteval(), FlowService.class.toString());
			
			//4 Invoke the flowAcceptor class
			result = flowAcceptor.acceptFlow(flowService.getSourceAPNamingInfo(), flowService.getDestinationAPNamingInfo());
			if (result != null){
				throw new Exception(result);
			}
		}catch(Exception ex){
			log.error("Problems accepting the new flow: "+ ex.getMessage());
			try{
				cdapMessage = CDAPMessage.getCreateObjectResponseMessage(null, null, 0, null, null, 1, ex.getMessage(), 1);
				pdu = cdapSessionManager.encodeCDAPMessage(cdapMessage);
				socket.getOutputStream().write(delimiter.getDelimitedSdu(pdu));
				if (!socket.isClosed()){
					socket.close();
				}
			}catch(Exception e){
				log.error(e);
				//TODO, what to do?
			}
		}
		
		//5 Create the flow object, and either notify the flowListener or put it in the queue
		//(depending if we are on blocking or non-blocking operation)
		FlowImpl flowImpl = new DefaultFlowImpl();
		flowImpl.setSourceApplication(flowService.getSourceAPNamingInfo());
		flowImpl.setDestinationApplication(flowService.getDestinationAPNamingInfo());
		flowImpl.setQosSpec(flowService.getQoSSpecification());
		try{
			((DefaultFlowImpl)flowImpl).setCDAPSessionManager(cdapSessionManager);
			((DefaultFlowImpl)flowImpl).setDelimiter(delimiter);
			((DefaultFlowImpl)flowImpl).setEncoder(encoder);
			((DefaultFlowImpl)flowImpl).setPortId(flowService.getPortId());
			flowImpl.setSocket(socket);
		}catch(IPCException ex){
			log.error("This error should never happen " + ex);
		}
		//TODO ignoring QoS parameters for now
		//SDU listener has to be set by the flowListerner (non-blocking) or will be set by the accept() (blocking) call in the Flow object
		
		flow = new Flow(flowImpl);
		try{
			if (flowListener == null){
				acceptedFlowsQueue.put(flow);
			}else{
				flowListener.flowAccepted(flow);
			}
		}catch(Exception ex){
			log.error(ex);
			//TODO, what to do?
		}
		
		//6 The flow has been accepted, answer back positively
		try{
			log.debug("Flow accepted, replying back.");
			cdapMessage = CDAPMessage.getCreateObjectResponseMessage(null, null, 0, null, null, 0, null, 1);
			pdu = cdapSessionManager.encodeCDAPMessage(cdapMessage);
			socket.getOutputStream().write(delimiter.getDelimitedSdu(pdu));
		}catch(Exception ex){
			ex.printStackTrace();
			log.error(ex);
			//TODO, what to do?
		}
	}
	
	private byte[] getFirstPDUFromSocket(Socket socket){
		boolean lookingForSduLength = true;
		int length = 0;
		int index = 0;
		byte[] lastSduLengthCandidate = new byte[0];
		byte[] currentSduLengthCandidate = null;
		byte[] pdu = null;
		byte nextByte = 0;
		int value = 0;
		
		while(true){
			//Delimit the byte array that contains a serialized CDAP message
			try{
				value = socket.getInputStream().read();
				if (value == -1){
					return null;
				}
	
				nextByte = (byte) value;
				if (lookingForSduLength){
					currentSduLengthCandidate = new byte[lastSduLengthCandidate.length + 1];
					for(int i=0; i<lastSduLengthCandidate.length; i++){
						currentSduLengthCandidate[i] = lastSduLengthCandidate[i];
					}
					currentSduLengthCandidate[lastSduLengthCandidate.length] = nextByte;
					length = delimiter.readVarint32(currentSduLengthCandidate);
					if (length == -2){
						lastSduLengthCandidate = currentSduLengthCandidate;
					}else{
						lastSduLengthCandidate = new byte[0];
						if (length > 0){
							log.debug("Found a delimited CDAP message, of length " + length);
							lookingForSduLength = false;
						}
					}
				}else{
					if (index < length){
						if (pdu == null){
							pdu = new byte[length];
						}
						pdu[index] = nextByte;
						index ++;
						if (index == length){
							return pdu;

						}
					}
				}
			}catch(IOException ex){
				ex.printStackTrace();
				return null;
			}
		}
	}
}
