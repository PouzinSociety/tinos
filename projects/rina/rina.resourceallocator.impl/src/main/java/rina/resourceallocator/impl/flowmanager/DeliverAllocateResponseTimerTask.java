package rina.resourceallocator.impl.flowmanager;

import java.util.Map;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.ipcservice.api.APService;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCService;
import rina.resourceallocator.impl.flowmanager.FlowServiceState.Status;

public class DeliverAllocateResponseTimerTask extends TimerTask{

	private static final Log log = LogFactory.getLog(DeliverAllocateResponseTimerTask.class);
	
	private IPCService ipcService = null;
	private FlowService flowService = null;
	private Map<Integer, FlowServiceState> flowServiceStates = null;
	private APService apService = null;
	
	public DeliverAllocateResponseTimerTask(IPCService ipcService, FlowService flowService, Map<Integer, 
			FlowServiceState> flowServiceStates, APService apService){
		this.ipcService = ipcService;
		this.flowService = flowService;
		this.flowServiceStates = flowServiceStates;
		this.apService = apService;
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
			
			//TODO Notify about the event
		}catch(Exception ex){
			log.error("Problems submiting allocate response for N-1 flow identified by portId "+flowService.getPortId()+". "+ex);
		}
	}

}
