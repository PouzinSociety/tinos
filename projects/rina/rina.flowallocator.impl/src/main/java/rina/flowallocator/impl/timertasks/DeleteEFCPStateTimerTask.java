package rina.flowallocator.impl.timertasks;

import java.util.Timer;
import java.util.TimerTask;

import rina.aux.BlockingQueueSet;
import rina.efcp.api.DataTransferAE;
import rina.flowallocator.api.Flow;
import rina.flowallocator.api.FlowAllocator;

public class DeleteEFCPStateTimerTask extends TimerTask{
	
	private Timer timer = null;
	private FlowAllocator flowAllocator = null;
	private int portId = -1;
	private BlockingQueueSet incomingFlowQueues = null;
	private Flow flow = null;
	private DataTransferAE dataTransferAE = null;
	
	public DeleteEFCPStateTimerTask(Timer timer, FlowAllocator flowAllocator, BlockingQueueSet incomingFlowQueues, 
			int portId, Flow flow, DataTransferAE dataTransferAE){
		this.timer = timer;
		this.flowAllocator = flowAllocator;
		this.incomingFlowQueues = incomingFlowQueues;
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
		
		// 3 Remove the incoming flow queue, remove the DTP and DTCP state
		this.incomingFlowQueues.removeDataQueue(new Integer(portId));
		for(int i=0; i<flow.getConnectionIds().size(); i++){
			this.dataTransferAE.deleteConnection(flow.getConnectionIds().get(i));
		}
		this.dataTransferAE.freeCEPIds(this.portId);
	}

}
