package rina.shimipcprocess.ip;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.applicationprocess.api.WhatevercastName;
import rina.delimiting.api.Delimiter;
import rina.efcp.api.DataTransferConstants;
import rina.enrollment.api.Neighbor;
import rina.flowallocator.api.Flow;
import rina.flowallocator.api.QoSCube;
import rina.ipcmanager.api.IPCManager;
import rina.ipcprocess.api.BaseIPCProcess;
import rina.ipcprocess.api.IPCProcessComponent;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCException;
import rina.ipcservice.api.IPCService;
import rina.ribdaemon.api.RIBDaemon;
import rina.shimipcprocess.ip.flowallocator.ApplicationRegistration;
import rina.shimipcprocess.ip.flowallocator.DirectoryEntry;
import rina.shimipcprocess.ip.flowallocator.FlowAllocatorImpl;
import rina.shimipcprocess.ip.ribdaemon.RIBDaemonImpl;

public class ShimIPCProcessForIPLayers extends BaseIPCProcess implements IPCService{

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
	 * The Flow allocator
	 */
	private FlowAllocatorImpl flowAllocator = null;
	
	/**
	 * The RIB Daemon
	 */
	private RIBDaemon ribDaemon = null;
	
	public ShimIPCProcessForIPLayers(ApplicationProcessNamingInfo apNamingInfo, String hostName, String difName, Delimiter delimiter, IPCManager ipcManager){
		super();
		this.apNamingInfo = apNamingInfo;
		this.hostName = hostName;
		this.difName = difName;
		this.addIPCProcessComponent(delimiter);
		this.flowAllocator = new FlowAllocatorImpl(hostName, delimiter, ipcManager, this);
		this.addIPCProcessComponent(this.flowAllocator);
		this.flowAllocator.setIPCProcess(this);
		this.ribDaemon = new RIBDaemonImpl(this, flowAllocator);
		this.addIPCProcessComponent(ribDaemon);
		this.ribDaemon.setIPCProcess(this);
	}
	
	public String getHostname(){
		return this.hostName;
	}
	
	@Override
	public IPCProcessComponent getIPCProcessComponent(String componentName){
		return this.ribDaemon;
	}
	
	/**
	 * Reserve a socket number for a certain application
	 * @param apNamingInfo the naming information (AP name, AP instance) of the application
	 * @param socketNumber the socket number to reserve
	 * @throws IPCException
	 */
	public void addExpectedApplicationRegistration(ApplicationProcessNamingInfo apNamingInfo, int socketNumber) throws IPCException{
		this.flowAllocator.addExpectedApplicationRegistration(apNamingInfo, socketNumber);
	}
	
	public Map<String, Integer> getExpectedApplicationRegistrations(){
		return this.flowAllocator.getExpectedApplicationRegistrations();
	}
	
	/**
	 * Add an entry to the directory or modify it if it exists
	 * @param apNamingInfo
	 * @param hostName
	 * @param portNumber
	 * @throws IPCException
	 */
	public void addOrModifyDirectoryEntry(ApplicationProcessNamingInfo apNamingInfo, String hostName, int portNumber) throws IPCException{
		this.flowAllocator.addOrModifyDirectoryEntry(apNamingInfo, hostName, portNumber);
	}
	
	public Map<String, DirectoryEntry> getDirectory(){
		return this.flowAllocator.getDirectory();
	}

	/**
	 * Register an application to the shim IPC Process. It will cause the shim IPC Process to 
	 * listen to a certain TCP and UDP port number.
	 * @param apNamingInfo The naming information of the IPC Process to register
	 * @param applicationCallback the callback to contact the application in case of incoming flow requests
	 */
	public void register(ApplicationProcessNamingInfo apNamingInfo, APService applicationCallback) throws IPCException{
		this.flowAllocator.register(apNamingInfo, applicationCallback);
	}
	
	/**
	 * Unregisters a local application from this shim IPC Process. The shim will stop 
	 * listening at TCP and UDP ports.
	 * @param apNamingInfo the application to be unregistered
	 */
	public void unregister(ApplicationProcessNamingInfo apNamingInfo) throws IPCException{
		this.flowAllocator.unregister(apNamingInfo);
	}

	/**
	 * Invoked by a local application to request a new flow to be allocated
	 * @param flowService Specifies the allocation information request
	 * @param applicationCallback the callback to contact the application in case of incoming flow requests
	 */
	public int submitAllocateRequest(FlowService flowService, APService applicationCallback) throws IPCException {
		return this.flowAllocator.submitAllocateRequest(flowService, applicationCallback);
	}

	public void submitAllocateResponse(int portId, boolean successs, String reason, APService applicationCallback) throws IPCException {
		this.flowAllocator.submitAllocateResponse(portId, successs, reason, applicationCallback);
	}

	public void submitDeallocate(int portId) throws IPCException {
		this.flowAllocator.submitDeallocate(portId);
	}

	public void submitStatus(int arg0) {
	}

	public void submitTransfer(int portId, byte[] sdu) throws IPCException {
		this.flowAllocator.submitTransfer(portId, sdu);
	}
	
	public void addIPCProcessComponent(IPCProcessComponent ipcProcessComponent) {
	}

	public void destroy() {
		Iterator<ApplicationRegistration> registrations = this.flowAllocator.getRegisteredApplications().values().iterator();
		while(registrations.hasNext()){
			try {
				this.flowAllocator.unregister(registrations.next().getApNamingInfo());
			} catch (IPCException e) {
				e.printStackTrace();
			}
		}
	}

	public void execute(Runnable runnable) {
		this.ipcManager.execute(runnable);
	}

	public Long getAddress() {
		byte[] encodedAddress = null;
		long result = 0L;
		try{
			encodedAddress = InetAddress.getByName(this.hostName).getAddress();
			result = (encodedAddress[0] & 0xFFL) << 24 | (encodedAddress[1] & 0xFFL) << 16 | 
						(encodedAddress[2] & 0xFFL) << 8 | (encodedAddress[3] & 0xFFL);
		}catch(Exception ex){
			return null;
		}
		
		
		return new Long(result);
	}

	public List<Flow> getAllocatedFlows() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getApplicationProcessInstance() {
		return this.apNamingInfo.getApplicationProcessInstance();
	}

	public String getApplicationProcessName() {
		return this.apNamingInfo.getApplicationProcessName();
	}

	public ApplicationProcessNamingInfo getApplicationProcessNamingInfo() {
		return this.apNamingInfo;
	}

	public String getDIFName() {
		return this.difName;
	}

	public DataTransferConstants getDataTransferConstants() {
		return null;
	}

	public List<Neighbor> getNeighbors() {
		return null;
	}

	public OperationalStatus getOperationalStatus() {
		return OperationalStatus.STARTED;
	}

	public List<QoSCube> getQoSCubes() {
		return this.flowAllocator.getQoSCubes();
	}

	public List<WhatevercastName> getWhatevercastNames() {
		return null;
	}

}
