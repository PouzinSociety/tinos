package rina.resourceallocator.impl.flowmanager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.cdap.api.BaseCDAPSessionManager;
import rina.cdap.api.CDAPSession;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.CDAPSessionManager;
import rina.configuration.SDUProtectionOption;
import rina.efcp.api.PDU;
import rina.efcp.api.PDUParser;
import rina.enrollment.api.Neighbor;
import rina.events.api.events.NMinusOneFlowAllocatedEvent;
import rina.events.api.events.NMinusOneFlowAllocationFailedEvent;
import rina.events.api.events.NMinusOneFlowDeallocatedEvent;
import rina.ipcmanager.api.IPCManager;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcprocess.api.IPCProcess.IPCProcessType;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCException;
import rina.ipcservice.api.IPCService;
import rina.protection.api.BaseSDUProtectionModuleRepository;
import rina.protection.api.SDUProtectionModuleRepository;
import rina.resourceallocator.api.NMinus1FlowDescriptor;
import rina.resourceallocator.api.NMinus1FlowManager;
import rina.resourceallocator.api.PDUForwardingTable;
import rina.resourceallocator.impl.ribobjects.DIFRegistrationRIBObject;
import rina.resourceallocator.impl.ribobjects.DIFRegistrationSetRIBObject;
import rina.resourceallocator.impl.ribobjects.NMinus1FlowRIBObject;
import rina.resourceallocator.impl.ribobjects.NMinus1FlowSetRIBObject;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * Manages the allocation and lifetime of N-1 Flows for 
 * an IPC Process 
 * @author eduardgrasa
 *
 */
public class NMinus1FlowManagerImpl implements NMinus1FlowManager, APService{
	
	private static final Log log = LogFactory.getLog(NMinus1FlowManagerImpl.class);

	/**
	 * The IPC Manager
	 */
	private IPCManager ipcManager = null;
	
	/**
	 * The IPC Process the N-1 Flow Manager is part of
	 */
	private IPCProcess ipcProcess = null;
	
	/**
	 * The RIB Daemon
	 */
	private RIBDaemon ribDaemon = null;
	
	/**
	 * The CDAP Session Manager
	 */
	private CDAPSessionManager cdapSessionManager = null;
	
	/**
	 * The states of all the ongoing and allocated N-1 flows
	 */
	private Map<Integer, NMinus1FlowDescriptor> nMinus1FlowDescriptors = null;
	
	/**
	 * The DIFs this IPC Process is registered at
	 */
	private List<String> difRegistrations = null;
	
	/**
	 * The PDU Forwarding table
	 */
	private PDUForwardingTable pduForwardingTable = null;
	
	/**
	 * Maps the preferred SDU protection module for each potential 
	 * N-1 DIF
	 */
	private Map<String, String> nMinus1DIFProtectionOptions = null;
	
	private Timer timer = null;

	public NMinus1FlowManagerImpl(PDUForwardingTable pduForwardingTable){
		this.nMinus1FlowDescriptors = new ConcurrentHashMap<Integer, NMinus1FlowDescriptor>();
		this.difRegistrations = new ArrayList<String>();
		this.timer = new Timer();
		this.pduForwardingTable = pduForwardingTable;
		this.nMinus1DIFProtectionOptions = new ConcurrentHashMap<String, String>();
	}
	
	public void setIPCProcess(IPCProcess ipcProcess){
		this.ipcProcess = ipcProcess;
		this.ipcManager = ipcProcess.getIPCManager();
		this.ribDaemon = (RIBDaemon) ipcProcess.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		this.cdapSessionManager = (CDAPSessionManager) ipcProcess.
				getIPCProcessComponent(BaseCDAPSessionManager.getComponentName());
		populateRIB(ipcProcess);
	}
	
	private void populateRIB(IPCProcess ipcProcess){
		try{
			RIBObject ribObject = new NMinus1FlowSetRIBObject(this, ipcProcess);
			ribDaemon.addRIBObject(ribObject);
			ribObject = new DIFRegistrationSetRIBObject(this, ipcProcess);
			ribDaemon.addRIBObject(ribObject);
		}catch(RIBDaemonException ex){
			ex.printStackTrace();
			log.error("Could not subscribe to RIB Daemon:" +ex.getMessage());
		}
	}
	
	/**
	 * Return the N-1 Flow descriptor associated to the flow identified by portId
	 * @param portId
	 * @return the N-1 Flow descriptor
	 * @throws IPCException if no N-1 Flow identified by portId exists
	 */
	public NMinus1FlowDescriptor getNMinus1FlowDescriptor(int portId) throws IPCException{
		NMinus1FlowDescriptor result = this.nMinus1FlowDescriptors.get(new Integer(portId));
		if (result == null){
			throw new IPCException(IPCException.ERROR_CODE, 
					"Could not find an N-1 flow identified by portId " + portId);
		}
		
		return result;
	}
	
	/**
	 * Set the list of preferred SDU protection options as specified by management
	 * @param sduProtectionOptions
	 */
	public void setSDUProtecionOptions(List<SDUProtectionOption> sduProtectionOptions){
		if (sduProtectionOptions == null){
			return;
		}
		
		for(int i=0; i<sduProtectionOptions.size(); i++){
			this.nMinus1DIFProtectionOptions.put(sduProtectionOptions.get(i).getnMinus1DIFName(), 
					sduProtectionOptions.get(i).getSduProtectionType());
		}
	}
	
	/**
	 * Return the type of SDU Protection module to be used for the DIF called "nminus1DIFName"
	 * (return the NULL type if no entries for "nminus1DIFName" are found)
	 * @param nMinus1DIFName
	 * @return
	 */
	public String getSDUProtectionOption(String nMinus1DIFName){
		String result = this.nMinus1DIFProtectionOptions.get(nMinus1DIFName);
		if (result == null){
			return SDUProtectionModuleRepository.NULL;
		}
		
		return result;
	}
	
	/**
	 * Request the allocation of an N-1 Flow with the requested QoS 
	 * to the destination IPC Process 
	 * @param flowService contains the destination IPC Process and requested QoS information
	 */
	public void allocateNMinus1Flow(FlowService flowService){
		//TODO, implement properly with the IDD, right now it requests the flow allocation to 
		//the first shim IPC Process for IP networks that it finds.
		IPCService ipcService = null;
		String nMinus1DIFName = null;
		NMinus1FlowDescriptor nMinus1FlowDescriptor = null;
		
		List<IPCProcess> candidates = this.ipcManager.listIPCProcesses();
		IPCProcess currentCandidate = null;
		for(int i=0; i<candidates.size(); i++){
			currentCandidate = candidates.get(i);
			if (currentCandidate.getType().equals(IPCProcessType.SHIM_IP)){
				ipcService = (IPCService) currentCandidate;
				nMinus1DIFName = currentCandidate.getDIFName();
			}
		}
		
		if (ipcService == null){
			log.error("Could not allocate an N-1 flow with the following characteristics " +
					"because a suitable IPC Process could not be found. "+flowService.toString());
			return;
		}
		
		try{
			int portId = ipcService.submitAllocateRequest(flowService, this);
			flowService.setPortId(portId);
			nMinus1FlowDescriptor = new NMinus1FlowDescriptor();
			nMinus1FlowDescriptor.setFlowService(flowService);
			nMinus1FlowDescriptor.setIpcService(ipcService);
			nMinus1FlowDescriptor.setStatus(NMinus1FlowDescriptor.Status.ALLOCATION_REQUESTED);
			if (flowService.getDestinationAPNamingInfo().getApplicationEntityName().equals(IPCService.MANAGEMENT_AE)){
				nMinus1FlowDescriptor.setManagement(true);
			}else{
				nMinus1FlowDescriptor.setManagement(false);
			}
			nMinus1FlowDescriptor.setnMinus1DIFName(nMinus1DIFName);
			this.nMinus1FlowDescriptors.put(new Integer(portId), nMinus1FlowDescriptor);
		}catch(Exception ex){
			log.error("Issues allocating an N-1 flow to "+flowService+". Details: "+ex);
		}
	}

	/**
	 * Deallocate the N-1 Flow identified by portId
	 * @param portId
	 * @throws IPCException if no N-1 Flow identified by portId exists
	 */
	public void deallocateNMinus1Flow(int portId) throws IPCException {
		NMinus1FlowDescriptor nMinus1FlowDescriptor = this.nMinus1FlowDescriptors.remove(new Integer(portId));
		if (nMinus1FlowDescriptor == null){
			throw new IPCException(IPCException.PROBLEMS_DEALLOCATING_FLOW_CODE, 
					IPCException.PROBLEMS_DEALLOCATING_FLOW + ". Could not find an N-1 flow identified by portId " + portId);
		}
		
		nMinus1FlowDescriptor.getIpcService().submitDeallocate(portId);
		
		try{
			this.ribDaemon.delete(NMinus1FlowRIBObject.N_MINUS_ONE_FLOW_RIB_OBJECT_CLASS, 
					NMinus1FlowSetRIBObject.N_MINUS_ONE_FLOW_SET_RIB_OBJECT_NAME + RIBObjectNames.SEPARATOR + portId);
		}catch(RIBDaemonException ex){
			log.warn("Error deleting N Minus One Flow RIB Object", ex);
		}
		
		//TODO Move this to the routing module
		if (!nMinus1FlowDescriptor.isManagement()){
			long destinationAddress = this.getNeighborAddress(nMinus1FlowDescriptor.getFlowService().getDestinationAPNamingInfo());
			if (destinationAddress != -1){
				int qosId = nMinus1FlowDescriptor.getFlowService().getQoSSpecification().getQosCubeId();
				this.pduForwardingTable.removeEntry(destinationAddress, qosId);
			}
		}
		
		//Notify about the event
		CDAPSession cdapSession = cdapSessionManager.getCDAPSession(portId);
		CDAPSessionDescriptor cdapSessionDescriptor = null;
		if (cdapSession != null){
			cdapSessionDescriptor = cdapSession.getSessionDescriptor();
		}
		NMinusOneFlowDeallocatedEvent event = new NMinusOneFlowDeallocatedEvent(portId, cdapSessionDescriptor);
		this.ribDaemon.deliverEvent(event);
	}
	
	/**
	 * Register the IPC Process to one or more N-1 DIFs
	 * @param difName The N-1 DIF where the IPC Process will register
	 * @throws IPCException
	 */
	public void registerIPCProcess(String difName) throws IPCException{
		if (this.difRegistrations.contains(difName)){
			throw new IPCException(IPCException.PROBLEMS_REGISTERING_APPLICATION_CODE, 
			"The IPC Process is already registered at the DIF "+difName);
		}
		
		IPCService ipcService = (IPCService) this.ipcManager.getIPCProcessBelongingToDIF(difName);
		if (ipcService == null){
			throw new IPCException(IPCException.PROBLEMS_REGISTERING_APPLICATION_CODE, 
					"Problems registering IPC Process: Could not find any N-1 IPC Process to register at.");
		}
		
		//Register both the Management AE and the Data Transfer AE for this IPC Process
		ApplicationProcessNamingInfo apNamingInfo = (ApplicationProcessNamingInfo) this.ipcProcess.getApplicationProcessNamingInfo().clone();
		apNamingInfo.setApplicationEntityName(IPCService.MANAGEMENT_AE);
		ipcService.register(apNamingInfo, this);
		
		apNamingInfo = (ApplicationProcessNamingInfo) apNamingInfo.clone();
		apNamingInfo.setApplicationEntityName(IPCService.DATA_TRANSFER_AE);
		ipcService.register(apNamingInfo, this);
		
		this.difRegistrations.add(difName);
		try{
			this.ribDaemon.create(DIFRegistrationRIBObject.DIF_REGISTRATION_RIB_OBJECT_CLASS, 
					DIFRegistrationSetRIBObject.DIF_REGISTRATION_SET_RIB_OBJECT_NAME + RIBObjectNames.SEPARATOR + difName, 
					difName);
		}catch(RIBDaemonException ex){
			log.warn("Error creating DIF Registration RIB Object", ex);
		}
	}

	/**
	 * Called by an N-1 DIF
	 */
	public String deliverAllocateRequest(FlowService flowService, IPCService nMinus1ipcService) {
		String errorMessage = null;
		if (flowService.getDestinationAPNamingInfo().getApplicationProcessName().equals(this.ipcProcess.getApplicationProcessName()) 
				&& flowService.getDestinationAPNamingInfo().getApplicationProcessInstance().equals(this.ipcProcess.getApplicationProcessInstance())){
			if (flowService.getDestinationAPNamingInfo().getApplicationEntityName() == null){
				//Deny the request
				errorMessage = "Null destination application entity";
				log.error(errorMessage);
				DeliverAllocateResponseTimerTask timerTask =  new DeliverAllocateResponseTimerTask(
						nMinus1ipcService, null, flowService, null, this, null, null, errorMessage, false);
				this.timer.schedule(timerTask, 10);
				return errorMessage;
			}
			
			String encodedApNamingInfo = flowService.getDestinationAPNamingInfo().getEncodedString();
			if (flowService.getDestinationAPNamingInfo().getApplicationEntityName().equals(IPCService.MANAGEMENT_AE)){
				//Flow for the Management AE, check there's not already one with the source AP Name
				synchronized(this.nMinus1FlowDescriptors){
					Iterator<NMinus1FlowDescriptor> iterator = this.nMinus1FlowDescriptors.values().iterator();
					NMinus1FlowDescriptor currentFlowDescriptor = null;
					while(iterator.hasNext()){
						currentFlowDescriptor = iterator.next();
						if (currentFlowDescriptor.getFlowService().getDestinationAPNamingInfo().equals(encodedApNamingInfo) || 
								currentFlowDescriptor.getFlowService().getSourceAPNamingInfo().equals(encodedApNamingInfo)){
							//Deny the flow request, we already have a Management flow with the source application
							errorMessage = "Management flow with "+encodedApNamingInfo+" already established";
							log.error(errorMessage);
							DeliverAllocateResponseTimerTask timerTask =  new DeliverAllocateResponseTimerTask(
									nMinus1ipcService, null, flowService, null, this, null, null, errorMessage, true);
							this.timer.schedule(timerTask, 10);
							return errorMessage;
						}
					}
					
					//Reply with a positive answer
					try{
						DeliverAllocateResponseTimerTask timerTask =  new DeliverAllocateResponseTimerTask(
								nMinus1ipcService, this.ipcProcess, flowService, nMinus1FlowDescriptors, this, this.ribDaemon, 
								this, null, true);
						this.timer.schedule(timerTask, 10);
					}catch(Exception ex){
						log.error("Problems submiting allocate response for N-1 flow identified by portId "+flowService.getPortId()+". "+ex);
					}

					return null;
				}
			}else if (flowService.getDestinationAPNamingInfo().getApplicationEntityName().equals(IPCService.DATA_TRANSFER_AE)){
				//Always reply with a positive answer for the moment. TODO: Only allows 1 flow to the data transfer AE per IPC Process at a time
				try{
					DeliverAllocateResponseTimerTask timerTask =  new DeliverAllocateResponseTimerTask(
							nMinus1ipcService, this.ipcProcess, flowService, nMinus1FlowDescriptors, this, this.ribDaemon, 
							this, null, false);
					this.timer.schedule(timerTask, 10);
				}catch(Exception ex){
					log.error("Problems submiting allocate response for N-1 flow identified by portId "+flowService.getPortId()+". "+ex);
				}

				return null;
			}else{
				errorMessage = "Unrecognized AE Name";
				log.error(errorMessage);
				DeliverAllocateResponseTimerTask timerTask =  new DeliverAllocateResponseTimerTask(
						nMinus1ipcService, null, flowService, null, this, null, null, errorMessage, false);
				this.timer.schedule(timerTask, 10);
				return errorMessage;
			}
		}else{
			errorMessage = "This IPC Process is not the intended destination of this flow";
			log.error(errorMessage);
			DeliverAllocateResponseTimerTask timerTask =  new DeliverAllocateResponseTimerTask(
					nMinus1ipcService, null, flowService, null, this, null, null, errorMessage, false);
			this.timer.schedule(timerTask, 10);
			return errorMessage;
		}
	}

	public void deliverAllocateResponse(int portId, int result, String resultReason) {
		NMinus1FlowDescriptor nMinus1FlowDescriptor = this.nMinus1FlowDescriptors.get(new Integer(portId));
		if (nMinus1FlowDescriptor == null){
			log.warn("Received an allocation notification of an N-1 flow that I was not aware of: "+portId);
			return;
		}
		
		if (!nMinus1FlowDescriptor.getStatus().equals(NMinus1FlowDescriptor.Status.ALLOCATION_REQUESTED)){
			log.warn("Received an allocation notification of an N-1 flow " +
					"whose status was not ALLOCATION_REQUESTED. "+portId);
		}
		
		if (result == 0){
			nMinus1FlowDescriptor.setStatus(NMinus1FlowDescriptor.Status.ALLOCATED);
			try{
				this.ribDaemon.create(NMinus1FlowRIBObject.N_MINUS_ONE_FLOW_RIB_OBJECT_CLASS, 
						NMinus1FlowSetRIBObject.N_MINUS_ONE_FLOW_SET_RIB_OBJECT_NAME + RIBObjectNames.SEPARATOR + portId, 
						nMinus1FlowDescriptor);
			}catch(RIBDaemonException ex){
				log.warn("Error creating N Minus One Flow RIB Object", ex);
			}
			
			nMinus1FlowDescriptor.setPortId(portId);
			//Get adequate SDU protection module
			try{
				SDUProtectionModuleRepository sduProc = (SDUProtectionModuleRepository) this.ipcProcess.getIPCProcessComponent(BaseSDUProtectionModuleRepository.getComponentName());
				nMinus1FlowDescriptor.setSduProtectionModule(
						sduProc.getSDUProtectionModule(getSDUProtectionOption(nMinus1FlowDescriptor.getnMinus1DIFName())));
			}catch(Exception ex){
				log.error(ex);
			}
			
			//TODO Move this to the routing module
			if (!nMinus1FlowDescriptor.isManagement()){
				long destinationAddress = this.getNeighborAddress(nMinus1FlowDescriptor.getFlowService().getDestinationAPNamingInfo());
				if (destinationAddress != -1){
					int qosId = nMinus1FlowDescriptor.getFlowService().getQoSSpecification().getQosCubeId();
					this.pduForwardingTable.addEntry(destinationAddress, qosId, new int[]{portId});
				}
				
				//Send NO-OP PDU
				try{
					PDU noOpPDU = PDUParser.generateIdentifySenderPDU(this.ipcProcess.getAddress().longValue());
					byte[] sdu = nMinus1FlowDescriptor.getSduProtectionModule().protectPDU(noOpPDU);
					this.ipcManager.getOutgoingFlowQueue(portId).writeDataToQueue(sdu);
				}catch(Exception ex){
					ex.printStackTrace();
					log.error("Problems sending No OP PDU through N-1 flow "+portId, ex);
				}
			}
			
			//Notify about the event
			NMinusOneFlowAllocatedEvent event = new NMinusOneFlowAllocatedEvent(nMinus1FlowDescriptor);
			this.ribDaemon.deliverEvent(event);
		}else{
			log.error("Allocation of N-1 flow identified by portId "+ portId + " denied because "+resultReason);
			this.nMinus1FlowDescriptors.remove(new Integer(portId));
			
			//Notify about the event
			NMinusOneFlowAllocationFailedEvent event = new NMinusOneFlowAllocationFailedEvent(
					portId, nMinus1FlowDescriptor.getFlowService(), resultReason);
			this.ribDaemon.deliverEvent(event);
		}
	}

	/**
	 * The N-1 flow identified by portId has been deallocated. Generate an N-1 Flow 
	 * deallocated event to trigger a Forwarding table recalculation
	 */
	public void deliverDeallocate(int portId) {
		NMinus1FlowDescriptor nMinus1FlowDescriptor = this.nMinus1FlowDescriptors.remove(new Integer(portId));
		if (nMinus1FlowDescriptor == null){
			log.warn("Received a deallocation notification of an N-1 flow that I was not aware of: "+portId);
			return;
		}
		
		try{
			this.ribDaemon.delete(NMinus1FlowRIBObject.N_MINUS_ONE_FLOW_RIB_OBJECT_CLASS, 
					NMinus1FlowSetRIBObject.N_MINUS_ONE_FLOW_SET_RIB_OBJECT_NAME + RIBObjectNames.SEPARATOR + portId);
		}catch(RIBDaemonException ex){
			log.warn("Error deleting N Minus One Flow RIB Object", ex);
		}
		
		//TODO Move this to the routing module
		if (!nMinus1FlowDescriptor.isManagement()){
			long destinationAddress = this.getNeighborAddress(nMinus1FlowDescriptor.getFlowService().getDestinationAPNamingInfo());
			if (destinationAddress != -1){
				int qosId = nMinus1FlowDescriptor.getFlowService().getQoSSpecification().getQosCubeId();
				this.pduForwardingTable.removeEntry(destinationAddress, qosId);
			}
		}
		
		//Notify about the event
		CDAPSession cdapSession = cdapSessionManager.getCDAPSession(portId);
		CDAPSessionDescriptor cdapSessionDescriptor = null;
		if (cdapSession != null){
			cdapSessionDescriptor = cdapSession.getSessionDescriptor();
		}
		NMinusOneFlowDeallocatedEvent event = new NMinusOneFlowDeallocatedEvent(portId, cdapSessionDescriptor);
		this.ribDaemon.deliverEvent(event);
	}
	
	/**
	 * Get the address of the neighbor
	 * @param apNamingInfo the naming info of the neighbor
	 * @return the neighbor's address, or -1 if it could not be found
	 */
	private long getNeighborAddress(ApplicationProcessNamingInfo apNamingInfo){
		List<Neighbor> neighbors = this.ipcProcess.getNeighbors();
		for(int i=0; i<neighbors.size(); i++){
			if (neighbors.get(i).getApplicationProcessName().equals(apNamingInfo.getApplicationProcessName()) && 
					neighbors.get(i).getApplicationProcessInstance().equals(apNamingInfo.getApplicationProcessInstance())){
				return neighbors.get(i).getAddress();
			}
		}
		
		return -1;
	}

	public void deliverStatus(int portId, boolean arg1) {
		// TODO Auto-generated method stub
	}

}
