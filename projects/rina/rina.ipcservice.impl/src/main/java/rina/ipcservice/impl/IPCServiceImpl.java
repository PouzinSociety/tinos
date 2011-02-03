package rina.ipcservice.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import rina.flowallocator.api.FlowAllocator;
import rina.ipcservice.api.AllocateRequest;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.IPCService;
import rina.ipcservice.api.APService;

/**
 * Point of entry to the IPC process for the application process. It is in charge 
 * of orchestrating the calls provided by the IPCService interface (by delegating to 
 * the FlowAllocator, RIBDaemon, ...). It also contains the pointers to the client 
 * application processes
 * @author eduardgrasa
 *
 */
public class IPCServiceImpl implements IPCService{
	
	/**
	 * Stores the applications that have a port Id allocated
	 */
	private Map<Integer, APService> applicationProcessesWithFlows = null;
	
	/**
	 * The instance of the flow allocator
	 */
	private FlowAllocator flowAllocator = null;
	
	/**
	 * The address of this IPC process
	 */
	private byte[] address = null;
	
	/**
	 * The application naming info for this IPC process
	 */
	private ApplicationProcessNamingInfo namingInfo = null;
	
	public IPCServiceImpl(){
		this.applicationProcessesWithFlows = new HashMap<Integer, APService>();
	}
	
	public byte[] getAddress() {
		return address;
	}

	public void setAddress(byte[] address) {
		this.address = address;
	}

	public ApplicationProcessNamingInfo getNamingInfo() {
		return namingInfo;
	}

	public void setNamingInfo(ApplicationProcessNamingInfo namingInfo) {
		this.namingInfo = namingInfo;
	}

	public FlowAllocator getFlowAllocator() {
		return flowAllocator;
	}

	public void setFlowAllocator(FlowAllocator flowAllocator) {
		this.flowAllocator = flowAllocator;
	}

	public Map<Integer, APService> getApplicationProcessesWithFlows() {
		return applicationProcessesWithFlows;
	}

	/**
	 * Forward the allocate request to the port allocator. Before, choose an available portId
	 * @param allocateRequest
	 * @param applicationProcess
	 */
	public void submitAllocateRequest(AllocateRequest allocateRequest, APService applicationProcess) {
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

	public void submitAllocateResponse(int arg0, boolean arg1) {
		// TODO Auto-generated method stub
	}

	public void submitDeallocate(int arg0) {
		// TODO Auto-generated method stub
	}

	public void submitStatus(int arg0) {
		// TODO Auto-generated method stub
	}

	public boolean submitTransfer(int arg0, byte[] arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public void unregister(ApplicationProcessNamingInfo arg0) {
		// TODO Delegate to RIB Daemon
	}
	
	public void register(ApplicationProcessNamingInfo arg0) {
		// TODO delegate to RIBDaemon
	}
}