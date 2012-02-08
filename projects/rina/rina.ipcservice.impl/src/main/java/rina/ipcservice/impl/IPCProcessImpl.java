package rina.ipcservice.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.flowallocator.api.BaseFlowAllocator;
import rina.flowallocator.api.FlowAllocator;
import rina.ipcprocess.api.BaseIPCProcess;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCException;
import rina.ipcservice.api.IPCService;
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
	 * The RIB Daemon
	 */
	private RIBDaemon ribDaemon = null;

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

	/**
	 * Forward the allocate request to the Flow Allocator. Before, choose an available portId
	 * @param allocateRequest
	 * @param applicationProcess
	 * @throws IPCException
	 */
	public synchronized int submitAllocateRequest(FlowService flowService) throws IPCException{
		log.debug("Allocate request received, forwarding it to the Flow Allocator");
		FlowAllocator flowAllocator = (FlowAllocator) this.getIPCProcessComponent(BaseFlowAllocator.getComponentName());
		return flowAllocator.submitAllocateRequest(flowService);
	}

	/**
	 * Forward the allocate response to the Flow Allocator.
	 * @param portId
	 * @param success
	 */
	public synchronized void submitAllocateResponse(int portId, boolean success, String reason) throws IPCException{
		log.debug("Allocate request received, forwarding it to the Flow Allocator");
		FlowAllocator flowAllocator = (FlowAllocator) this.getIPCProcessComponent(FlowAllocator.class.getName());
		flowAllocator.submitAllocateResponse(portId, success, reason);
	}

	/**
	 * Forward the deallocate call to the Flow Allocator
	 * @param portId 
	 */
	public synchronized void submitDeallocate(int portId) throws IPCException{
		log.debug("Deallocate request received, forwarding it to the Flow Allocator");
		FlowAllocator flowAllocator = (FlowAllocator) this.getIPCProcessComponent(FlowAllocator.class.getName());
		flowAllocator.submitDeallocate(portId);
	}

	public synchronized void submitStatus(int arg0) {
		// TODO Auto-generated method stub
	}

	/**
	 * Send an SDU through the flow identified by portId
	 * @param portId
	 * @param sdu
	 * @throws IPCException
	 */
	public synchronized void submitTransfer(int portId, byte[] sdu) throws IPCException{
		FlowAllocator flowAllocator = (FlowAllocator) this.getIPCProcessComponent(FlowAllocator.class.getName());
		flowAllocator.submitTransfer(portId, sdu);
		
		/*List<byte[]> sdus = new ArrayList<byte[]>();
		sdus.add(sdu);
		DataTransferAE dataTransferAE = (DataTransferAE) this.getIPCProcessComponent(DataTransferAE.class.getName());
		DataTransferAEInstance dataTransferAEInstance = dataTransferAE.getDataTransferAEInstance(portId);
		dataTransferAEInstance.sdusDelivered(sdus);*/
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

	public void destroy() {
		// TODO Auto-generated method stub
	}
	
	public Long getAddress(){
		Long result = null;
		try{
			result = (Long) ribDaemon.read(null, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT +
					RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.CURRENT_SYNONYM, 0).getObjectValue();
		}catch(Exception ex){
			log.error(ex);
		}

		return result;
	}
	
	public String getApplicationProcessName(){
		String result = null;
		try{
			result = ((ApplicationProcessNamingInfo) ribDaemon.read(null, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.APNAME, 0).getObjectValue()).
					getApplicationProcessName();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		return result;
	}
	
	public String getApplicationProcessInstance(){
		String result = null;
		try{
			result = ((ApplicationProcessNamingInfo) ribDaemon.read(null, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.APNAME, 0).getObjectValue()).
					getApplicationProcessInstance();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		return result;
	}
}