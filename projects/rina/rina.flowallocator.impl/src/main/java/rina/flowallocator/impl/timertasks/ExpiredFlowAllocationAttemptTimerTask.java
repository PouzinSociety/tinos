package rina.flowallocator.impl.timertasks;

import java.net.Socket;
import java.util.TimerTask;

import rina.flowallocator.impl.FlowAllocatorImpl;
import rina.flowallocator.impl.FlowAllocatorInstanceImpl;

/**
 * This task will look for a certain flow allocation that was in process when the 
 * timertask was created. If the flow allocation is still in process, the timer task 
 * will cancel it and cleanup all the resources tied to the flow allocation
 * @author eduardgrasa
 *
 */
public class ExpiredFlowAllocationAttemptTimerTask extends TimerTask{

	private FlowAllocatorImpl flowAllocator = null;
	
	private int portId = 0;
	
	private long tcpRendezVousId = 0;
	
	private boolean socket = false;
	
	public ExpiredFlowAllocationAttemptTimerTask(
			FlowAllocatorImpl flowAllocator, int portId, long tcpRendezVousId, boolean socket){
		this.flowAllocator = flowAllocator;
		this.portId = portId;
		this.tcpRendezVousId = tcpRendezVousId;
		this.socket = socket;
	}

	@Override
	public void run() {
		if (socket){
			Socket socket = flowAllocator.getPendingSockets().remove(new Long(tcpRendezVousId));
			if (socket != null && socket.isConnected()){
				try{
					socket.close();
				}catch(Exception ex){
				}
			}
		}else{
			FlowAllocatorInstanceImpl instance = (FlowAllocatorInstanceImpl) 
					flowAllocator.getFlowAllocatorInstances().get(new Integer(portId));
			if (instance.getSocket() == null){
				flowAllocator.getFlowAllocatorInstances().remove(new Integer(portId));
			}
		}
		
	}
}
