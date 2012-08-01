package rina.shimipcprocess.ip;

import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.ipcmanager.api.IPCManager;
import rina.shimipcprocess.ip.flowallocator.FlowAllocatorImpl;

public class BlockinqQueueReader implements Runnable{
	
	private static final Log log = LogFactory.getLog(BlockinqQueueReader.class);

	private IPCManager ipcManager = null;
	private int portId = -1;
	private FlowAllocatorImpl flowAllocator = null;
	private BlockingQueue<byte[]> blockingQueue = null;
	private boolean end = false;
	
	public BlockinqQueueReader(BlockingQueue<byte[]> blockingQueue, IPCManager ipcManager, int portId, FlowAllocatorImpl flowAllocator) {
		this.blockingQueue = blockingQueue;
		this.ipcManager = ipcManager;
		this.portId = portId;
		this.flowAllocator = flowAllocator;
	}
	
	public void stop(){
		this.end = true;
		try{
			blockingQueue.put(new byte[0]);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void run(){
		byte[] sdu = null;
		
		log.debug("Started blocking queue reader");
		while(true){
			try{
				sdu = blockingQueue.take();
				if (end){
					break;
				}
				this.ipcManager.getOutgoingFlowQueue(this.portId).writeDataToQueue(sdu);
			}catch(Exception ex){
				log.error(ex);
			}
		}
		
		flowAllocator.socketClosed(this.portId);
	}

}
