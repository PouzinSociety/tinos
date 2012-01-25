package rina.ipcmanager.impl.apservice;

import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.ObjectValue;
import rina.delimiting.api.Delimiter;
import rina.delimiting.api.DelimiterFactory;
import rina.encoding.api.Encoder;
import rina.ipcmanager.api.InterDIFDirectory;
import rina.ipcmanager.impl.IPCManagerImpl;
import rina.ipcmanager.impl.apservice.FlowServiceState.Status;
import rina.ipcprocess.api.IPCProcessFactory;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCException;
import rina.ipcservice.api.IPCService;

/**
 * Implements the part of the IPCManager that deals with applications
 * @author eduardgrasa
 *
 */
public class APServiceImpl implements APService{
	private static final Log log = LogFactory.getLog(APServiceImpl.class);
	private static final int MAXWORKERTHREADS = 10;
	
	/**
	 * The thread pool implementation
	 */
	private ExecutorService executorService = null;
	
	/**
	 * The IPC Process factory
	 */
	private IPCProcessFactory ipcProcessFactory = null;

	private InterDIFDirectory interDIFDirectory = null;
	
	private CDAPSessionManager cdapSessionManager = null;
	
	private Encoder encoder = null;
	
	private Delimiter delimiter = null;
	
	/**
	 * Server listening for incoming connections from applications
	 */
	private APServiceTCPServer tcpServer = null;
	
	/**
	 * The state of all the requested flow services, hashed by portId
	 */
	private Map<Integer, FlowServiceState> flowServices = null;
	
	public APServiceImpl(){
		tcpServer = new APServiceTCPServer(this);
		executorService = Executors.newFixedThreadPool(MAXWORKERTHREADS);
		executorService.execute(tcpServer);
		flowServices = new Hashtable<Integer, FlowServiceState>();
	}
	
	public void setInterDIFDirectory(InterDIFDirectory interDIFDirectory){
		this.interDIFDirectory = interDIFDirectory;
	}
	
	public void setIPCProcessFactory(IPCProcessFactory ipcProcessFactory){
		this.ipcProcessFactory = ipcProcessFactory;
		cdapSessionManager = ipcProcessFactory.getCDAPSessionManagerFactory().createCDAPSessionManager();
		encoder = ipcProcessFactory.getEncoderFactory().createEncoderInstance();
		delimiter = ipcProcessFactory.getDelimiterFactory().createDelimiter(DelimiterFactory.DIF);
	}
	
	/**
	 * Start a new thread to read from the socket
	 * @param socket
	 */
	public synchronized void newConnectionAccepted(Socket socket){
		TCPSocketReader socketReader = new TCPSocketReader(socket, ipcProcessFactory.getDelimiterFactory().createDelimiter(DelimiterFactory.DIF),
				ipcProcessFactory.getEncoderFactory().createEncoderInstance(), ipcProcessFactory.getCDAPSessionManagerFactory().createCDAPSessionManager(), 
				this);
		executorService.execute(socketReader);
		
		//TODO, keep a list of active socketReaders in this class? Will this ever be needed?
	}
	
	/**
	 * This operation will query the IDD to see what DIF is the one through which the destination application process 
	 * is available. After that, it will find the IPC process that belongs to that DIF, and trigger a flow allocation 
	 * request.
	 * @param flowService
	 * @param cdapMessage
	 * @param socket
	 */
	public synchronized void processAllocateRequest(FlowService flowService, CDAPMessage cdapMessage, Socket socket, TCPSocketReader tcpSocketReader){
		//1 check that there isn't a flow already allocated or in the process of being allocated
		if (flowServices.get(new Integer(socket.getPort())) != null){
			CDAPMessage errorMessage = cdapMessage.getReplyMessage();
			errorMessage.setResult(1);
			errorMessage.setResultReason("A flow is already allocated or being allocated, cannot allocate another one.");
			sendErrorMessageAndCloseSocket(errorMessage, socket);
		}
		
		String difName = interDIFDirectory.mapApplicationProcessNamingInfoToDIFName(flowService.getDestinationAPNamingInfo());
		
		//Look for the local IPC Process that is a member of difName
		IPCService ipcService = (IPCService) ipcProcessFactory.getIPCProcessBelongingToDIF(difName);
		if (ipcService == null){
			CDAPMessage errorMessage = cdapMessage.getReplyMessage();
			errorMessage.setResult(1);
			errorMessage.setResultReason("Could not find an IPC Process belonging to DIF " + difName + " in this system");
			sendErrorMessageAndCloseSocket(errorMessage, socket);
		}
		
		//Once we have the IPCService, invoke allocate request
		try{
			flowService.setPortId(socket.getPort());
			ipcService.submitAllocateRequest(flowService, this);
		}catch(IPCException ex){
			ex.printStackTrace();
			CDAPMessage errorMessage = cdapMessage.getReplyMessage();
			errorMessage.setResult(1);
		}
		
		//Store the state of the requested flow service
		FlowServiceState flowServiceState = new FlowServiceState();
		flowServiceState.setFlowService(flowService);
		flowServiceState.setSocket(socket);
		flowServiceState.setIpcService(ipcService);
		flowServiceState.setCdapMessage(cdapMessage);
		flowServiceState.setTcpSocketReader(tcpSocketReader);
		flowServiceState.setStatus(Status.ALLOCATION_REQUESTED);
		flowServices.put(new Integer(socket.getPort()), flowServiceState);
	}
	
	/**
	 * Call the IPC process to release the flow associated to the socket. Once it is 
	 * released, reply back to the application library with an M_RELEASE_R and close
	 * the socket.
	 * @param socket
	 */
	public synchronized void processDeallocateRequest(Socket socket, CDAPMessage cdapMessage){
		FlowServiceState flowServiceState = flowServices.get(new Integer(socket.getPort()));
		if (flowServiceState == null){
			CDAPMessage errorMessage = cdapMessage.getReplyMessage();
			errorMessage.setResult(1);
			errorMessage.setResultReason("Received a deallocate request for portid " + socket.getPort() + ", but there is no allocated flow identified by this portId");
			sendErrorMessageAndCloseSocket(errorMessage, socket);
			return;
		}
		
		if (!flowServiceState.getStatus().equals(Status.ALLOCATED)){
			//TODO, what to do?
		}
		
		try{
			flowServiceState.getIpcService().submitDeallocateRequest(socket.getPort(), this);
			flowServiceState.setCdapMessage(cdapMessage);
			flowServiceState.setStatus(Status.DEALLOCATION_REQUESTED);
		}catch(Exception ex){
			ex.printStackTrace();
			//TODO, what to do?
		}
	}
	
	/**
	 * Just call the IPC process to release the flow associated to the socket, if it was not already done.
	 * @param socket
	 */
	public synchronized void processSocketClosed(Socket socket){
		FlowServiceState flowServiceState = flowServices.get(new Integer(socket.getPort()));
		if (flowServiceState == null){
			return;
		}
		
		if (!flowServiceState.getStatus().equals(Status.ALLOCATED)){
			return;
		}
		
		try{
			flowServiceState.getIpcService().submitDeallocateRequest(socket.getPort(), this);
			flowServiceState.setCdapMessage(null);
			flowServiceState.setStatus(Status.DEALLOCATION_REQUESTED);
		}catch(Exception ex){
			ex.printStackTrace();
			//TODO, what to do?
		}
		
	}

	public synchronized void deliverAllocateRequest(FlowService flowService) {
		// TODO Auto-generated method stub
		
	}

	
	/**
	 * This primitive is invoked by the IPC process to the IPC Manager
	 * to indicate the success or failure of the request associated with this port-id. 
	 * @param requestedAPinfo
	 * @param port_id -1 if error, portId otherwise
	 * @param result errorCode if result > 0, ok otherwise
	 * @param resultReason null if no error, error description otherwise
	 */
	public synchronized void deliverAllocateResponse(ApplicationProcessNamingInfo requestedAPinfo, int portId, int result, String resultReason){
		FlowServiceState flowServiceState = flowServices.get(new Integer(portId));
		if (flowServiceState == null){
			log.warn("Received an allocate response for portid " + portId + ", but didn't have any pending allocation request identified by this portId");
			return;
		}
		
		if (!flowServiceState.getStatus().equals(FlowServiceState.Status.ALLOCATION_REQUESTED)){
			//TODO, what to do?
			return;
		}
		
		switch(result){
		case 0:
			try{
				flowServiceState.setStatus(Status.ALLOCATED);
				flowServiceState.getTcpSocketReader().setIPCService(flowServiceState.getIpcService());
				byte[] encodedValue = encoder.encode(flowServiceState.getFlowService());
				ObjectValue objectValue = new ObjectValue();
				objectValue.setByteval(encodedValue);
				CDAPMessage confirmationMessage = flowServiceState.getCdapMessage().getReplyMessage();
				confirmationMessage.setObjValue(objectValue);
				confirmationMessage.setResult(result);
				confirmationMessage.setResultReason(resultReason);
				sendReplyMessage(confirmationMessage, flowServiceState.getSocket());
			}catch(Exception ex){
				ex.printStackTrace();
				//TODO what to do?
			}
			break;
		default:
			flowServices.remove(new Integer(portId));
			CDAPMessage errorMessage = flowServiceState.getCdapMessage().getReplyMessage();
			errorMessage.setResult(result);
			errorMessage.setResultReason(resultReason);
			sendErrorMessageAndCloseSocket(errorMessage, flowServiceState.getSocket());
		}
	}
	
	/**
	 * Invoked when in the Transfer state to deliver an SDU on this portId. It will encapsulate 
	 * the SDU in a CDAP M_READ_R message and send it to the application library through the 
	 * socket associated to this portId.
	 * @param port_id
	 * @param sdu
	 */
	public synchronized void deliverTransfer(int portId, byte[] sdu){
		FlowServiceState flowServiceState = flowServices.get(new Integer(portId));
		if (flowServiceState == null){
			log.warn("Received data from portid " + portId + ", but didn't have any allocated flow on this port");
			return;
		}
		
		if (!flowServiceState.getStatus().equals(FlowServiceState.Status.ALLOCATED)){
			//TODO, what to do?
			return;
		}
		
		try{
			ObjectValue objectValue = new ObjectValue();
			objectValue.setByteval(sdu);
			CDAPMessage cdapMessage = CDAPMessage.getReadObjectResponseMessage(null, null, 0, null, objectValue, 0, null, 1);
			sendReplyMessage(cdapMessage, flowServiceState.getSocket());
		}catch(Exception ex){
			ex.printStackTrace();
			//TODO, what to do?
		}
	}
	
	/**
	 * Invoked in any state by an Flow Allocator instance to notify the local application process that the release 
	 * of all the resources allocated to this instance are released 
	 * @param portId
	 * @param result
	 * @param resultReason
	 */
	public void deliverDeallocateResponse(int portId, int result, String resultReason){
		FlowServiceState flowServiceState = flowServices.get(new Integer(portId));
		if (flowServiceState == null){
			log.warn("Received a deallocate response for portid " + portId + ", but didn't have any pending deallocation request identified by this portId");
			return;
		}
		
		if (!flowServiceState.getStatus().equals(FlowServiceState.Status.DEALLOCATION_REQUESTED)){
			//TODO, what to do?
			return;
		}
		
		switch(result){
		case 0:
			flowServices.remove(new Integer(portId));
			if (flowServiceState.getCdapMessage() != null){
				CDAPMessage responseMessage = flowServiceState.getCdapMessage().getReplyMessage();
				sendReplyMessage(responseMessage, flowServiceState.getSocket());
				try{
					flowServiceState.getSocket().close();
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
			break;
		default:
			flowServiceState.setStatus(Status.ALLOCATED);
			if (flowServiceState.getCdapMessage() != null){
				CDAPMessage errorMessage = flowServiceState.getCdapMessage().getReplyMessage();
				errorMessage.setResult(result);
				errorMessage.setResultReason(resultReason);
				sendReplyMessage(errorMessage, flowServiceState.getSocket());
			}
		}
	}
	
	/**
	 * Invoked when a Delete_Flow primitive is received at the requested IPC process
	 * @param request
	 */
	public void deliverDeallocateRequest(int portId) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Send a reply message to the application library
	 * @param cdapMessage
	 * @param socket
	 */
	public synchronized void sendReplyMessage(CDAPMessage cdapMessage, Socket socket){
		try{
			sendCDAPMessage(cdapMessage, socket);
		}catch(Exception ex){
			ex.printStackTrace();
			//TODO what to do?
		}
	}
	
	/**
	 * Send a CDAP reply message to the application library, and after that close the 
	 * socket.
	 * @param message
	 * @param socket
	 */
	public synchronized void sendErrorMessageAndCloseSocket(CDAPMessage cdapMessage, Socket socket){
		log.error(cdapMessage.getResultReason());
		try{
			sendCDAPMessage(cdapMessage, socket);
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			try{
				socket.close();
			}catch(IOException ex){
				ex.printStackTrace();
			}
		}
	}
	
	private void sendCDAPMessage(CDAPMessage cdapMessage, Socket socket) throws CDAPException, IOException{
		byte[] encodedCDAPMessage = cdapSessionManager.encodeCDAPMessage(cdapMessage);
		byte[] delimitedMessage = delimiter.getDelimitedSdu(encodedCDAPMessage);
		socket.getOutputStream().write(delimitedMessage);
	}

	public synchronized void deliverStatus(int arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}

}
