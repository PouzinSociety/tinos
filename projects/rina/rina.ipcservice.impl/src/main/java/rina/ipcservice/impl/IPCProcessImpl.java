package rina.ipcservice.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.flowallocator.api.BaseFlowAllocator;
import rina.flowallocator.api.DirectoryForwardingTable;
import rina.flowallocator.api.DirectoryForwardingTableEntry;
import rina.flowallocator.api.FlowAllocator;
import rina.ipcprocess.api.BaseIPCProcess;
import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCException;
import rina.ipcservice.api.IPCService;
import rina.ipcservice.impl.ribobjects.WhatevercastNameSetRIBObject;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.NotificationPolicy;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;
import rina.ribdaemon.api.SimpleRIBObject;

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
	 * The RIB Daemon
	 */
	private RIBDaemon ribDaemon = null;
	
	/**
	 * The Flow Allocator
	 */
	private FlowAllocator flowAllocator = null;

	public IPCProcessImpl(String applicationProcessName, String applicationProcessInstance, RIBDaemon ribDaemon){
		this.ribDaemon = ribDaemon;
		populateRIB(applicationProcessName, applicationProcessInstance);
	}
	
	/**
	 * Will call the execute operation of the IPCManager in order to execute a runnable.
	 * Classes implementing IPCProcess should not create its own thread pool, but use 
	 * the one managed by the IPCManager instead.
	 * @param runnable
	 */
	public void execute(Runnable runnable){
		this.getIPCManager().execute(runnable);
	}

	/**
	 * Tell the ribDaemon the portions of the RIB space that the IPC process will manage
	 */
	private void populateRIB(String applicationProcessName, String applicationProcessInstance){
		try{
			ApplicationProcessNamingInfo apNamingInfo = new ApplicationProcessNamingInfo(applicationProcessName, applicationProcessInstance);
			RIBObject ribObject = new SimpleRIBObject(this, 
					ApplicationProcessNamingInfo.APPLICATION_PROCESS_NAMING_INFO_RIB_OBJECT_CLASS, 
					ApplicationProcessNamingInfo.APPLICATION_PROCESS_NAMING_INFO_RIB_OBJECT_NAME, 
					apNamingInfo);
			ribDaemon.addRIBObject(ribObject);
			ribObject = new WhatevercastNameSetRIBObject(this); 
			ribDaemon.addRIBObject(ribObject);
		}catch(RIBDaemonException ex){
			ex.printStackTrace();
			log.error("Could not subscribe to RIB Daemon:" +ex.getMessage());
		}
	}
	
	private FlowAllocator getFlowAllocator(){
		if (this.flowAllocator == null){
			this.flowAllocator = (FlowAllocator) this.getIPCProcessComponent(BaseFlowAllocator.getComponentName());
		}
		
		return this.flowAllocator;
	}

	/**
	 * Forward the allocate request to the Flow Allocator. Before, choose an available portId
	 * @param allocateRequest
	 * @param applicationProcess
	 * @throws IPCException
	 */
	public int submitAllocateRequest(FlowService flowService) throws IPCException{
		log.debug("Allocate request received, forwarding it to the Flow Allocator");
		return getFlowAllocator().submitAllocateRequest(flowService);
	}

	/**
	 * Forward the allocate response to the Flow Allocator.
	 * @param portId
	 * @param success
	 */
	public void submitAllocateResponse(int portId, boolean success, String reason) throws IPCException{
		log.debug("Allocate request received, forwarding it to the Flow Allocator");
		getFlowAllocator().submitAllocateResponse(portId, success, reason);
	}

	/**
	 * Forward the deallocate call to the Flow Allocator
	 * @param portId 
	 */
	public void submitDeallocate(int portId) throws IPCException{
		log.debug("Deallocate request received, forwarding it to the Flow Allocator");
		getFlowAllocator().submitDeallocate(portId);
	}

	public void submitStatus(int arg0) {
		// TODO Auto-generated method stub
	}

	/**
	 * Send an SDU through the flow identified by portId
	 * @param portId
	 * @param sdu
	 * @throws IPCException
	 */
	public void submitTransfer(int portId, byte[] sdu) throws IPCException{
		getFlowAllocator().submitTransfer(portId, sdu);
		
		/*List<byte[]> sdus = new ArrayList<byte[]>();
		sdus.add(sdu);
		DataTransferAE dataTransferAE = (DataTransferAE) this.getIPCProcessComponent(DataTransferAE.class.getName());
		DataTransferAEInstance dataTransferAEInstance = dataTransferAE.getDataTransferAEInstance(portId);
		dataTransferAEInstance.sdusDelivered(sdus);*/
	}

	/**
	 * An application says it is no longer available through this DIF
	 */
	public void unregister(ApplicationProcessNamingInfo apNamingInfo) {
		RIBDaemon ribDaemon = (RIBDaemon) this.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		try{
			NotificationPolicy notificationPolicy = new NotificationPolicy(new int[0]);
			ribDaemon.delete(DirectoryForwardingTable.DIRECTORY_FORWARDING_TABLE_ENTRY_RIB_OBJECT_CLASS, 
					DirectoryForwardingTable.DIRECTORY_FORWARDING_ENTRY_SET_RIB_OBJECT_NAME + RIBObjectNames.SEPARATOR + 
					apNamingInfo.getEncodedString(), null, notificationPolicy);
		}catch(RIBDaemonException ex){
			log.error(ex);
		}
	}

	/**
	 * An application process says it is available through this DIF
	 */
	public void register(ApplicationProcessNamingInfo apNamingInfo) {
		RIBDaemon ribDaemon = (RIBDaemon) this.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		try{
			DirectoryForwardingTableEntry entry = new DirectoryForwardingTableEntry();
			entry.setAddress(this.getAddress().longValue());
			entry.setApNamingInfo(apNamingInfo);
			entry.setTimestamp(System.nanoTime()/1000L);
			
			NotificationPolicy notificationPolicy = new NotificationPolicy(new int[0]);
			ribDaemon.create(DirectoryForwardingTable.DIRECTORY_FORWARDING_TABLE_ENTRY_RIB_OBJECT_CLASS, 
					DirectoryForwardingTable.DIRECTORY_FORWARDING_ENTRY_SET_RIB_OBJECT_NAME + RIBObjectNames.SEPARATOR 
					+ apNamingInfo.getEncodedString(), entry, notificationPolicy);
		}catch(RIBDaemonException ex){
			log.error(ex);
		}
	}

	public void destroy() {
		// TODO Auto-generated method stub
	}
}