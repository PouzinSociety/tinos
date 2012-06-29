package rina.shimipcprocess.ip;

import java.net.InetAddress;
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
	
	public ShimIPCProcessForIPLayers(ApplicationProcessNamingInfo apNamingInfo, String hostName, String difName, Delimiter delimiter){
		super();
		this.apNamingInfo = apNamingInfo;
		this.hostName = hostName;
		this.difName = difName;
		this.addIPCProcessComponent(delimiter);
		this.flowAllocator = new FlowAllocatorImpl(hostName);
		this.addIPCProcessComponent(this.flowAllocator);
		this.addIPCProcessComponent(new RIBDaemonImpl(this, flowAllocator));
	}
	
	public String getHostname(){
		return this.hostName;
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

	@Override
	/**
	 * Register an application to the shim IPC Process. It will cause the shim IPC Process to 
	 * listen to a certain TCP and UDP port number.
	 * @param apNamingInfo The naming information of the IPC Process to register
	 * @param applicationCallback the callback to contact the application in case of incoming flow requests
	 */
	public void register(ApplicationProcessNamingInfo apNamingInfo, APService applicationCallback) throws IPCException{
		this.flowAllocator.register(apNamingInfo, applicationCallback);
	}
	
	@Override
	/**
	 * Unregisters a local application from this shim IPC Process. The shim will stop 
	 * listening at TCP and UDP ports.
	 * @param apNamingInfo the application to be unregistered
	 */
	public void unregister(ApplicationProcessNamingInfo apNamingInfo) throws IPCException{
		this.flowAllocator.unregister(apNamingInfo);
	}

	@Override
	/**
	 * Invoked by a local application to request a new flow to be allocated
	 * @param flowService Specifies the allocation information request
	 * @param applicationCallback the callback to contact the application in case of incoming flow requests
	 */
	public int submitAllocateRequest(FlowService flowService, APService applicationCallback) throws IPCException {
		return this.flowAllocator.submitAllocateRequest(flowService, applicationCallback);
	}

	@Override
	public void submitAllocateResponse(int portId, boolean successs, String reason, APService applicationCallback) throws IPCException {
		this.flowAllocator.submitAllocateResponse(portId, successs, reason, applicationCallback);
	}

	@Override
	public void submitDeallocate(int portId) throws IPCException {
		this.flowAllocator.submitDeallocate(portId);
	}

	@Override
	public void submitStatus(int arg0) {
	}

	@Override
	public void submitTransfer(int arg0, byte[] sdu) throws IPCException {
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
	public List<Neighbor> getNeighbors() {
		return null;
	}

	@Override
	public OperationalStatus getOperationalStatus() {
		return OperationalStatus.STARTED;
	}

	@Override
	public List<QoSCube> getQoSCubes() {
		return this.flowAllocator.getQoSCubes();
	}

	@Override
	public List<WhatevercastName> getWhatevercastNames() {
		return null;
	}

}
