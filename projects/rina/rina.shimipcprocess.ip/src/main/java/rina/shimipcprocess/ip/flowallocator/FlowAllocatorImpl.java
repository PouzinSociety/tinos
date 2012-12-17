package rina.shimipcprocess.ip.flowallocator;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.cdap.api.message.CDAPMessage;
import rina.configuration.RINAConfiguration;
import rina.delimiting.api.Delimiter;
import rina.flowallocator.api.BaseFlowAllocator;
import rina.flowallocator.api.DirectoryForwardingTable;
import rina.flowallocator.api.QoSCube;
import rina.ipcmanager.api.IPCManager;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCException;
import rina.ipcservice.api.IPCService;
import rina.ipcservice.api.QualityOfServiceSpecification;
import rina.shimipcprocess.ip.BlockinqQueueReader;
import rina.shimipcprocess.ip.ShimIPCProcessForIPLayers;
import rina.shimipcprocess.ip.TCPServer;
import rina.shimipcprocess.ip.TCPSocketReader;
import rina.shimipcprocess.ip.UDPServer;
import rina.shimipcprocess.ip.UDPSocketReader;
import rina.shimipcprocess.ip.flowallocator.FlowState.State;

public class FlowAllocatorImpl extends BaseFlowAllocator{

	private static final Log log = LogFactory.getLog(FlowAllocatorImpl.class);

	public static final String SOCKET_NUMBER_ALREADY_RESERVED_FOR_ANOTHER_APPLICATION = "This socket number is already reserved by another application.";
	public static final String COULD_NOT_FIND_SOCKET_NUMBER_FOR_APPLICATION = "Could not find a socket number for this application.";
	public static final String APPLICATION_ALREADY_REGISTERED = "Application already registered.";
	public static final String APPLICATION_NOT_REGISTERED = "The application was not registered.";
	public static final int MAX_PACKETS_IN_UNRELIABLE_QUEUE = 1000;

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
	 * The unreliable flow queues, used to demultiplex 
	 * the different unreliable incoming flows to an application
	 */
	private Map<String, BlockingQueue<byte[]>> unreliableFlowQueues = null;

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

	/**
	 * The IPCManager
	 */
	private IPCManager ipcManager = null;

	/**
	 * The IPC Process
	 */
	private IPCProcess ipcProcess = null;

	/**
	 * The runnable that reads the incoming flow queues and 
	 * dispatches the SDUs following a certain QoS cryteria
	 */
	private OutgoingFlowQueuesReader incomingFlowQueuesReader = null;

	public FlowAllocatorImpl(String hostName, Delimiter delimiter, IPCManager ipcManager, ShimIPCProcessForIPLayers ipcProcess){
		this.hostName = hostName;
		this.expectedApplicationRegistrations = new ConcurrentHashMap<String, Integer>();
		this.registeredApplications = new ConcurrentHashMap<String, ApplicationRegistration>();
		this.directory = new ConcurrentHashMap<String, DirectoryEntry>();
		this.flows = new ConcurrentHashMap<Integer, FlowState>();
		this.unreliableFlowQueues = new ConcurrentHashMap<String, BlockingQueue<byte[]>>();
		this.delimiter = delimiter;
		this.timer = new Timer();
		this.ipcManager = ipcManager;
		this.ipcProcess = ipcProcess;
		this.incomingFlowQueuesReader = new OutgoingFlowQueuesReader(ipcManager, flows, delimiter);
		this.ipcManager.execute(incomingFlowQueuesReader);

		//Create QoS cubes
		this.qosCubes = new ArrayList<QoSCube>();
		QoSCube qosCube = new QoSCube();
		qosCube.setQosId(1);
		qosCube.setName("unreliable");
		qosCube.setOrder(false);
		qosCube.setPartialDelivery(true);
		qosCube.setUndetectedBitErrorRate(new Double("1.0E-9"));
		this.qosCubes.add(qosCube);
	    qosCube = new QoSCube();
		qosCube.setQosId(2);
		qosCube.setName("reliable");
		qosCube.setMaxAllowableGapSdu(0);
		qosCube.setOrder(true);
		qosCube.setPartialDelivery(false);
		qosCube.setUndetectedBitErrorRate(new Double("1.0E-9"));
		this.qosCubes.add(qosCube);
	}

	public void stop(){
		super.stop();
		this.incomingFlowQueuesReader.stop();
	}

	public List<QoSCube> getQoSCubes(){
		return this.qosCubes;
	}

	public Map<String, ApplicationRegistration> getRegisteredApplications(){
		return this.registeredApplications;
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

	public Map<Integer, FlowState> getFlows(){
		return this.flows;
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

		log.debug("Application "+apNamingInfo.getEncodedString()+" is assigned socket port number "+socketNumber.intValue());
		TCPServer tcpServer = new TCPServer(this.hostName, socketNumber.intValue(), applicationCallback, apNamingInfo, this);
		this.ipcManager.execute(tcpServer);
		UDPServer udpServer = new UDPServer(this.hostName, socketNumber.intValue(), this, applicationCallback, apNamingInfo);
		this.ipcManager.execute(udpServer);
		log.debug("Started TCP and UDP servers");

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

	public int submitAllocateRequest(FlowService flowService, APService applicationCallback) throws IPCException {
		log.debug("Requested new flow with the following characteristics:\n" + flowService.toString());

		//See if we have an entry for the destination application
		DirectoryEntry directoryEntry = this.directory.get(flowService.getDestinationAPNamingInfo().getEncodedString());
		if (directoryEntry == null){
			throw new IPCException(IPCException.COULD_NOT_FIND_ENTRY_IN_DIRECTORY_FORWARDING_TABLE_CODE, 
					IPCException.COULD_NOT_FIND_ENTRY_IN_DIRECTORY_FORWARDING_TABLE + 
					". Application: "+flowService.getDestinationAPNamingInfo().getEncodedString());
		}
		log.debug("Destination application available at "+directoryEntry.getHostname()+" port "+directoryEntry.getPortNumber());

		//Check the QoS requested for the flow
		boolean reliable = false;
		if (flowService.getQoSSpecification() != null && 
				(flowService.getQoSSpecification().getQosCubeId() == 2)){
			reliable = true;
		}

		FlowState flowState = null;
		int portId = -1;

		synchronized(this.flows){
			flowState = new FlowState();
			portId = this.ipcManager.getAvailablePortId();
			if (portId == -1){
				throw new IPCException(IPCException.PROBLEMS_ALLOCATING_FLOW_CODE, 
						IPCException.PROBLEMS_ALLOCATING_FLOW + 
						". No more available portIds");
			}
			this.flows.put(new Integer(portId), flowState);
		}

		log.debug("Assigned portId "+portId+" to the flow allocation");
		flowService.setPortId(portId);
		flowState.setFlowService(flowService);
		flowState.setApplicationCallback(applicationCallback);
		flowState.setPortId(portId);
		try{
			if (reliable){
				Socket socket = new Socket(directoryEntry.getHostname(), directoryEntry.getPortNumber());
				flowState.setSocket(socket);

				TCPSocketReader reader = new TCPSocketReader(socket, delimiter, ipcManager, portId, this);
				this.ipcManager.execute(reader);
				AllocateResponseTimerTask timerTask = new AllocateResponseTimerTask(applicationCallback, portId);
				timer.schedule(timerTask, 20);
			}else{
				DatagramSocket datagramSocket = new DatagramSocket(0, InetAddress.getByName(hostName));
				datagramSocket.connect(InetAddress.getByName(directoryEntry.getHostname()), directoryEntry.getPortNumber());
				flowState.setDatagramSocket(datagramSocket);

				UDPSocketReader reader = new UDPSocketReader(datagramSocket, ipcManager, portId, this);
				AllocateResponseTimerTask timerTask = new AllocateResponseTimerTask(applicationCallback, portId);
				this.ipcManager.execute(reader);
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
		int queueCapacity = 0;
		if (flowService.getDestinationAPNamingInfo().getApplicationEntityName() == null || 
			!flowService.getDestinationAPNamingInfo().getApplicationEntityName().equals(IPCService.MANAGEMENT_AE)){
			queueCapacity = RINAConfiguration.getInstance().getLocalConfiguration().getLengthOfFlowQueues();
		}
		this.ipcManager.addFlowQueues(portId, queueCapacity);
		this.ipcManager.getOutgoingFlowQueue(portId).subscribeToQueueReadyToBeReadEvents(this.incomingFlowQueuesReader);
		return portId;
	}

	/**
	 * Invoked by the TCP or UDP socket readers when they detect that a socket has been closed
	 */
	public void socketClosed(int portId){
		log.debug("The socket associated to the flow "+portId+" is closed");
		FlowState flowState = null;

		synchronized(this.flows){
			Integer iPortId = new Integer(portId);
			flowState = this.flows.remove(iPortId);
			this.ipcManager.removeFlowQueues(portId);
			this.ipcManager.freePortId(portId);
		}

		if (flowState != null){
			flowState.getApplicationCallback().deliverDeallocate(portId);
			if (flowState.getBlockingQueueId() != null){
				synchronized(this.unreliableFlowQueues){
					this.unreliableFlowQueues.remove(flowState.getBlockingQueueId());
				}
			}
		}
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
			portId = this.ipcManager.getAvailablePortId();
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

		log.debug("Got new flow request, assigned temporary portId: "+portId);
		FlowService flowService = new FlowService();
		QualityOfServiceSpecification qoSSpec = new QualityOfServiceSpecification();
		qoSSpec.setName("reliable");
		qoSSpec.setQosCubeId(2);
		qoSSpec.setMaxAllowableGapSDU(0);
		qoSSpec.setOrder(true);
		qoSSpec.setPartialDelivery(false);
		flowService.setQoSSpecification(qoSSpec);
		ApplicationProcessNamingInfo sourceAPNamingInfo = this.getAPNamingInfo(socket.getInetAddress().getHostName());
		if (sourceAPNamingInfo != null){
			flowService.setSourceAPNamingInfo(sourceAPNamingInfo);
		}else{
			flowService.setSourceAPNamingInfo(new ApplicationProcessNamingInfo(
					socket.getInetAddress().getHostName(), ""+socket.getPort()));
		}
		flowService.setDestinationAPNamingInfo(apNamingInfo);
		flowService.setPortId(portId);
		flowState.setFlowService(flowService);
		flowState.setSocket(socket);
		flowState.setPortId(portId);
		flowState.setApplicationCallback(applicationCallback);
		flowState.setState(State.ALLOCATION_REQUESTED);

		log.debug("Delivering the allocate request to local application: "+apNamingInfo.getEncodedString());
		applicationCallback.deliverAllocateRequest(flowService, (IPCService) this.ipcProcess);
	}

	private ApplicationProcessNamingInfo getAPNamingInfo(String hostName){
		Iterator<DirectoryEntry> iterator = this.directory.values().iterator();
		DirectoryEntry currentEntry = null;
		while(iterator.hasNext()){
			currentEntry = iterator.next();
			if (currentEntry.getHostname().equals(hostName)){
				return currentEntry.getApNamingInfo();
			}
		}

		return null;
	}

	/**
	 * Called by the UDP server when a datagram has been received
	 * @param datagram
	 * @param localSocketNumber
	 * @param applicationCallback
	 * @param apNamingInfo
	 * @param datagramSocket
	 */
	public void datagramReceived(DatagramPacket datagram, int localSocketNumber, APService applicationCallback, 
			ApplicationProcessNamingInfo apNamingInfo, DatagramSocket datagramSocket){
		String queueId = datagram.getAddress().getHostAddress()+"-"+datagram.getPort()+"-"+localSocketNumber;

		//Check if there's a queue for this id
		BlockingQueue<byte[]> queue = this.unreliableFlowQueues.get(queueId);
		if (queue != null){
			try{
				byte[] sdu = new byte[datagram.getLength()];
				System.arraycopy(datagram.getData(), 
						datagram.getOffset(), sdu, 0, datagram.getLength());
				queue.put(sdu);
			}catch(Exception ex){
				log.error(ex.getMessage());
			}

			return;
		}

		//There's no queue, a new flow is being requested
		FlowState flowState = null;
		int portId = 0;
		synchronized(this.flows){
			flowState = new FlowState();
			portId = this.ipcManager.getAvailablePortId();
			//Not enough portIds, cannot accept the request
			if (portId == -1){
				try{
					log.warn("Ignoring flow since there are not enough portIds available");
					return;
				}catch(Exception ex){
				}
			}
			this.flows.put(new Integer(portId), flowState);
		}

		log.debug("Got new flow request, assigned temporary portId: "+portId);
		FlowService flowService = new FlowService();
		QualityOfServiceSpecification qoSSpec = new QualityOfServiceSpecification();
		qoSSpec.setName("unreliable");
		qoSSpec.setQosCubeId(1);
		flowService.setQoSSpecification(qoSSpec);
		flowService.setSourceAPNamingInfo(new ApplicationProcessNamingInfo(
				datagram.getAddress().getHostAddress(), ""+datagram.getPort()));
		flowService.setDestinationAPNamingInfo(apNamingInfo);
		flowService.setPortId(portId);
		flowState.setFlowService(flowService);
		flowState.setDatagramSocket(datagramSocket);
		flowState.setPortId(portId);
		flowState.setApplicationCallback(applicationCallback);
		flowState.setState(State.ALLOCATION_REQUESTED);

		//Create the new queue
		queue = new ArrayBlockingQueue<byte[]>(MAX_PACKETS_IN_UNRELIABLE_QUEUE);
		synchronized(this.unreliableFlowQueues){
			this.unreliableFlowQueues.put(queueId, queue);
		}
		flowState.setBlockingQueueId(queueId);

		//Store the first packet at the queue
		try{
			byte[] sdu = new byte[datagram.getLength()];
			System.arraycopy(datagram.getData(), 
					datagram.getOffset(), sdu, 0, datagram.getLength());
			queue.put(sdu);
		}catch(Exception ex){
			log.error(ex.getMessage());
		}

		//Call the local application
		log.debug("Delivering the allocate request to local application: "+apNamingInfo.getEncodedString());
		applicationCallback.deliverAllocateRequest(flowService, (IPCService) this.ipcProcess);
	}

	public void submitAllocateResponse(int portId, boolean success, String reason, APService applicationCallback) throws IPCException {
		log.debug("Local application invoked allocateResponse for the flow at portId "+portId);
		FlowState flowState = null;
		synchronized(this.flows){
			flowState = this.flows.get(new Integer(portId));
		}

		if (flowState == null || (flowState.getState() != State.ALLOCATION_REQUESTED)){
			throw new IPCException(IPCException.PROBLEMS_ALLOCATING_FLOW_CODE, 
					IPCException.PROBLEMS_ALLOCATING_FLOW + ". Flow state not consistent with allocate response call");
		}

		if (!success){
			log.debug("The flow allocation has been denied because: "+reason);
			this.submitDeallocate(portId);
			return;
		}

		log.debug("Flow allocation accepted");
		if (flowState.getSocket() != null){
			submitAllocateResponseForReliableFlow(flowState.getSocket(), applicationCallback, portId);
		}else{
			submitAllocateResponseForUnreliableFlow(flowState, applicationCallback, portId);
		}

		flowState.setApplicationCallback(applicationCallback);
		flowState.setState(State.ALLOCATED);
		int queueCapacity = 0;
		if (flowState.getFlowService().getDestinationAPNamingInfo().getApplicationEntityName() == null || 
			!flowState.getFlowService().getDestinationAPNamingInfo().getApplicationEntityName().equals(IPCService.MANAGEMENT_AE)){
			queueCapacity = RINAConfiguration.getInstance().getLocalConfiguration().getLengthOfFlowQueues();
		}
		this.ipcManager.addFlowQueues(portId, queueCapacity);
		this.ipcManager.getOutgoingFlowQueue(portId).subscribeToQueueReadyToBeReadEvents(this.incomingFlowQueuesReader);
	}

	private void submitAllocateResponseForReliableFlow(Socket socket, APService applicationCallback, int portId){
		TCPSocketReader reader = new TCPSocketReader(socket, delimiter, ipcManager, portId, this);
		this.ipcManager.execute(reader);
	}

	private void submitAllocateResponseForUnreliableFlow(FlowState flowState, APService applicationCallback, int portId){
		BlockinqQueueReader reader = new BlockinqQueueReader(
				this.unreliableFlowQueues.get(flowState.getBlockingQueueId()), 
				ipcManager, portId, this);
		this.ipcManager.execute(reader);
		flowState.setBlockingQueueReader(reader);
	}

	/**
	 * Remove the flow state and deallocate 
	 * the flow
	 */
	public void submitDeallocate(int portId) throws IPCException {
		FlowState flowState = null;

		synchronized(this.flows){
			Integer iPortId = new Integer(portId);
			flowState = this.flows.remove(iPortId);
			this.ipcManager.removeFlowQueues(portId);
			this.ipcManager.freePortId(portId);
		}

		if (flowState != null){
			try{
				if (flowState.getSocket() != null){
					flowState.getSocket().close();
				}else if (flowState.getBlockingQueueReader() != null){
					flowState.getBlockingQueueReader().stop();
					synchronized(this.unreliableFlowQueues){
						this.unreliableFlowQueues.remove(flowState.getBlockingQueueId());
					}
				}else if (flowState.getDatagramSocket() != null){
					flowState.getDatagramSocket().close();
				}
			}catch(Exception ex){
			}
		}
	}

	public void createFlowRequestMessageReceived(CDAPMessage cdapMessage, int arg1) {
		//Won't implement
	}

	public DirectoryForwardingTable getDirectoryForwardingTable() {
		// Won't implement
		return null;
	}

	public void receivedLocalFlowResponse(int arg0, int arg1, boolean arg2, String arg3) throws IPCException {
		//Won't implement
	}

	public void receivedLocalFlowRequest(FlowService arg0) throws IPCException {
		//Won't implement
	}

	public void removeFlowAllocatorInstance(int arg0) {
		//Won't implement
	}

	public void receivedDeallocateLocalFlowRequest(int arg0) throws IPCException {
		//Won't implement
	}
}