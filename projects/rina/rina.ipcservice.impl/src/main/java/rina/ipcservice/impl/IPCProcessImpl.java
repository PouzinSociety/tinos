package rina.ipcservice.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.efcp.api.DataTransferAE;
import rina.efcp.api.DataTransferAEInstance;
import rina.flowallocator.api.BaseFlowAllocator;
import rina.flowallocator.api.FlowAllocator;
import rina.ipcprocess.api.BaseIPCProcess;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.AllocateRequest;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.IPCException;
import rina.ipcservice.api.IPCService;
import rina.ipcservice.impl.jobs.DeliverDeallocateJob;
import rina.ipcservice.impl.jobs.DeliverSDUJob;
import rina.ipcservice.impl.jobs.SubmitAllocateRequestJob;
import rina.ipcservice.impl.ribobjects.ApplicationProcessNameRIBObject;
import rina.ipcservice.impl.ribobjects.WhatevercastNameSetRIBObject;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * Point of entry to the IPC process for the application process. It is in charge 
 * of orchestrating the calls provided by the IPCService interface (by delegating to 
 * the FlowAllocator, RIBDaemon, ...). It also contains the pointers to the client 
 * application processes
 * @author eduardgrasa
 *
 */
public class IPCProcessImpl extends BaseIPCProcess implements IPCService{

	private static final Log log = LogFactory.getLog(IPCProcessImpl.class);

	/**
	 * The maximum number of worker threads in the IPC Process thread pool
	 */
	private static int MAXWORKERTHREADS = 10;

	/**
	 * Stores the applications that have a port Id in transfer state
	 */
	private Map<Integer, APService> transferApplicationProcesses = null;

	/**
	 * The thread pool implementation
	 */
	private ExecutorService executorService = null;
	
	/**
	 * The RIB Daemon
	 */
	private RIBDaemon ribDaemon = null;

	public IPCProcessImpl(String applicationProcessName, String applicationProcessInstance, RIBDaemon ribDaemon){
		this.executorService = Executors.newFixedThreadPool(MAXWORKERTHREADS);
		this.transferApplicationProcesses = new HashMap<Integer, APService>();
		this.ribDaemon = ribDaemon;
		populateRIB(applicationProcessName, applicationProcessInstance);
	}

	/**
	 * Tell the ribDaemon the portions of the RIB space that the IPC process will manage
	 */
	private void populateRIB(String applicationProcessName, String applicationProcessInstance){
		try{
			ApplicationProcessNamingInfo apNamingInfo = new ApplicationProcessNamingInfo(applicationProcessName, applicationProcessInstance);
			RIBObject ribObject = new ApplicationProcessNameRIBObject(this);
			ribObject.write(null, null, 0, apNamingInfo);
			ribDaemon.addRIBObject(ribObject);
			ribObject = new WhatevercastNameSetRIBObject(this);
			ribDaemon.addRIBObject(ribObject);
		}catch(RIBDaemonException ex){
			ex.printStackTrace();
			log.error("Could not subscribe to RIB Daemon:" +ex.getMessage());
		}
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
		log.debug("Allocate request received, forwarding it to the Flow Allocator");
		FlowAllocator flowAllocator = (FlowAllocator) this.getIPCProcessComponent(BaseFlowAllocator.getComponentName());
		SubmitAllocateRequestJob job = new SubmitAllocateRequestJob(allocateRequest, flowAllocator, applicationProcess);
		executorService.execute(job);
	}

	/**
	 * Forward the allocate response to the Flow Allocator.
	 * @param portId
	 * @param success
	 */
	public synchronized void submitAllocateResponse(int portId, boolean success, String reason) throws IPCException{
		Integer key = new Integer(portId);
		FlowAllocator flowAllocator = (FlowAllocator) this.getIPCProcessComponent(FlowAllocator.class.getName());
		flowAllocator.submitAllocateResponse(portId, success, reason);
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

		FlowAllocator flowAllocator = (FlowAllocator) this.getIPCProcessComponent(FlowAllocator.class.getName());
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

	public synchronized  void submitTransfer(int portId, byte[] sdu) throws IPCException{
		Integer key = new Integer(portId);

		if (!transferApplicationProcesses.keySet().contains(key)){
			throw new IPCException(IPCException.PORTID_NOT_IN_TRANSFER_STATE);
		}

		List<byte[]> sdus = new ArrayList<byte[]>();
		sdus.add(sdu);
		DataTransferAE dataTransferAE = (DataTransferAE) this.getIPCProcessComponent(DataTransferAE.class.getName());
		DataTransferAEInstance dataTransferAEInstance = dataTransferAE.getDataTransferAEInstance(portId);
		dataTransferAEInstance.sdusDelivered(sdus);
	}

	/**
	 * An application says it is no longer available through this DIF
	 */
	public synchronized void unregister(ApplicationProcessNamingInfo apNamingInfo) {
		FlowAllocator flowAllocator = (FlowAllocator) this.getIPCProcessComponent(FlowAllocator.class.getName());
		flowAllocator.getDirectoryForwardingTable().removeEntry(apNamingInfo);
		//TODO tell the RIB Daemon to disseminate this
	}

	/**
	 * An application process says it is available through this DIF
	 */
	public synchronized void register(ApplicationProcessNamingInfo apNamingInfo) {
		FlowAllocator flowAllocator = (FlowAllocator) this.getIPCProcessComponent(FlowAllocator.class.getName());
		try{
			Long currentSynonym = (Long) ribDaemon.read(null, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + 
					RIBObjectNames.CURRENT_SYNONYM, 0).getObjectValue();
			//TODO fix this
			//flowAllocator.getDirectoryForwardingTable().addEntry(apNamingInfo, currentSynonym.);
			//TODO tell the RIB Daemon to disseminate this
		}catch(RIBDaemonException ex){
			log.error(ex);
		}
	}

	public void destroy(){
		executorService.shutdown();
	}
}