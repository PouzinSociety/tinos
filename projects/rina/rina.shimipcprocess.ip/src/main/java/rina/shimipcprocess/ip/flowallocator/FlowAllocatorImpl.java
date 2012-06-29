package rina.shimipcprocess.ip.flowallocator;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.cdap.api.message.CDAPMessage;
import rina.delimiting.api.BaseDelimiter;
import rina.delimiting.api.Delimiter;
import rina.flowallocator.api.BaseFlowAllocator;
import rina.flowallocator.api.DirectoryForwardingTable;
import rina.flowallocator.api.QoSCube;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCException;
import rina.ipcservice.api.IPCService;
import rina.ipcservice.api.QualityOfServiceSpecification;
import rina.shimipcprocess.ip.TCPServer;
import rina.shimipcprocess.ip.TCPSocketReader;
import rina.shimipcprocess.ip.UDPServer;
import rina.shimipcprocess.ip.UDPSocketReader;
import rina.shimipcprocess.ip.flowallocator.FlowState.State;

public class FlowAllocatorImpl extends BaseFlowAllocator{
	
	public static final String SOCKET_NUMBER_ALREADY_RESERVED_FOR_ANOTHER_APPLICATION = "This socket number is already reserved by another application.";
	public static final String COULD_NOT_FIND_SOCKET_NUMBER_FOR_APPLICATION = "Could not find a socket number for this application.";
	public static final String APPLICATION_ALREADY_REGISTERED = "Application already registered.";
	public static final String APPLICATION_NOT_REGISTERED = "The application was not registered.";
	
	/**
	 * The expected application registrations (map app name to socket number)
	 */
	private Map<String, Integer> expectedApplicationRegistrations = null;
	
	/**
	 * The list of registered applications
	 */
	private Map<String, ApplicationRegistration> registeredApplications = null;
	
	/**
	 * The directory to be used for flow allocation
	 */
	private Map<String, DirectoryEntry> directory = null;
	
	/**
	 * The state of the flows currently allocated or with 
	 * allocation in process
	 */
	private Map<Integer, FlowState> flows = null;
	
	/**
	 * The list of QoS cubes
	 */
	private List<QoSCube> qosCubes = null;
	
	/**
	 * The hostname
	 */
	private String hostName = null;
	
	/**
	 * The delimiter instance
	 */
	private Delimiter delimiter = null;
	
	/**
	 * Timer that controls several timertasks
	 */
	private Timer timer = null;
	
	public FlowAllocatorImpl(String hostName){
		this.hostName = hostName;
		this.expectedApplicationRegistrations = new ConcurrentHashMap<String, Integer>();
		this.registeredApplications = new ConcurrentHashMap<String, ApplicationRegistration>();
		this.directory = new ConcurrentHashMap<String, DirectoryEntry>();
		this.flows = new ConcurrentHashMap<Integer, FlowState>();
		this.delimiter = (Delimiter) this.getIPCProcess().getIPCProcessComponent(BaseDelimiter.getComponentName());
		this.timer = new Timer();
		
		//Create QoS cubes
		this.qosCubes = new ArrayList<QoSCube>();
		QoSCube qosCube = new QoSCube();
		qosCube.setQosId(1);
		qosCube.setName("reliable");
		qosCube.setMaxAllowableGapSdu(0);
		qosCube.setOrder(true);
		qosCube.setPartialDelivery(false);
		qosCube.setUndetectedBitErrorRate(new Double("1.0E-9"));
		this.qosCubes.add(qosCube);
		qosCube = new QoSCube();
		qosCube.setQosId(2);
		qosCube.setName("unreliable");
		qosCube.setOrder(false);
		qosCube.setPartialDelivery(true);
		qosCube.setUndetectedBitErrorRate(new Double("1.0E-9"));
		this.qosCubes.add(qosCube);
	}
	
	public List<QoSCube> getQoSCubes(){
		return this.getQoSCubes();
	}
	
	/**
	 * Reserve a socket number for a certain application
	 * @param apNamingInfo the naming information (AP name, AP instance) of the application
	 * @param socketNumber the socket number to reserve
	 * @throws IPCException
	 */
	public void addExpectedApplicationRegistration(ApplicationProcessNamingInfo apNamingInfo, int socketNumber) throws IPCException{
		if (this.expectedApplicationRegistrations.containsKey(apNamingInfo.getEncodedString())){
			throw new IPCException(IPCException.ERROR_CODE, 
					SOCKET_NUMBER_ALREADY_RESERVED_FOR_ANOTHER_APPLICATION + " Socket number: "+socketNumber);
		}
		
		synchronized(this.expectedApplicationRegistrations){
			this.expectedApplicationRegistrations.put(apNamingInfo.getEncodedString(), socketNumber);
		}
	}
	
	public Map<String, Integer> getExpectedApplicationRegistrations(){
		return this.expectedApplicationRegistrations;
	}
	
	/**
	 * Add an entry to the directory or modify it if it exists
	 * @param apNamingInfo
	 * @param hostName
	 * @param portNumber
	 * @throws IPCException
	 */
	public void addOrModifyDirectoryEntry(ApplicationProcessNamingInfo apNamingInfo, String hostName, int portNumber) throws IPCException{
		synchronized (this.directory){
			DirectoryEntry entry = this.directory.get(apNamingInfo.getEncodedString());
			if (entry != null){
				entry.setHostname(hostName);
				entry.setPortNumber(portNumber);
			}else{
				entry = new DirectoryEntry();
				entry.setApNamingInfo(apNamingInfo);
				entry.setHostname(hostName);
				entry.setPortNumber(portNumber);
				directory.put(apNamingInfo.getEncodedString(), entry);
			}
		}
	}
	
	public Map<String, DirectoryEntry> getDirectory(){
		return this.directory;
	}
	
	/**
	 * Register an application to the shim IPC Process. It will cause the shim IPC Process to 
	 * listen to a certain TCP and UDP port number.
	 * @param apNamingInfo The naming information of the IPC Process to register
	 * @param applicationCallback the callback to contact the application in case of incoming flow requests
	 */
	public void register(ApplicationProcessNamingInfo apNamingInfo, APService applicationCallback) throws IPCException{
		Integer socketNumber = this.expectedApplicationRegistrations.get(apNamingInfo.getEncodedString());
		if (socketNumber == null){
			throw new IPCException(IPCException.ERROR_CODE, 
					COULD_NOT_FIND_SOCKET_NUMBER_FOR_APPLICATION + " Application naming info: "+ 
					apNamingInfo.getEncodedString());
		}
		
		if (this.registeredApplications.get(apNamingInfo.getEncodedString()) != null){
			throw new IPCException(IPCException.ERROR_CODE, 
					APPLICATION_ALREADY_REGISTERED + " Application naming info: "+ 
					apNamingInfo.getEncodedString());
		}
		
		TCPServer tcpServer = new TCPServer(this.hostName, socketNumber.intValue(), applicationCallback, apNamingInfo, this);
		this.getIPCProcess().execute(tcpServer);
		UDPServer udpServer = new UDPServer(this.hostName, socketNumber.intValue(), this);
		this.getIPCProcess().execute(udpServer);
		
		ApplicationRegistration apRegistration = new ApplicationRegistration();
		apRegistration.setApNamingInfo(apNamingInfo);
		apRegistration.setApplicationCallback(applicationCallback);
		apRegistration.setPortNumber(socketNumber.intValue());
		apRegistration.setTcpServer(tcpServer);
		apRegistration.setUdpServer(udpServer);
		synchronized(this.registeredApplications){
			this.registeredApplications.put(apNamingInfo.getEncodedString(), apRegistration);
		}
	}
	
	/**
	 * Unregisters a local application from this shim IPC Process. The shim will stop 
	 * listening at TCP and UDP ports.
	 * @param apNamingInfo the application to be unregistered
	 */
	public void unregister(ApplicationProcessNamingInfo apNamingInfo) throws IPCException{
		ApplicationRegistration apRegistration = null;
		synchronized(this.registeredApplications){
			apRegistration = this.registeredApplications.remove(apNamingInfo.getEncodedString());
		}
		
		if (apRegistration == null){
			throw new IPCException(IPCException.ERROR_CODE, 
					APPLICATION_NOT_REGISTERED + " Application naming info: "+ 
					apNamingInfo.getEncodedString());
		}
		
		apRegistration.getTcpServer().setEnd(true);
		apRegistration.getUdpServer().setEnd(true);
	}
	
	@Override
	public int submitAllocateRequest(FlowService flowService, APService applicationCallback) throws IPCException {
		//See if we have an entry for the destination application
		DirectoryEntry directoryEntry = this.directory.get(flowService.getDestinationAPNamingInfo().getEncodedString());
		if (directoryEntry == null){
			throw new IPCException(IPCException.COULD_NOT_FIND_ENTRY_IN_DIRECTORY_FORWARDING_TABLE_CODE, 
					IPCException.COULD_NOT_FIND_ENTRY_IN_DIRECTORY_FORWARDING_TABLE + 
					". Application: "+flowService.getDestinationAPNamingInfo().getEncodedString());
		}
		
		//Check the QoS requested for the flow
		boolean reliable = false;
		if (flowService.getQoSSpecification() != null && 
				(flowService.getQoSSpecification().getQosCubeId() == 1)){
			reliable = true;
		}
		
		FlowState flowState = null;
		int portId = -1;
		
		synchronized(this.flows){
			flowState = new FlowState();
			portId = generatePortId();
			if (portId == -1){
				throw new IPCException(IPCException.PROBLEMS_ALLOCATING_FLOW_CODE, 
						IPCException.PROBLEMS_ALLOCATING_FLOW + 
						". No more available portIds");
			}
			this.flows.put(new Integer(portId), flowState);
		}
		
		flowState.setFlowService(flowService);
		flowState.setApplicationCallback(applicationCallback);
		flowState.setPortId(portId);
		try{
			if (reliable){
				Socket socket = new Socket(directoryEntry.getHostname(), directoryEntry.getPortNumber());
				flowState.setSocket(socket);

				TCPSocketReader reader = new TCPSocketReader(socket, delimiter, applicationCallback, portId, this);
				AllocateResponseTimerTask timerTask = new AllocateResponseTimerTask(applicationCallback, 
						this.getIPCProcess(), reader, null, portId);
				timer.schedule(timerTask, 20);
			}else{
				DatagramSocket datagramSocket = new DatagramSocket(0, InetAddress.getByName(hostName));
				datagramSocket.connect(InetAddress.getByName(directoryEntry.getHostname()), directoryEntry.getPortNumber());
				flowState.setDatagramSocket(datagramSocket);
				
				UDPSocketReader reader = new UDPSocketReader(datagramSocket, applicationCallback, portId, this);
				AllocateResponseTimerTask timerTask = new AllocateResponseTimerTask(applicationCallback, 
						this.getIPCProcess(), null, reader, portId);
				timer.schedule(timerTask, 20);
			}
		}catch(Exception ex){
			synchronized(this.flows){
				this.flows.remove(new Integer(portId));
			}
			throw new IPCException(IPCException.PROBLEMS_ALLOCATING_FLOW_CODE, 
					IPCException.PROBLEMS_ALLOCATING_FLOW + ex.getMessage());
		}
		
		flowState.setState(State.ALLOCATED);
		return portId;
	}
	
	/**
	 * Invoked by the TCP or UDP socket readers when they detect that a socket has been closed
	 */
	public void socketClosed(int portId){
		FlowState flowState = null;
		
		synchronized(this.flows){
			flowState = this.flows.remove(new Integer(portId));
		}
		
		if (flowState != null){
			flowState.getApplicationCallback().deliverDeallocate(portId);
		}
	}
	
	private int generatePortId(){
		int i=1;
		while (i<Integer.MAX_VALUE){
			if (this.flows.get(new Integer(i)) == null){
				return i;
			}

			i++;
		}

		return -1;
	}
	
	/**
	 * Called when the TCP Server detects a new TCP connection
	 * @param socket
	 * @param applicationCallback
	 * @param apNamingInfo
	 */
	public void newConnectionAccepted(Socket socket, APService applicationCallback, 
			ApplicationProcessNamingInfo apNamingInfo){
		FlowState flowState = null;
		int portId = -1;
		
		synchronized(this.flows){
			flowState = new FlowState();
			portId = generatePortId();
			//Not enough portIds, cannot accept the request
			if (portId == -1){
				try{
					socket.close();
					return;
				}catch(Exception ex){
				}
			}
			this.flows.put(new Integer(portId), flowState);
		}
		
		FlowService flowService = new FlowService();
		QualityOfServiceSpecification qoSSpec = new QualityOfServiceSpecification();
		qoSSpec.setName("reliable");
		qoSSpec.setQosCubeId(1);
		flowService.setQoSSpecification(qoSSpec);
		flowService.setSourceAPNamingInfo(new ApplicationProcessNamingInfo(
				socket.getInetAddress().getHostName(), ""+socket.getPort()));
		flowService.setDestinationAPNamingInfo(apNamingInfo);
		flowState.setFlowService(flowService);
		flowState.setSocket(socket);
		flowState.setPortId(portId);
		flowState.setApplicationCallback(applicationCallback);
		flowState.setState(State.ALLOCATION_REQUESTED);
		
		applicationCallback.deliverAllocateRequest(flowService, (IPCService) this.getIPCProcess());
	}

	@Override
	public void submitAllocateResponse(int portId, boolean success, String reason, APService applicationCallback) throws IPCException {
		FlowState flowState = null;
		synchronized(this.flows){
			flowState = this.flows.get(new Integer(portId));
		}
		
		if (flowState == null || (flowState.getState() != State.ALLOCATION_REQUESTED)){
			throw new IPCException(IPCException.PROBLEMS_ALLOCATING_FLOW_CODE, 
					IPCException.PROBLEMS_ALLOCATING_FLOW + ". Flow state not consistent with allocate response call");
		}
		
		if (!success){
			this.submitDeallocate(portId);
			return;
		}
		
		if (flowState.getSocket() != null){
			TCPSocketReader reader = new TCPSocketReader(flowState.getSocket(), delimiter, applicationCallback, portId, this);
			AllocateResponseTimerTask timerTask = new AllocateResponseTimerTask(applicationCallback, 
					this.getIPCProcess(), reader, null, portId);
			timer.schedule(timerTask, 20);
		}else{
			UDPSocketReader reader = new UDPSocketReader(flowState.getDatagramSocket(), applicationCallback, portId, this);
			AllocateResponseTimerTask timerTask = new AllocateResponseTimerTask(applicationCallback, 
					this.getIPCProcess(), null, reader, portId);
			timer.schedule(timerTask, 20);
		}
		
		flowState.setState(State.ALLOCATED);
	}

	@Override
	/**
	 * Remove the flow state and deallocate 
	 * the flow
	 */
	public void submitDeallocate(int portId) throws IPCException {
		FlowState flowState = null;
		
		synchronized(this.flows){
			flowState = this.flows.remove(new Integer(portId));
		}
		
		if (flowState != null){
			try{
				if (flowState.getSocket() != null){
					flowState.getSocket().close();
				}else if (flowState.getDatagramSocket() != null){
					flowState.getDatagramSocket().close();
				}
			}catch(Exception ex){
			}
		}
	}
	
	public void submitTransfer(int portId, byte[] sdu) throws IPCException {
		FlowState flowState = null;
		
		synchronized(this.flows){
			flowState = this.flows.get(new Integer(portId));
		}
		
		if (flowState == null){
			throw new IPCException(IPCException.PROBLEMS_SENDING_SDU_CODE, 
					IPCException.PROBLEMS_SENDING_SDU + ". Could not find state associated to portId "+portId);
		}
		
		try{
			if (flowState.getSocket() != null){
				flowState.getSocket().getOutputStream().write(sdu);
			}else if (flowState.getDatagramSocket() != null){
				//TODO flowState.getDatagramSocket().send()
			}
		}catch(Exception ex){
			throw new IPCException(IPCException.PROBLEMS_SENDING_SDU_CODE, 
					IPCException.PROBLEMS_SENDING_SDU + ex.getMessage());
		}
	}
	
	@Override
	public void createFlowRequestMessageReceived(CDAPMessage cdapMessage, int arg1) {
		//Won't implement
	}

	@Override
	public DirectoryForwardingTable getDirectoryForwardingTable() {
		// Won't implement
		return null;
	}
	
	@Override
	public void receivedLocalFlowResponse(int arg0, int arg1, boolean arg2, String arg3) throws IPCException {
		//Won't implement
	}
	
	@Override
	public void receivedLocalFlowRequest(FlowService arg0, String arg1) throws IPCException {
		//Won't implement
	}
	
	@Override
	public void removeFlowAllocatorInstance(int arg0) {
		//Won't implement
	}
	
	@Override
	public void receivedDeallocateLocalFlowRequest(int arg0) throws IPCException {
		//Won't implement
	}
	
	@Override
	public void newConnectionAccepted(Socket socket){
		//Won't implement
	}
}