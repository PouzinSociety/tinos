package rina.ipcmanager.impl.apservice;

import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.ObjectValue;
import rina.delimiting.api.Delimiter;
import rina.delimiting.api.DelimiterFactory;
import rina.encoding.api.Encoder;
import rina.ipcmanager.api.IPCManager;
import rina.ipcmanager.api.InterDIFDirectory;
import rina.ipcmanager.impl.apservice.FlowServiceState.Status;
import rina.ipcprocess.api.IPCProcessFactory;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.ApplicationRegistration;
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
	
	/**
	 * The IPC Process factory
	 */
	private IPCProcessFactory ipcProcessFactory = null;
	
	private IPCManager ipcManager = null;

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
	
	private Map<String, ApplicationRegistrationState> applicationRegistrations = null;
	
	public APServiceImpl(IPCManager ipcManager){
		this.ipcManager = ipcManager;
		tcpServer = new APServiceTCPServer(this);
		ipcManager.execute(tcpServer);
		flowServices = new Hashtable<Integer, FlowServiceState>();
		applicationRegistrations = new Hashtable<String, ApplicationRegistrationState>();
	}
	
	public void stop(){
		tcpServer.setEnd(true);
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
		ipcManager.execute(socketReader);
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
			return;
		}
		
		//TODO currently choosing the first DIF suggested by the IDD
		List<String> difNames = interDIFDirectory.mapApplicationProcessNamingInfoToDIFName(flowService.getDestinationAPNamingInfo());
		String difName = null;
		if (difNames != null){
			difName = difNames.get(0);
		}else{
			CDAPMessage errorMessage = cdapMessage.getReplyMessage();
			errorMessage.setResult(1);
			errorMessage.setResultReason("Could not find a DIF for application process "+flowService.getDestinationAPNamingInfo());
			sendErrorMessageAndCloseSocket(errorMessage, socket);
			return;
		}
		
		//Look for the local IPC Process that is a member of difName
		IPCService ipcService = (IPCService) ipcProcessFactory.getIPCProcessBelongingToDIF(difName);
		if (ipcService == null){
			CDAPMessage errorMessage = cdapMessage.getReplyMessage();
			errorMessage.setResult(1);
			errorMessage.setResultReason("Could not find an IPC Process belonging to DIF " + difName + " in this system");
			sendErrorMessageAndCloseSocket(errorMessage, socket);
			return;
		}
		
		//Once we have the IPCService, invoke allocate request
		try{
			int portId = ipcService.submitAllocateRequest(flowService);
			tcpSocketReader.setPortId(portId);
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
		flowServices.put(new Integer(flowService.getPortId()), flowServiceState);
	}
	
	/**
	 * Call the IPC process to release the flow associated to the socket. Once it is 
	 * released, reply back to the application library with an M_RELEASE_R and close
	 * the socket.
	 * @param socket
	 */
	public synchronized void processDeallocate(CDAPMessage cdapMessage, int portId, Socket socket){
		FlowServiceState flowServiceState = flowServices.get(new Integer(portId));
		if (flowServiceState == null){
			CDAPMessage errorMessage = cdapMessage.getReplyMessage();
			errorMessage.setResult(1);
			errorMessage.setResultReason("Received a deallocate request for portid " + portId + ", but there is no allocated flow identified by this portId");
			sendErrorMessageAndCloseSocket(errorMessage, socket);
			return;
		}
		
		if (!flowServiceState.getStatus().equals(Status.ALLOCATED)){
			//TODO, what to do?
		}
		
		try{
			flowServiceState.getIpcService().submitDeallocate(portId);
			
			flowServices.remove(new Integer(portId));
			ApplicationRegistrationState arState = applicationRegistrations.get(flowServiceState.getFlowService().getDestinationAPNamingInfo().getProcessKey());
			if (arState == null){
				//TODO what to do?
			}else{
				arState.getFlowServices().remove(flowServiceState);
			}
			
			CDAPMessage confirmationMessage = cdapMessage.getReplyMessage();
			sendMessage(confirmationMessage, socket);
			if (flowServiceState.getSocket().isConnected()){
				flowServiceState.getSocket().close();
			}
		}catch(Exception ex){
			ex.printStackTrace();
			//TODO, what to do?
		}
	}
	
	/**
	 * Just call the IPC process to release the flow associated to the socket, if it was not already done.
	 * @param portId
	 * @param apNamingInfo True if this socket was being used as an application registration
	 */
	public synchronized void processSocketClosed(int portId, ApplicationProcessNamingInfo apNamingInfo){
		if (apNamingInfo == null){
			FlowServiceState flowServiceState = flowServices.get(new Integer(portId));
			if (flowServiceState == null){
				return;
			}

			if (!flowServiceState.getStatus().equals(Status.ALLOCATED)){
				return;
			}

			try{
				flowServiceState.getIpcService().submitDeallocate(portId);
				flowServiceState.setCdapMessage(null);
				flowServiceState.setStatus(Status.DEALLOCATION_REQUESTED);
			}catch(Exception ex){
				ex.printStackTrace();
				//TODO, what to do?
			}
		}else{
			ApplicationRegistrationState apState = applicationRegistrations.get(apNamingInfo.getProcessKey());
			if (apState == null){
				return;
			}
			
			List<String> difNames = null;
			if (apState.getApplicationRegistration().getDifNames() == null || apState.getApplicationRegistration().getDifNames().size() == 0){
				difNames = ipcProcessFactory.listDIFNames();
			}else{
				difNames = apState.getApplicationRegistration().getDifNames();
			}
			
			for(int i=0; i<difNames.size(); i++){
				IPCService ipcService = (IPCService) ipcProcessFactory.getIPCProcessBelongingToDIF(difNames.get(i));
				if (ipcService != null){
					ipcService.unregister(apNamingInfo);
				}
			}
			
			interDIFDirectory.removeMapping(apNamingInfo, difNames);			
			applicationRegistrations.remove(apNamingInfo.getProcessKey());
			log.info("Application "+apNamingInfo.getProcessKey()+" implicitly canceled the registration with DIF(s) "+printStringList(difNames));
		}
	}
	
	/**
	 * Register the application to one or more DIFs available in this system. The application will open a 
	 * server socket at a certain port and listen for connection attempts.
	 * @param applicationRegistration
	 * @param cdapMessage
	 * @param socket
	 * @param tcpSocketReader
	 */
	public synchronized void processApplicationRegistrationRequest(ApplicationRegistration applicationRegistration, 
			CDAPMessage cdapMessage, Socket socket, TCPSocketReader tcpSocketReader){
		if(applicationRegistrations.get(applicationRegistration.getApNamingInfo().getProcessKey()) != null){
			CDAPMessage errorMessage = cdapMessage.getReplyMessage();
			errorMessage.setResult(1);
			errorMessage.setResultReason("The application is already registered through this socket");
			sendMessage(errorMessage, socket);
			return;
		}
		
		List<String> difNames = null;
		if (applicationRegistration.getDifNames() == null || applicationRegistration.getDifNames().size() == 0){
			difNames = ipcProcessFactory.listDIFNames();
		}else{
			difNames = applicationRegistration.getDifNames();
		}

		//Register the application in the required DIFs
		for(int i=0; i<difNames.size(); i++){
			IPCService ipcService = (IPCService) ipcProcessFactory.getIPCProcessBelongingToDIF(difNames.get(i));
			if (ipcService == null){
				//TODO error message, one of the DIF names is not available in the system
				//return?
			}else{
				ipcService.register(applicationRegistration.getApNamingInfo());
			}
		}
		
		interDIFDirectory.addMapping(applicationRegistration.getApNamingInfo(), difNames);

		//Reply to the application
		try{
			CDAPMessage confirmationMessage = cdapMessage.getReplyMessage();
			sendMessage(confirmationMessage, socket);
		}catch(Exception ex){
			ex.printStackTrace();
			//TODO what to do?
		}

		//Update the state
		ApplicationRegistrationState applicationRegistrationState = new ApplicationRegistrationState(applicationRegistration);
		applicationRegistrationState.setSocket(socket);
		applicationRegistrations.put(applicationRegistration.getApNamingInfo().getProcessKey(), applicationRegistrationState);
		
		log.info("Application "+applicationRegistration.getApNamingInfo().getProcessKey()+" registered to DIF(s) "+printStringList(difNames));
	}
	
	/**
	 * Unregister the application from one or more DIFs, do any house keeping and close the socket
	 * @param apNamingInfo
	 * @param cdapMessage
	 * @param socket
	 */
	public void processApplicationUnregistration(ApplicationProcessNamingInfo apNamingInfo, CDAPMessage cdapMessage, Socket socket){
		ApplicationRegistrationState apState = applicationRegistrations.get(apNamingInfo.getProcessKey());
		if (apState == null){
			CDAPMessage errorMessage = cdapMessage.getReplyMessage();
			errorMessage.setResult(1);
			errorMessage.setResultReason("The application is not registered through this socket");
			sendErrorMessageAndCloseSocket(cdapMessage, socket);
		}
		
		List<String> difNames = null;
		if (apState.getApplicationRegistration().getDifNames() == null || apState.getApplicationRegistration().getDifNames().size() == 0){
			difNames = ipcProcessFactory.listDIFNames();
		}else{
			difNames = apState.getApplicationRegistration().getDifNames();
		}
		
		for(int i=0; i<difNames.size(); i++){
			IPCService ipcService = (IPCService) ipcProcessFactory.getIPCProcessBelongingToDIF(difNames.get(i));
			if (ipcService != null){
				ipcService.unregister(apNamingInfo);
			}
		}
		
		interDIFDirectory.removeMapping(apNamingInfo, difNames);
		
		//Reply to the application
		try{
			CDAPMessage confirmationMessage = cdapMessage.getReplyMessage();
			sendMessage(confirmationMessage, socket);
		}catch(Exception ex){
			ex.printStackTrace();
			//TODO what to do?
		}
		
		applicationRegistrations.remove(apNamingInfo.getProcessKey());
		
		try{
			if (socket.isConnected()){
				socket.close();
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		log.info("Application "+apNamingInfo.getProcessKey()+" explicitly canceled the registration from DIF(s) "+printStringList(difNames));
	}

	/**
	 * Another application is trying to establish a flow to the application identified by 
	 * the application naming information within the FlowService object. This operation will look at the application 
	 * and see if it is registered. If it is, it will send an M_CREATE message to it, and wait for the response. If not,
	 * it will call the ipcService with a negative response. It could try to instantiate the destination application, 
	 * but this is not implemented right now.
	 * @param FlowService flowService
	 * @param IPCService the ipcService to call back
	 * @return string if there was no error it is null. If the IPC Manager could not find the
	 * destination application or something else bad happens, it will return a string detailing the error 
	 * (then the callback will never be invoked back)
	 */
	public synchronized String deliverAllocateRequest(FlowService flowService, IPCService ipcService){
		String key = flowService.getDestinationAPNamingInfo().getProcessKey();
		ApplicationRegistrationState registrationState = applicationRegistrations.get(key);
		if (registrationState == null){
			return "The destination application process is not registered";
		}
		
		//TODO, check if the request is coming from a DIF where the application is registered
		
		//Connect to the destination application process
		try{
			Socket socket = new Socket("localhost", registrationState.getApplicationRegistration().getSocketNumber());
			TCPSocketReader socketReader = new TCPSocketReader(socket, ipcProcessFactory.getDelimiterFactory().createDelimiter(DelimiterFactory.DIF),
					ipcProcessFactory.getEncoderFactory().createEncoderInstance(), ipcProcessFactory.getCDAPSessionManagerFactory().createCDAPSessionManager(), 
					this);
			socketReader.setPortId(flowService.getPortId());
			ipcManager.execute(socketReader);
			
			byte[] encodedObject = encoder.encode(flowService);
			ObjectValue objectValue = new ObjectValue();
			objectValue.setByteval(encodedObject);
			CDAPMessage cdapMessage = CDAPMessage.
				getCreateObjectRequestMessage(null, null, FlowService.OBJECT_CLASS, 0, FlowService.OBJECT_NAME, objectValue, 0);
			sendMessage(cdapMessage, socket);
			
			FlowServiceState flowServiceState = new FlowServiceState();
			flowServiceState.setFlowService(flowService);
			flowServiceState.setSocket(socket);
			flowServiceState.setIpcService(ipcService);
			flowServiceState.setStatus(Status.ALLOCATION_REQUESTED);
			flowServices.put(flowService.getPortId(), flowServiceState);
			registrationState.getFlowServices().add(flowServiceState);
		}catch(Exception ex){
			ex.printStackTrace();
			return ex.getMessage();
		}
		
		return null;
	}

	/**
	 * Invoked when the application process has sent an M_CREATE_R in response to an
	 * allocation request
	 * @param cdapMessage
	 * @param portId
	 */
	public synchronized void processAllocateResponse(CDAPMessage cdapMessage, int portId, TCPSocketReader socketReader){
		FlowServiceState flowServiceState = flowServices.get(portId);
		if (flowServiceState == null || !flowServiceState.getStatus().equals(Status.ALLOCATION_REQUESTED)){
			//TODO, what to do? just send error message?
			return;
		}
		
		if (cdapMessage.getResult() == 0){
			//Flow allocation accepted
			try{
				flowServiceState.getIpcService().submitAllocateResponse(portId, true, null);
				flowServiceState.setStatus(Status.ALLOCATED);
				socketReader.setIPCService(flowServiceState.getIpcService());
			}catch(Exception ex){
				ex.printStackTrace();
				//TODO what to do?
			}
		}else{
			//Flow allocation denied
			try{
				flowServiceState.getIpcService().submitAllocateResponse(portId, false, cdapMessage.getResultReason());
				flowServices.remove(portId);
				String key = flowServiceState.getFlowService().getDestinationAPNamingInfo().getProcessKey();
				ApplicationRegistrationState registrationState = applicationRegistrations.get(key);
				registrationState.getFlowServices().remove(flowServiceState);
				Socket socket = flowServiceState.getSocket();
				if (socket.isConnected()){
					socket.close();
				}
			}catch(Exception ex){
				ex.printStackTrace();
				//TODO what to do?
			}
		}
	}
	
	/**
	 * This primitive is invoked by the IPC process to the IPC Manager
	 * to indicate the success or failure of the request associated with this port-id. 
	 * @param port_id -1 if error, portId otherwise
	 * @param result errorCode if result > 0, ok otherwise
	 * @param resultReason null if no error, error description otherwise
	 */
	public synchronized void deliverAllocateResponse(int portId, int result, String resultReason){
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
				sendMessage(confirmationMessage, flowServiceState.getSocket());
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
			CDAPMessage cdapMessage = CDAPMessage.getWriteObjectRequestMessage(null, null, null, 0, objectValue, null, 1);
			sendMessage(cdapMessage, flowServiceState.getSocket());
		}catch(Exception ex){
			ex.printStackTrace();
			//TODO, what to do?
		}
	}
	
	/**
	 * Invoked when a Delete_Flow primitive invoked by an IPC process
	 * @param request
	 */
	public void deliverDeallocate(int portId) {
		FlowServiceState flowServiceState = flowServices.get(new Integer(portId));
		if (flowServiceState == null){
			log.warn("Received a deallocate response for portid " + portId + ", but didn't have any pending deallocation request identified by this portId");
			return;
		}
		
		if (!flowServiceState.getStatus().equals(FlowServiceState.Status.ALLOCATED)){
			//TODO, what to do?
			return;
		}
		
		try{
			CDAPMessage cdapMessage = CDAPMessage.getDeleteObjectRequestMessage(null, null, null, 0, null, 0);
			sendCDAPMessage(cdapMessage, flowServiceState.getSocket());
			flowServices.remove(new Integer(portId));
			ApplicationRegistrationState arState = applicationRegistrations.get(flowServiceState.getFlowService().getDestinationAPNamingInfo().getProcessKey());
			if (arState == null){
				//TODO what to do?
			}else{
				arState.getFlowServices().remove(flowServiceState);
			}
			if (flowServiceState.getSocket().isConnected()){
				flowServiceState.getSocket().close();
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	/**
	 * Send a message to the application library
	 * @param cdapMessage
	 * @param socket
	 */
	public synchronized void sendMessage(CDAPMessage cdapMessage, Socket socket){
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
	
	private String printStringList(List<String> list){
		String result = "";
		if (list == null){
			return "";
		}
		
		for(int i=0; i<list.size(); i++){
			result = result + list.get(i);
			if (i+1<list.size()){
				result = result + ", ";
			}
		}
		
		return result;
	}

}
