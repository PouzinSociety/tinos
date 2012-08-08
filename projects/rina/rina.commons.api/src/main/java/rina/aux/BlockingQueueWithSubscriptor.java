package rina.aux;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import rina.ipcservice.api.IPCException;

/**
 * Represents a set of byte[] blocking queues identified by an Integer.
 * A special queue queues the identifiers of the queues that have data but 
 * have not been read yet.
 * Every time a new byte[] is written to one of the queues, a new
 * @author eduardgrasa
 *
 */
public class BlockingQueueWithSubscriptor<T> {
	
	/**
	 * The class that is subscribed to this queue and 
	 * will be informed every time there is data available
	 * to be read
	 */
	private QueueSubscriptor queueSubscriptor = null;
	
	/**
	 * The queue
	 */
	private BlockingQueue<T> dataQueue = null;
	
	/**
	 * The id of the queue
	 */
	private int queueId = 0;
	
	/**
	 * Constructs the queue with a queueId and a capacity
	 * @param queueId the identifier of the queue
	 * @param queueCapacity the capacity of the queue
	 */
	public BlockingQueueWithSubscriptor(int queueId, int queueCapacity){
		this.queueId = queueId;
		this.dataQueue = new ArrayBlockingQueue<T>(queueCapacity);
	}
	
	public void subscribeToQueue(QueueSubscriptor queueSubscriptor){
		this.queueSubscriptor = queueSubscriptor;
	}
	
	public BlockingQueue<T> getDataQueue(){
		return dataQueue;
	}
	
	/**
	 * Write the data byte array to the queue. The 
	 * queue subscriptor will be notified of new data available
	 * @param identifier
	 * @param data
	 * @throws IPCException
	 */
	public void writeDataToQueue(T data) throws IPCException{
		try{
			dataQueue.put(data);
			if (this.queueSubscriptor != null){
				this.queueSubscriptor.queueReadyToBeRead(this.queueId);
			}
		}catch(Exception ex){
			throw new IPCException("Problems writing data to queue "+this.queueId+". "+ex.getMessage());
		}
	}
	
	/**
	 * Returns the data in the queue. The thread calling this operation
	 * will block until data is available in the queue
	 * @return
	 * @throws InterruptedException
	 */
	public T take() throws InterruptedException{
		return this.dataQueue.take();
	}
}
