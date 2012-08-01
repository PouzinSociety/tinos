package rina.flowallocator.impl.timertasks;

import java.util.Timer;
import java.util.TimerTask;

import rina.efcp.api.DataTransferAE;
import rina.flowallocator.api.Flow;
import rina.flowallocator.api.FlowAllocator;
import rina.ipcmanager.api.IPCManager;

public class DeleteEFCPStateTimerTask extends TimerTask{
	
	private Timer timer = null;
	private FlowAllocator flowAllocator = null;
	private int portId = -1;
	private IPCManager ipcManager = null;
	private Flow flow = null;
	private DataTransferAE dataTransferAE = null;
	
	public DeleteEFCPStateTimerTask(Timer timer, FlowAllocator flowAllocator, IPCManager ipcManager, 
			int portId, Flow flow, DataTransferAE dataTransferAE){
		this.timer = timer;
		this.flowAllocator = flowAllocator;
		this.ipcManager = ipcManager;
		this.portId = portId;
		this.flow = flow;
		this.dataTransferAE = dataTransferAE;
	}

	@Override
	public void run() {
		//1 Cancel any pending timers
		if (timer != null){
			timer.cancel();
		}
		
		//2 Remove the FAI from the Flow Allocator list
		this.flowAllocator.removeFlowAllocatorInstance(this.portId);
		
		// 3 Remove the incoming and outgoing flow queues, remove the DTP and DTCP state
		this.ipcManager.removeFlowQueues(portId);
		for(int i=0; i<flow.getConnectionIds().size(); i++){
			this.dataTransferAE.deleteConnection(flow.getConnectionIds().get(i));
		}
		this.dataTransferAE.freeCEPIds(this.portId);
	}

}
