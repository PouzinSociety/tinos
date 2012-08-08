package rina.resourceallocator.impl.flowmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.ApplicationProcessNamingInfo;
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
import rina.resourceallocator.api.NMinus1FlowManager;
import rina.resourceallocator.api.PDUForwardingTable;
import rina.resourceallocator.impl.flowmanager.FlowServiceState.Status;
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
	 * The states of all the ongoing and allocated flows
	 */
	private Map<Integer, FlowServiceState> flowServiceStates = null;
	
	/**
	 * The DIFs this IPC Process is registered at
	 */
	private List<String> difRegistrations = null;
	
	/**
	 * The PDU Forwarding table
	 */
	private PDUForwardingTable pduForwardingTable = null;
	
	private Timer timer = null;

	public NMinus1FlowManagerImpl(PDUForwardingTable pduForwardingTable){
		this.flowServiceStates = new ConcurrentHashMap<Integer, FlowServiceState>();
		this.difRegistrations = new ArrayList<String>();
		this.timer = new Timer();
		this.pduForwardingTable = pduForwardingTable;
	}
	
	public void setIPCProcess(IPCProcess ipcProcess){
		this.ipcProcess = ipcProcess;
		this.ipcManager = ipcProcess.getIPCManager();
		this.ribDaemon = (RIBDaemon) ipcProcess.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
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
	 * Request the allocation of an N-1 Flow with the requested QoS 
	 * to the destination IPC Process 
	 * @param flowService contains the destination IPC Process and requested QoS information
	 * @param management true if this flow will be used for layer management, false otherwise
	 */
	public void allocateNMinus1Flow(FlowService flowService, boolean management){
		//TODO, implement properly with the IDD, right now it requests the flow allocation to 
		//the first shim IPC Process for IP networks that it finds.
		IPCService ipcService = null;
		FlowServiceState flowServiceState = null;
		
		List<IPCProcess> candidates = this.ipcManager.listIPCProcesses();
		IPCProcess currentCandidate = null;
		for(int i=0; i<candidates.size(); i++){
			currentCandidate = candidates.get(i);
			if (currentCandidate.getType().equals(IPCProcessType.SHIM_IP)){
				ipcService = (IPCService) currentCandidate;
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
			flowServiceState = new FlowServiceState();
			flowServiceState.setFlowService(flowService);
			flowServiceState.setIpcService(ipcService);
			flowServiceState.setStatus(Status.ALLOCATION_REQUESTED);
			flowServiceState.setManagement(management);
			this.flowServiceStates.put(new Integer(portId), flowServiceState);
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
		FlowServiceState flowServiceState = this.flowServiceStates.remove(new Integer(portId));
		if (flowServiceState == null){
			throw new IPCException(IPCException.PROBLEMS_DEALLOCATING_FLOW_CODE, 
					IPCException.PROBLEMS_DEALLOCATING_FLOW + ". Could not find an N-1 flow identified by portId " + portId);
		}
		
		flowServiceState.getIpcService().submitDeallocate(portId);
		
		try{
			this.ribDaemon.delete(NMinus1FlowRIBObject.N_MINUS_ONE_FLOW_RIB_OBJECT_CLASS, 
					NMinus1FlowSetRIBObject.N_MINUS_ONE_FLOW_SET_RIB_OBJECT_NAME + RIBObjectNames.SEPARATOR + portId);
		}catch(RIBDaemonException ex){
			log.warn("Error deleting N Minus One Flow RIB Object", ex);
		}
		
		//TODO Move this to the routing module
		if (!flowServiceState.isManagement()){
			long destinationAddress = this.getNeighborAddress(flowServiceState.getFlowService().getDestinationAPNamingInfo());
			if (destinationAddress != -1){
				int qosId = flowServiceState.getFlowService().getQoSSpecification().getQosCubeId();
				this.pduForwardingTable.removeEntry(destinationAddress, qosId);
			}
		}
		
		//Notify about the event
		NMinusOneFlowDeallocatedEvent event = new NMinusOneFlowDeallocatedEvent(portId);
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
		
		ipcService.register(this.ipcProcess.getApplicationProcessNamingInfo(), this);
		this.difRegistrations.add(difName);
		try{
			this.ribDaemon.create(DIFRegistrationRIBObject.DIF_REGISTRATION_RIB_OBJECT_CLASS, 
					DIFRegistrationSetRIBObject.DIF_REGISTRATION_SET_RIB_OBJECT_NAME + RIBObjectNames.SEPARATOR + difName, 
					difName);
		}catch(RIBDaemonException ex){
			log.warn("Error creating DIF Registration RIB Object", ex);
		}
	}

	public String deliverAllocateRequest(FlowService flowService, IPCService ipcService) {
		if (flowService.getDestinationAPNamingInfo().getApplicationProcessName().equals(this.ipcProcess.getApplicationProcessName()) 
				&& flowService.getDestinationAPNamingInfo().getApplicationProcessInstance().equals(this.ipcProcess.getApplicationProcessInstance())){
			//Accept the request
			try{
				DeliverAllocateResponseTimerTask timerTask =  new DeliverAllocateResponseTimerTask(
						ipcService, flowService, flowServiceStates, this, this.ribDaemon, 
						this.getNeighborAddress(flowService.getSourceAPNamingInfo()), this.pduForwardingTable);
				this.timer.schedule(timerTask, 10);
			}catch(Exception ex){
				log.error("Problems submiting allocate response for N-1 flow identified by portId "+flowService.getPortId()+". "+ex);
			}
			
			return null;
		}else{
			return "This IPC Process is not the intended destination of this flow";
		}
	}

	public void deliverAllocateResponse(int portId, int result, String resultReason) {
		FlowServiceState flowServiceState = this.flowServiceStates.get(new Integer(portId));
		if (flowServiceState == null){
			log.warn("Received an allocation notification of an N-1 flow that I was not aware of: "+portId);
			return;
		}
		
		if (!flowServiceState.getStatus().equals(Status.ALLOCATION_REQUESTED)){
			log.warn("Received an allocation notification of an N-1 flow " +
					"whose status was not ALLOCATION_REQUESTED. "+portId);
		}
		
		if (result == 0){
			flowServiceState.setStatus(Status.ALLOCATED);
			try{
				this.ribDaemon.create(NMinus1FlowRIBObject.N_MINUS_ONE_FLOW_RIB_OBJECT_CLASS, 
						NMinus1FlowSetRIBObject.N_MINUS_ONE_FLOW_SET_RIB_OBJECT_NAME + RIBObjectNames.SEPARATOR + portId, 
						flowServiceState.getFlowService());
			}catch(RIBDaemonException ex){
				log.warn("Error creating N Minus One Flow RIB Object", ex);
			}
			
			//TODO Move this to the routing module
			if (!flowServiceState.isManagement()){
				long destinationAddress = this.getNeighborAddress(flowServiceState.getFlowService().getDestinationAPNamingInfo());
				if (destinationAddress != -1){
					int qosId = flowServiceState.getFlowService().getQoSSpecification().getQosCubeId();
					this.pduForwardingTable.addEntry(destinationAddress, qosId, new int[]{portId});
				}
			}
			
			//Notify about the event
			NMinusOneFlowAllocatedEvent event = new NMinusOneFlowAllocatedEvent(portId, flowServiceState.getFlowService());
			this.ribDaemon.deliverEvent(event);
		}else{
			log.error("Allocation of N-1 flow identified by portId "+ portId + " denied because "+resultReason);
			this.flowServiceStates.remove(new Integer(portId));
			
			//Notify about the event
			NMinusOneFlowAllocationFailedEvent event = new NMinusOneFlowAllocationFailedEvent(
					portId, flowServiceState.getFlowService(), resultReason);
			this.ribDaemon.deliverEvent(event);
		}
	}

	/**
	 * The N-1 flow identified by portId has been deallocated. Generate an N-1 Flow 
	 * deallocated event to trigger a Forwarding table recalculation
	 */
	public void deliverDeallocate(int portId) {
		FlowServiceState flowServiceState = this.flowServiceStates.remove(new Integer(portId));
		if (flowServiceState == null){
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
		if (!flowServiceState.isManagement()){
			long destinationAddress = this.getNeighborAddress(flowServiceState.getFlowService().getDestinationAPNamingInfo());
			if (destinationAddress != -1){
				int qosId = flowServiceState.getFlowService().getQoSSpecification().getQosCubeId();
				this.pduForwardingTable.removeEntry(destinationAddress, qosId);
			}
		}
		
		//Notify about the event
		NMinusOneFlowDeallocatedEvent event = new NMinusOneFlowDeallocatedEvent(portId);
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
