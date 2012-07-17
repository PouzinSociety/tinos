package rina.flowallocator.impl.timertasks;

import java.util.TimerTask;

import rina.flowallocator.impl.FlowAllocatorInstanceImpl;

public class LastSDUSentTimerTask extends TimerTask{
	
	public static final int DELAY = 5*1000;
	
	private FlowAllocatorInstanceImpl flowAllocatorInstance = null;

	public LastSDUSentTimerTask(FlowAllocatorInstanceImpl flowAllocatorInstance){
		this.flowAllocatorInstance = flowAllocatorInstance;
	}

	@Override
	public void run() {
		flowAllocatorInstance.socketClosed();
	}
}
