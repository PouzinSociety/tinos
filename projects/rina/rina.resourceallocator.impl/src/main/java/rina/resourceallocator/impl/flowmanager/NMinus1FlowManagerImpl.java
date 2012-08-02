package rina.resourceallocator.impl.flowmanager;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.BaseCDAPSessionManager;
import rina.cdap.api.CDAPSessionManager;
import rina.events.api.events.NMinusOneFlowDeallocatedEvent;
import rina.ipcmanager.api.IPCManager;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcprocess.api.IPCProcess.IPCProcessType;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCException;
import rina.ipcservice.api.IPCService;
import rina.resourceallocator.api.NMinus1FlowManager;
import rina.resourceallocator.impl.flowmanager.FlowServiceState.Status;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemon;

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
	 * The states of all the ongoing and allocated flows
	 */
	private Map<Integer, FlowServiceState> flowServiceStates = null;
	
	/**
	 * Tells if this IPC Process is registered to an N-1 IPC Process
	 * TODO: Fix this once the IDD is ready.
	 */
	private boolean registered = false;
	
	private Timer timer = null;

	public NMinus1FlowManagerImpl(IPCManager ipcManager){
		this.ipcManager = ipcManager;
		this.flowServiceStates = new ConcurrentHashMap<Integer, FlowServiceState>();
		this.timer = new Timer();
	}
	
	public void setIPCProcess(IPCProcess ipcProcess){
		this.ipcProcess = ipcProcess;
		this.ribDaemon = (RIBDaemon) ipcProcess.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		this.cdapSessionManager = (CDAPSessionManager) ipcProcess.getIPCProcessComponent(BaseCDAPSessionManager.getComponentName());
	}
	
	@Override
	/**
	 * Request the allocation of an N-1 Flow with the requested QoS 
	 * to the destination IPC Process 
	 * @param flowService contains the destination IPC Process and requested QoS information
	 */
	public void allocateNMinus1Flow(FlowService flowService){
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
			flowServiceState = new FlowServiceState();
			flowServiceState.setFlowService(flowService);
			flowServiceState.setIpcService(ipcService);
			flowServiceState.setStatus(Status.ALLOCATION_REQUESTED);
			this.flowServiceStates.put(new Integer(portId), flowServiceState);
		}catch(Exception ex){
			log.error("Issues allocating an N-1 flow to "+flowService+". Details: "+ex);
		}
	}

	@Override
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
		
		//Notify about the event
		NMinusOneFlowDeallocatedEvent event = new NMinusOneFlowDeallocatedEvent(portId, 
				cdapSessionManager.getCDAPSession(portId).getSessionDescriptor());
		this.ribDaemon.deliverEvent(event);
	}
	
	/**
	 * Register the IPC Process to one or more N-1 DIFs
	 * @param difNames The list of N-1 DIFs where this IPC Process will register
	 * @throws IPCException
	 */
	public void registerIPCProcess(List<String> difNames) throws IPCException{
		//TODO do it right. Currently it will just register with the first shim 
		//IPC Process for IP networks it finds
		if (this.registered){
			throw new IPCException(IPCException.PROBLEMS_REGISTERING_APPLICATION_CODE, 
			"The IPC Process is already registered");
		}
		
		IPCService ipcService = null;
		
		List<IPCProcess> candidates = this.ipcManager.listIPCProcesses();
		IPCProcess currentCandidate = null;
		for(int i=0; i<candidates.size(); i++){
			currentCandidate = candidates.get(i);
			if (currentCandidate.getType().equals(IPCProcessType.SHIM_IP)){
				ipcService = (IPCService) currentCandidate;
			}
		}
		
		if (ipcService == null){
			throw new IPCException(IPCException.PROBLEMS_REGISTERING_APPLICATION_CODE, 
					"Problems registering IPC Process: Could not find any N-1 IPC Process to register at.");
		}
		
		ipcService.register(this.ipcProcess.getApplicationProcessNamingInfo(), this);
		this.registered = true;
	}

	@Override
	public String deliverAllocateRequest(FlowService flowService, IPCService ipcService) {
		if (flowService.getDestinationAPNamingInfo().getApplicationProcessName().equals(this.ipcProcess.getApplicationProcessName()) 
				&& flowService.getDestinationAPNamingInfo().getApplicationProcessInstance().equals(this.ipcProcess.getApplicationProcessInstance())){
			//Accept the request
			try{
				DeliverAllocateResponseTimerTask timerTask =  new DeliverAllocateResponseTimerTask(
						ipcService, flowService, flowServiceStates, this);
				this.timer.schedule(timerTask, 10);
			}catch(Exception ex){
				log.error("Problems submiting allocate response for N-1 flow identified by portId "+flowService.getPortId()+". "+ex);
			}
			
			return null;
		}else{
			return "This IPC Process is not the intended destination of this flow";
		}
	}

	@Override
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
			//TODO Notify about the event?
		}else{
			log.error("Allocation of N-1 flow identified by portId "+ portId + " denied because "+resultReason);
			this.flowServiceStates.remove(new Integer(portId));
			//TODO Notify about the event?
		}
	}

	@Override
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
		
		//Notify about the event
		NMinusOneFlowDeallocatedEvent event = new NMinusOneFlowDeallocatedEvent(portId, 
				cdapSessionManager.getCDAPSession(portId).getSessionDescriptor());
		this.ribDaemon.deliverEvent(event);
	}

	@Override
	public void deliverStatus(int portId, boolean arg1) {
		// TODO Auto-generated method stub
	}

}
