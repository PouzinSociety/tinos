package rina.shimipcprocess.ip;

import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.ipcservice.api.APService;
import rina.shimipcprocess.ip.flowallocator.FlowAllocatorImpl;

public class BlockinqQueueReader implements Runnable{
	
	private static final Log log = LogFactory.getLog(BlockinqQueueReader.class);

	private APService applicationCallback = null;
	private int portId = -1;
	private FlowAllocatorImpl flowAllocator = null;
	private BlockingQueue<byte[]> blockingQueue = null;
	private boolean end = false;
	
	public BlockinqQueueReader(BlockingQueue<byte[]> blockingQueue, APService applicationCallback, int portId, FlowAllocatorImpl flowAllocator) {
		this.blockingQueue = blockingQueue;
		this.applicationCallback = applicationCallback;
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
				this.applicationCallback.deliverTransfer(this.portId, sdu);
			}catch(Exception ex){
				//TODO what to do?
			}
		}
		
		flowAllocator.socketClosed(this.portId);
	}

}
