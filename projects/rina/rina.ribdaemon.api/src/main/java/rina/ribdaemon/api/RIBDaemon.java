package rina.ribdaemon.api;

/**
 * Specifies the interface of the RIB Daemon
 * @author eduardgrasa
 */
public interface RIBDaemon {
	
	/**
	 * Invoked by the RMT when it detects a CDAP message. The RIB Daemon has to process the CDAP message and, 
	 * if valid, it will either pass it to interested subscribers and/or write to storage and/or modify other 
	 * tasks data structures. It may be the case that the CDAP message is not addressed to an application 
	 * entity within this IPC process, then the RMT may decide to rely the message to the right destination 
	 * (after consulting an adequate forwarding table).
	 * @param cdapMessage
	 */
	public void cdapMessageDelivered(byte[] cdapMessage);
	
	/**
	 * Interested MessageSubscribers will be called when CDAP messages related to objects of one of the classes 
	 * contained within the objClasses array are received. The same MessageSubscriber can receive messages 
	 * from different types of object classes.
	 * @param objClass
	 * @param objectClasses
	 */
	public void subscribeToMessages(String[] objectClasses, MessageSubscriber messageSubscriber);
	
	/**
	 * Message subscribers will stop receiving CDAP messages related to objects of one of the classes 
	 * contained within the objClasses array
	 * @param objectClasses
	 * @param messageSubscriber
	 */
	public void unsubscribeFromMessages(String[] objectClasses, MessageSubscriber messageSubscriber);
}