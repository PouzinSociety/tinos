package rina.ipcservice.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rina.efcp.api.DataTransferAE;
import rina.flowallocator.api.FlowAllocator;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.AllocateRequest;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.IPCException;
import rina.ipcservice.api.IPCService;
import rina.ipcservice.api.APService;
import rina.ribdaemon.api.RIBDaemon;
import rina.rmt.api.RMT;

/**
 * Point of entry to the IPC process for the application process. It is in charge 
 * of orchestrating the calls provided by the IPCService interface (by delegating to 
 * the FlowAllocator, RIBDaemon, ...). It also contains the pointers to the client 
 * application processes
 * @author eduardgrasa
 *
 */
public class IPCProcessImpl implements IPCService, IPCProcess{
	
	/**
	 * Stores the applications that have a port Id allocated
	 */
	private Map<Integer, APService> applicationProcessesWithFlows = null;
	
	/**
	 * The instance of the flow allocator
	 */
	private FlowAllocator flowAllocator = null;
	
	/**
	 * The instance of the data transfer AE
	 */
	private DataTransferAE dataTransferAE = null;
	
	/**
	 * The instance of the RIB Daemon
	 */
	private RIBDaemon ribDaemon = null;
	
	/**
	 * The instance of the RMT
	 */
	private RMT rmt = null;
	
	/**
	 * The naming information of this IPC process
	 */
	private ApplicationProcessNamingInfo namingInfo = null;
	
	public IPCProcessImpl(ApplicationProcessNamingInfo namingInfo){
		this.applicationProcessesWithFlows = new HashMap<Integer, APService>();
		this.namingInfo = namingInfo;
	}

	public ApplicationProcessNamingInfo getIPCProcessNamingInfo() {
		return namingInfo;
	}

	public void setIPCProcessNamingInfo(ApplicationProcessNamingInfo namingInfo) {
		this.namingInfo = namingInfo;
	}

	public FlowAllocator getFlowAllocator() {
		return flowAllocator;
	}

	public void setFlowAllocator(FlowAllocator flowAllocator) {
		this.flowAllocator = flowAllocator;
		flowAllocator.setIPCProcess(this);
	}
	
	public DataTransferAE getDataTransferAE() {
		return dataTransferAE;
	}

	public void setDataTransferAE(DataTransferAE dataTransferAE) {
		this.dataTransferAE = dataTransferAE;
	}

	public RIBDaemon getRibDaemon() {
		return ribDaemon;
	}

	public void setRibDaemon(RIBDaemon ribDaemon) {
		this.ribDaemon = ribDaemon;
		ribDaemon.setIPCProcess(this);
	}

	public RMT getRmt() {
		return rmt;
	}

	public void setRmt(RMT rmt) {
		this.rmt = rmt;
		rmt.setIPCProcess(this);
	}
	
	public synchronized void deliverSDUsToApplicationProcess(List<byte[]> sdus, int portId) {
		APService applicationProcess = applicationProcessesWithFlows.get(new Integer(portId));
		
		if (applicationProcess == null ){
			//TODO, log, throw Exception?
			return;
		}
		
		if (sdus == null){
			//TODO, log, throw Exception?
			return;
		}
		
		DeliverSDUThread thread = new DeliverSDUThread(applicationProcess, sdus, portId);
		thread.start();
	}

	public Map<Integer, APService> getApplicationProcessesWithFlows() {
		return applicationProcessesWithFlows;
	}

	/**
	 * Forward the allocate request to the port allocator. Before, choose an available portId
	 * @param allocateRequest
	 * @param applicationProcess
	 */
	public synchronized void submitAllocateRequest(AllocateRequest allocateRequest, APService applicationProcess) throws IPCException{
		int portId = choosePortId();
		applicationProcessesWithFlows.put(new Integer(portId), applicationProcess);
		flowAllocator.submitAllocateRequest(allocateRequest, applicationProcess, portId);
	}
	
	/**
	 * Select a portId that is available
	 * @return
	 */
	private int choosePortId(){
		Set<Integer> allocatedPortIds = applicationProcessesWithFlows.keySet();
		for(int i=1; i<Integer.MAX_VALUE; i++){
			if (!allocatedPortIds.contains(new Integer(i))){
				return i;
			}
		}
		
		return 0;
	}

	public synchronized void submitAllocateResponse(int arg0, boolean arg1) {
		//1 Check if port id is allocated
		 
	}

	public synchronized void submitDeallocate(int arg0) {
		// TODO Auto-generated method stub
	}

	public synchronized void submitStatus(int arg0) {
		// TODO Auto-generated method stub
	}

	public synchronized  boolean submitTransfer(int portId, byte[] sdu) {
		// TODO Auto-generated method stub
		return false;
	}

	public synchronized void unregister(ApplicationProcessNamingInfo arg0) {
		// TODO Delegate to RIB Daemon
	}
	
	public synchronized void register(ApplicationProcessNamingInfo arg0) {
		// TODO delegate to RIBDaemon
	}
}