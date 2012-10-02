package rina.resourceallocator.impl.flowmanager;

import java.util.Map;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.events.api.events.NMinusOneFlowAllocatedEvent;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCService;
import rina.ipcprocess.api.IPCProcess;
import rina.protection.api.BaseSDUProtectionModuleRepository;
import rina.protection.api.SDUProtectionModuleRepository;
import rina.resourceallocator.api.NMinus1FlowDescriptor;
import rina.resourceallocator.api.NMinus1FlowManager;
import rina.resourceallocator.impl.ribobjects.NMinus1FlowRIBObject;
import rina.resourceallocator.impl.ribobjects.NMinus1FlowSetRIBObject;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObjectNames;

public class DeliverAllocateResponseTimerTask extends TimerTask{

	private static final Log log = LogFactory.getLog(DeliverAllocateResponseTimerTask.class);
	
	private IPCService nMinus1ipcService = null;
	private IPCProcess ipcProcess = null;
	private FlowService flowService = null;
	private Map<Integer, NMinus1FlowDescriptor> nMinus1FlowDescriptors = null;
	private APService apService = null;
	private RIBDaemon ribDaemon = null;
	private NMinus1FlowManager nMinus1FlowManager = null;
	private String reason = null;
	private boolean management = false;
	
	public DeliverAllocateResponseTimerTask(IPCService nMinus1ipcService, IPCProcess ipcProcess, FlowService flowService, Map<Integer, 
			NMinus1FlowDescriptor> nMinus1FlowDescriptors, APService apService, RIBDaemon ribDaemon, 
			NMinus1FlowManager nMinus1FlowManager, String reason, boolean management){
		this.nMinus1ipcService = nMinus1ipcService;
		this.ipcProcess = ipcProcess;
		this.flowService = flowService;
		this.nMinus1FlowDescriptors = nMinus1FlowDescriptors;
		this.apService = apService;
		this.ribDaemon = ribDaemon;
		this.nMinus1FlowManager = nMinus1FlowManager;
		this.reason = reason;
		this.management = management;
	}
	
	@Override
	public void run() {
		try{
			if (reason == null){
				nMinus1ipcService.submitAllocateResponse(flowService.getPortId(), true, null, apService);
				NMinus1FlowDescriptor nMinus1FlowDescriptor = new NMinus1FlowDescriptor();
				nMinus1FlowDescriptor.setFlowService(flowService);
				nMinus1FlowDescriptor.setIpcService(nMinus1ipcService);
				nMinus1FlowDescriptor.setStatus(NMinus1FlowDescriptor.Status.ALLOCATED);
				nMinus1FlowDescriptor.setnMinus1DIFName(((IPCProcess)nMinus1ipcService).getDIFName());
				nMinus1FlowDescriptor.setPortId(flowService.getPortId());
				nMinus1FlowDescriptor.setManagement(management);
				this.nMinus1FlowDescriptors.put(new Integer(flowService.getPortId()), nMinus1FlowDescriptor);

				try{
					this.ribDaemon.create(NMinus1FlowRIBObject.N_MINUS_ONE_FLOW_RIB_OBJECT_CLASS, 
							NMinus1FlowSetRIBObject.N_MINUS_ONE_FLOW_SET_RIB_OBJECT_NAME + RIBObjectNames.SEPARATOR + flowService.getPortId(), 
							nMinus1FlowDescriptor);
				}catch(RIBDaemonException ex){
					log.warn("Error creting N Minus One Flow RIB Object", ex);
				}

				//Get adequate SDU protection module
				try{
					SDUProtectionModuleRepository sduProc = (SDUProtectionModuleRepository) ipcProcess.getIPCProcessComponent(BaseSDUProtectionModuleRepository.getComponentName());
					nMinus1FlowDescriptor.setSduProtectionModule(
							sduProc.getSDUProtectionModule(
									nMinus1FlowManager.getSDUProtectionOption(
											nMinus1FlowDescriptor.getnMinus1DIFName())));
				}catch(Exception ex){
					ex.printStackTrace();
					log.error(ex);
				}

				//Notify about the event
				NMinusOneFlowAllocatedEvent event = new NMinusOneFlowAllocatedEvent(nMinus1FlowDescriptor);
				this.ribDaemon.deliverEvent(event);
			}else{
				nMinus1ipcService.submitAllocateResponse(flowService.getPortId(), false, reason, apService);
			}
		}catch(Exception ex){
			log.error("Problems submiting allocate response for N-1 flow identified by portId "+flowService.getPortId()+". "+ex);
			ex.printStackTrace();
		}
	}

}
