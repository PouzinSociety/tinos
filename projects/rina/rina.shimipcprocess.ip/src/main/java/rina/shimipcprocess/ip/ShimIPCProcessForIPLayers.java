package rina.shimipcprocess.ip;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.applicationprocess.api.WhatevercastName;
import rina.efcp.api.DataTransferConstants;
import rina.enrollment.api.Neighbor;
import rina.flowallocator.api.Flow;
import rina.flowallocator.api.QoSCube;
import rina.ipcmanager.api.IPCManager;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcprocess.api.IPCProcessComponent;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCException;
import rina.ipcservice.api.IPCService;

public class ShimIPCProcessForIPLayers implements IPCProcess, IPCService{
	
	public static final String SOCKET_NUMBER_ALREADY_RESERVED_FOR_ANOTHER_APPLICATION = "This socket number is already reserved by another application.";
	public static final String COULD_NOT_FIND_SOCKET_NUMBER_FOR_APPLICATION = "Could not find a socket number for this application.";
	public static final String APPLICATION_ALREADY_REGISTERED = "Application already registered.";
	public static final String APPLICATION_NOT_REGISTERED = "The application was not registered.";

	/**
	 * The IPC Manager
	 */
	private IPCManager ipcManager = null;
	
	/**
	 * The AP name and AP instance of this shim IPC Process
	 */
	private ApplicationProcessNamingInfo apNamingInfo = null;
	
	/**
	 * The hostName associated to the IP address (or the IP address) this shim IPC Process 
	 * is bounded to
	 */
	private String hostName = null;
	
	/**
	 * The name of the DIF this shim IPC Process is part of
	 */
	private String difName = null;
	
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
	 * The list of QoS cubes
	 */
	private List<QoSCube> qosCubes = null;
	
	public ShimIPCProcessForIPLayers(ApplicationProcessNamingInfo apNamingInfo, String hostName, String difName){
		this.apNamingInfo = apNamingInfo;
		this.hostName = hostName;
		this.difName = difName;
		this.expectedApplicationRegistrations = new ConcurrentHashMap<String, Integer>();
		this.registeredApplications = new ConcurrentHashMap<String, ApplicationRegistration>();
		this.directory = new ConcurrentHashMap<String, DirectoryEntry>();
		
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

	@Override
	/**
	 * Register an application to the shim IPC Process. It will cause the shim IPC Process to 
	 * listen to a certain TCP and UDP port number.
	 * @param apNamingInfo The naming information of the IPC Process to register
	 */
	public void register(ApplicationProcessNamingInfo apNamingInfo) throws IPCException{
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
		
		TCPServer tcpServer = new TCPServer(this.hostName, socketNumber.intValue(), this);
		this.execute(tcpServer);
		UDPServer udpServer = new UDPServer(this.hostName, socketNumber.intValue(), this);
		this.execute(udpServer);
		
		ApplicationRegistration apRegistration = new ApplicationRegistration();
		apRegistration.setApNamingInfo(apNamingInfo);
		apRegistration.setPortNumber(socketNumber.intValue());
		apRegistration.setTcpServer(tcpServer);
		apRegistration.setUdpServer(udpServer);
		synchronized(this.registeredApplications){
			this.registeredApplications.put(apNamingInfo.getEncodedString(), apRegistration);
		}
	}
	
	@Override
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
	/**
	 * Invoked by a local application to request a new flow to be allocated
	 * @param flowService Specifies the allocation information request
	 */
	public int submitAllocateRequest(FlowService flowService) throws IPCException {
		DirectoryEntry directoryEntry = this.directory.get(flowService.getDestinationAPNamingInfo().getEncodedString());
		if (directoryEntry == null){
			throw new IPCException(IPCException.COULD_NOT_FIND_ENTRY_IN_DIRECTORY_FORWARDING_TABLE_CODE, 
					IPCException.COULD_NOT_FIND_ENTRY_IN_DIRECTORY_FORWARDING_TABLE + 
					". Application: "+flowService.getDestinationAPNamingInfo().getEncodedString());
		}
		
		
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void submitAllocateResponse(int arg0, boolean arg1, String arg2)
			throws IPCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void submitDeallocate(int arg0) throws IPCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void submitStatus(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void submitTransfer(int arg0, byte[] arg1) throws IPCException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void addIPCProcessComponent(IPCProcessComponent ipcProcessComponent) {
	}

	@Override
	public void destroy() {
		//TODO
	}

	@Override
	public void execute(Runnable runnable) {
		this.ipcManager.execute(runnable);
	}

	@Override
	public APService getAPService() {
		return this.ipcManager.getAPService();
	}

	@Override
	public Long getAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Flow> getAllocatedFlows() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getApplicationProcessInstance() {
		return this.apNamingInfo.getApplicationProcessInstance();
	}

	@Override
	public String getApplicationProcessName() {
		return this.apNamingInfo.getApplicationProcessName();
	}

	@Override
	public ApplicationProcessNamingInfo getApplicationProcessNamingInfo() {
		return this.getApplicationProcessNamingInfo();
	}

	@Override
	public String getDIFName() {
		return this.difName;
	}

	@Override
	public DataTransferConstants getDataTransferConstants() {
		return null;
	}

	@Override
	public IPCManager getIPCManager() {
		return this.ipcManager;
	}

	@Override
	public IPCProcessComponent getIPCProcessComponent(String componentName) {
		return null;
	}

	@Override
	public Map<String, IPCProcessComponent> getIPCProcessComponents() {
		return null;
	}

	@Override
	public List<Neighbor> getNeighbors() {
		return null;
	}

	@Override
	public OperationalStatus getOperationalStatus() {
		return null;
	}

	@Override
	public List<QoSCube> getQoSCubes() {
		return this.qosCubes;
	}

	@Override
	public List<WhatevercastName> getWhatevercastNames() {
		return null;
	}

	@Override
	public IPCProcessComponent removeIPCProcessComponent(String componentName) {
		return null;
	}

	@Override
	public void setIPCManager(IPCManager ipcManager) {
		this.ipcManager = ipcManager;
	}

	@Override
	public void setIPCProcessCompnents(Map<String, IPCProcessComponent> arg0) {
	}

}
