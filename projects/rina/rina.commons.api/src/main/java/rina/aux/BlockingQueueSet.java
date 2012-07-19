package rina.aux;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import rina.ipcservice.api.IPCException;

/**
 * Represents a set of byte[] blocking queues identified by an Integer.
 * A special queue queues the identifiers of the queues that have data but 
 * have not been read yet.
 * Every time a new byte[] is written to one of the queues, a new
 * @author eduardgrasa
 *
 */
public class BlockingQueueSet {
	
	/**
	 * The queue that keeps the identifiers of the 
	 * queues that contain pending data to be read.
	 */
	private BlockingQueue<Integer> dataReadyQueue = null;
	
	/**
	 * The queues that contain the incoming each SDUs 
	 * for the different flows, indexed by portId
	 */
	private Map<Integer, BlockingQueue<byte[]>> dataQueues = null;
	
	public BlockingQueueSet(){
		this.dataReadyQueue = new LinkedBlockingQueue<Integer>();
		this.dataQueues = new ConcurrentHashMap<Integer, BlockingQueue<byte[]>>();
	}

	public void addDataQueue(Integer identifier, BlockingQueue<byte[]> dataQueue){
		this.dataQueues.put(identifier, dataQueue);
	}
	
	public void removeDataQueue(Integer identifier){
		this.dataQueues.remove(identifier);
	}
	
	public BlockingQueue<Integer> getDataReadyQueue(){
		return this.dataReadyQueue;
	}
	
	public BlockingQueue<byte[]> getDataQueue(Integer identifier){
		return dataQueues.get(identifier);
	}
	
	/**
	 * Write the data byte array to the queue identified by the 'identifier' attribute. The 
	 * dataReadyQueue will be updated by writing the 'identifier' attribute to it.
	 * @param identifier
	 * @param data
	 * @throws IPCException if no queue identified by the 'identifier' attribute exists
	 */
	public void writeDataToQueue(Integer identifier, byte[] data) throws IPCException{
		BlockingQueue<byte[]> dataQueue = this.dataQueues.get(identifier);
		if (dataQueue == null){
			throw new IPCException("Could not find data queue with identifier "+identifier);
		}
		
		try{
			dataQueue.put(data);
			this.dataReadyQueue.put(identifier);
		}catch(Exception ex){
			throw new IPCException("Problems writing data to queue "+identifier+". "+ex.getMessage());
		}
	}
	
	/**
	 * Returns the identifier of the queue that has data ready to be read. This operation 
	 * will block if there is no data available in any of the queues, until there is at least 
	 * a byte[] in one of the queues in the queue set.
	 * @return
	 * @throws InterruptedException
	 */
	public Integer select() throws InterruptedException{
		return this.dataReadyQueue.take();
	}
}
