package rina.resourceallocator.impl.flowmanager;

import java.util.Map;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.events.api.events.NMinusOneFlowAllocatedEvent;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCService;
import rina.protection.api.NullSDUProtectionModule;
import rina.resourceallocator.api.NMinus1FlowDescriptor;
import rina.resourceallocator.api.PDUForwardingTable;
import rina.resourceallocator.impl.ribobjects.NMinus1FlowRIBObject;
import rina.resourceallocator.impl.ribobjects.NMinus1FlowSetRIBObject;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObjectNames;

public class DeliverAllocateResponseTimerTask extends TimerTask{

	private static final Log log = LogFactory.getLog(DeliverAllocateResponseTimerTask.class);
	
	private IPCService ipcService = null;
	private FlowService flowService = null;
	private Map<Integer, NMinus1FlowDescriptor> nMinus1FlowDescriptors = null;
	private APService apService = null;
	private RIBDaemon ribDaemon = null;
	private long destinationAddress = -1;
	private PDUForwardingTable pduForwardingTable = null;
	
	public DeliverAllocateResponseTimerTask(IPCService ipcService, FlowService flowService, Map<Integer, 
			NMinus1FlowDescriptor> nMinus1FlowDescriptors, APService apService, RIBDaemon ribDaemon, 
			long destinationAddress, PDUForwardingTable pduForwardingTable){
		this.ipcService = ipcService;
		this.flowService = flowService;
		this.nMinus1FlowDescriptors = nMinus1FlowDescriptors;
		this.apService = apService;
		this.ribDaemon = ribDaemon;
		this.destinationAddress = destinationAddress;
		this.pduForwardingTable = pduForwardingTable;
	}
	
	@Override
	public void run() {
		try{
			ipcService.submitAllocateResponse(flowService.getPortId(), true, null, apService);
			NMinus1FlowDescriptor nMinus1FlowDescriptor = new NMinus1FlowDescriptor();
			nMinus1FlowDescriptor.setFlowService(flowService);
			nMinus1FlowDescriptor.setIpcService(ipcService);
			nMinus1FlowDescriptor.setStatus(NMinus1FlowDescriptor.Status.ALLOCATED);
			this.nMinus1FlowDescriptors.put(new Integer(flowService.getPortId()), nMinus1FlowDescriptor);
			
			try{
				this.ribDaemon.create(NMinus1FlowRIBObject.N_MINUS_ONE_FLOW_RIB_OBJECT_CLASS, 
						NMinus1FlowSetRIBObject.N_MINUS_ONE_FLOW_SET_RIB_OBJECT_NAME + RIBObjectNames.SEPARATOR + flowService.getPortId(), 
						nMinus1FlowDescriptor);
			}catch(RIBDaemonException ex){
				log.warn("Error creting N Minus One Flow RIB Object", ex);
			}
			
			//TODO Move this to the routing module
			if (!nMinus1FlowDescriptor.isManagement() && destinationAddress != -1){
				int qosId = nMinus1FlowDescriptor.getFlowService().getQoSSpecification().getQosCubeId();
				this.pduForwardingTable.addEntry(destinationAddress, qosId, new int[]{flowService.getPortId()});
			}
			
			//Notify about the event
			nMinus1FlowDescriptor.setPortId(flowService.getPortId());
			//TODO get adequate SDU protection module
			nMinus1FlowDescriptor.setSduProtectionModule(new NullSDUProtectionModule());
			NMinusOneFlowAllocatedEvent event = new NMinusOneFlowAllocatedEvent(nMinus1FlowDescriptor);
			this.ribDaemon.deliverEvent(event);
		}catch(Exception ex){
			log.error("Problems submiting allocate response for N-1 flow identified by portId "+flowService.getPortId()+". "+ex);
			ex.printStackTrace();
		}
	}

}
