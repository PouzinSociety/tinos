package rina.resourceallocator.impl.flowmanager;

import java.util.Map;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.events.api.events.NMinusOneFlowAllocatedEvent;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCService;
import rina.resourceallocator.api.PDUForwardingTable;
import rina.resourceallocator.impl.flowmanager.FlowServiceState.Status;
import rina.resourceallocator.impl.ribobjects.NMinus1FlowRIBObject;
import rina.resourceallocator.impl.ribobjects.NMinus1FlowSetRIBObject;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObjectNames;

public class DeliverAllocateResponseTimerTask extends TimerTask{

	private static final Log log = LogFactory.getLog(DeliverAllocateResponseTimerTask.class);
	
	private IPCService ipcService = null;
	private FlowService flowService = null;
	private Map<Integer, FlowServiceState> flowServiceStates = null;
	private APService apService = null;
	private RIBDaemon ribDaemon = null;
	private long destinationAddress = -1;
	private PDUForwardingTable pduForwardingTable = null;
	
	public DeliverAllocateResponseTimerTask(IPCService ipcService, FlowService flowService, Map<Integer, 
			FlowServiceState> flowServiceStates, APService apService, RIBDaemon ribDaemon, 
			long destinationAddress, PDUForwardingTable pduForwardingTable){
		this.ipcService = ipcService;
		this.flowService = flowService;
		this.flowServiceStates = flowServiceStates;
		this.apService = apService;
		this.ribDaemon = ribDaemon;
		this.destinationAddress = destinationAddress;
		this.pduForwardingTable = pduForwardingTable;
	}
	
	@Override
	public void run() {
		try{
			ipcService.submitAllocateResponse(flowService.getPortId(), true, null, apService);
			FlowServiceState flowServiceState = new FlowServiceState();
			flowServiceState.setFlowService(flowService);
			flowServiceState.setIpcService(ipcService);
			flowServiceState.setStatus(Status.ALLOCATED);
			this.flowServiceStates.put(new Integer(flowService.getPortId()), flowServiceState);
			
			try{
				this.ribDaemon.create(NMinus1FlowRIBObject.N_MINUS_ONE_FLOW_RIB_OBJECT_CLASS, 
						NMinus1FlowSetRIBObject.N_MINUS_ONE_FLOW_SET_RIB_OBJECT_NAME + RIBObjectNames.SEPARATOR + flowService.getPortId(), 
						flowService);
			}catch(RIBDaemonException ex){
				log.warn("Error creting N Minus One Flow RIB Object", ex);
			}
			
			//TODO Move this to the routing module
			if (!flowServiceState.isManagement() && destinationAddress != -1){
				int qosId = flowServiceState.getFlowService().getQoSSpecification().getQosCubeId();
				this.pduForwardingTable.addEntry(destinationAddress, qosId, new int[]{flowService.getPortId()});
			}
			
			//Notify about the event
			NMinusOneFlowAllocatedEvent event = new NMinusOneFlowAllocatedEvent(flowService.getPortId(), flowService);
			this.ribDaemon.deliverEvent(event);
		}catch(Exception ex){
			log.error("Problems submiting allocate response for N-1 flow identified by portId "+flowService.getPortId()+". "+ex);
		}
	}

}
