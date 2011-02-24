package rina.ipcservice.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.efcp.api.DataTransferAE;
import rina.flowallocator.api.FlowAllocator;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.AllocateRequest;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.IPCException;
import rina.ipcservice.api.IPCService;
import rina.ipcservice.api.APService;
import rina.ipcservice.impl.jobs.DeliverAllocateResponseJob;
import rina.ipcservice.impl.jobs.DeliverDeallocateJob;
import rina.ipcservice.impl.jobs.DeliverSDUJob;
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
	
	private static final Log log = LogFactory.getLog(IPCProcessImpl.class);
	
	/**
	 * The maximum number of worker threads in the IPC Process thread pool
	 */
	private static int MAXWORKERTHREADS = 5;
	
	/**
	 * Stores the applications that have a port Id in allocation pending state
	 */
	private Map<Integer, APService> allocationPendingApplicationProcesses = null;
	
	/**
	 * Stores the applications that have a port Id in transfer state
	 */
	private Map<Integer, APService> transferApplicationProcesses = null;
	
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
	
	private byte[] address = null;
	
	/**
	 * The thread pool implementation
	 */
	private ExecutorService executorService = null;
	
	public IPCProcessImpl(ApplicationProcessNamingInfo namingInfo){
		this.executorService = Executors.newFixedThreadPool(MAXWORKERTHREADS);
		this.allocationPendingApplicationProcesses = new HashMap<Integer, APService>();
		this.transferApplicationProcesses = new HashMap<Integer, APService>();
		this.namingInfo = namingInfo;
	}
	
	public byte[] getIPCProcessAddress(){
		return address;
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
		APService applicationProcess = transferApplicationProcesses.get(new Integer(portId));
		
		if (applicationProcess == null ){
			//TODO, log, throw Exception?
			return;
		}
		
		if (sdus == null){
			//TODO, log, throw Exception?
			return;
		}
		
		executorService.execute(new DeliverSDUJob(applicationProcess, sdus, portId));
	}

	/**
	 * Forward the allocate request to the Flow Allocator. Before, choose an available portId
	 * @param allocateRequest
	 * @param applicationProcess
	 */
	public synchronized void submitAllocateRequest(AllocateRequest allocateRequest, APService applicationProcess){
		int portId = choosePortId();
		allocationPendingApplicationProcesses.put(new Integer(portId), applicationProcess);
		try{
			flowAllocator.submitAllocateRequest(allocateRequest, portId);
		}catch(IPCException ex){
			//Something in the validation request was not valid or there are not enough 
			//resources to honour the request. Notify the application process
			allocationPendingApplicationProcesses.remove(new Integer(portId));
			executorService.execute(new DeliverAllocateResponseJob(
					applicationProcess, allocateRequest.getRequestedAPinfo(), -1, ex.getErrorCode(), ex.getMessage()));
		}
	}
	
	/**
	 * Select a portId that is available
	 * @return
	 */
	private int choosePortId(){
		for(int i=1; i<Integer.MAX_VALUE; i++){
			Integer candidate = new Integer(i);
			if (!allocationPendingApplicationProcesses.keySet().contains(candidate) && 
					!transferApplicationProcesses.keySet().contains(candidate)){
				return i;
			}
		}
		
		return 0;
	}

	/**
	 * Forward the allocate response to the Flow Allocator.
	 * @param portId
	 * @param success
	 */
	public synchronized void submitAllocateResponse(int portId, boolean success) throws IPCException{
		Integer key = new Integer(portId);
		
		if (!allocationPendingApplicationProcesses.keySet().contains(key)){
			throw new IPCException(IPCException.PORTID_NOT_IN_ALLOCATION_PENDING_STATE);
		}
		
		APService apService = allocationPendingApplicationProcesses.remove(key);
		if (success){
			transferApplicationProcesses.put(key, apService);
		}
		
		flowAllocator.submitAllocateResponse(portId, success);
	}

	/**
	 * Forward the deallocate call to the Flow Allocator
	 * @param portId 
	 */
	public synchronized void submitDeallocate(int portId) throws IPCException{
		Integer key = new Integer(portId);
		
		if (!transferApplicationProcesses.keySet().contains(key)){
			throw new IPCException(IPCException.PORTID_NOT_IN_TRANSFER_STATE);
		}
		
		transferApplicationProcesses.remove(key);
		
		flowAllocator.submitDeallocate(portId);
	}
	
	/**
	 * Call the applicationProcess deallocate.deliver operation
	 * @param portId
	 */
	public void deliverDeallocateRequestToApplicationProcess(int portId) {
		Integer key = new Integer(portId);
		
		if (!transferApplicationProcesses.keySet().contains(key)){
			log.error("Could not find an application process with portId "+portId+" in transfer state");
			return;
		}
		
		APService apService = transferApplicationProcesses.remove(key);
		executorService.execute(new DeliverDeallocateJob(apService, portId));
	}

	public synchronized void submitStatus(int arg0) {
		// TODO Auto-generated method stub
	}

	public synchronized  boolean submitTransfer(int portId, byte[] sdu) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * An application says it is no longer available through this DIF
	 */
	public synchronized void unregister(ApplicationProcessNamingInfo apNamingInfo) {
		flowAllocator.getDirectory().removeEntry(apNamingInfo);
		//TODO tell the RIB Daemon to disseminate this
	}
	
	/**
	 * An application process says it is available through this DIF
	 */
	public synchronized void register(ApplicationProcessNamingInfo apNamingInfo) {
		flowAllocator.getDirectory().addEntry(apNamingInfo, address);
		//TODO tell the RIB Daemon to disseminate this
	}
	
	public void destroy(){
		executorService.shutdown();
	}
}