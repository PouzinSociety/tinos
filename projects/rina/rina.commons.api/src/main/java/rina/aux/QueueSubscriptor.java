package rina.aux;

/**
 * An interface of a class that is subscribed to a queue
 * @author eduardgrasa
 *
 */
public interface QueueSubscriptor {

	/**
	 * Called when the queue has data to be read
	 * @param queueId the identifier of the queue
	 */
	public void queueReadyToBeRead(int queueId);
}
