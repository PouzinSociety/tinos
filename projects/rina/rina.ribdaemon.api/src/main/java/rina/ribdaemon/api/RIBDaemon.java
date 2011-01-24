package rina.ribdaemon.api;

import rina.cdap.api.message.CDAPMessage;

/**
 * Specifies the interface of the RIB Daemon
 * @author eduardgrasa
 */
public interface RIBDaemon {
	
	/**
	 * Gets one or more objects from the RIB, matching the information specified by objectClass, objectInstance, objectName and a template object.
	 * All the parameters but objectClass are optional.
	 * @param objectClass optional if objectInstance specified, mandatory otherwise. A string identifying the class of the object
	 * @param objectInstance optional if objectClass specified, mandatory otherwise. An id that uniquely identifies the object within a RIB
	 * @param objectName optional if objectClass specified, ignored otherwise. An id that uniquely identifies an object within an objectClass
	 * @param template optional (if objectClass + objectName or objectInstance specified, template is ignored). A template object whose fields
	 * have to match the requested object(s)
	 * @return null if no object matching the call arguments can be found, one or more objects otherwise
	 */
	public Object read(String objectClass, long objectInstance, String objectName, Object template);
	
	/**
	 * Store an object to the RIB. This may cause a new object to be created in the RIB, or an existing object to be updated.
	 * @param objectClass optional if objectInstance specified, mandatory otherwise. A string identifying the class of the object
	 * @param objectInstance objectInstance optional if objectClass specified, mandatory otherwise. An id that uniquely identifies the object within a RIB
	 * @param objectName optional if objectClass specified, ignored otherwise. An id that uniquely identifies an object within an objectClass
	 * @param objectToWrite the object to be written to the RIB
	 */
	public void write(String objectClass, long objectInstance, String objectName, Object objectToWrite);
	
	/**
	 * Remove an object from the RIB
	 * @param objectClass optional if objectInstance specified, mandatory otherwise. A string identifying the class of the object
	 * @param objectInstance objectInstance optional if objectClass specified, mandatory otherwise. An id that uniquely identifies the object within a RIB
	 * @param objectName optional if objectClass specified, ignored otherwise. An id that uniquely identifies an object within an objectClass
	 */
	public void remove(String objectClass, long objectInstance, String objectName);
	
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
	 * Interested MessageSubscribers will be called when CDAP that comply with the 
	 * filter defined by the non-default attributes of the messageSubscription class are received.
	 * @param messageSubscription
	 * @param messageSubscriber
	 * @throws Exception if there's something wrong with the messageSubscription or messageSubscriber is null
	 */
	public void subscribeToMessages(MessageSubscription messageSubscription, MessageSubscriber messageSubscriber) throws Exception;
	
	/**
	 * MessageSubscribers will stop being called when CDAP that comply with the 
	 * filter defined by the non-default attributes of the messageSubscription class are received.
	 * @param messageSubscription
	 * @param messageSubscriber
	 * @throws Exception if there's something wrong with the messageSubscription or messageSubscriber is null, or the 
	 * messageSubscription does not exist
	 */
	public void unsubscribeFromMessages(MessageSubscription messageSubscription, MessageSubscriber messageSubscriber) throws Exception;
	
	/**
	 * Send an information update, consisting on a set of CDAP messages, using the updateStrategy update strategy
	 * (on demand, scheduled)
	 * @param cdapMessages
	 * @param updateStrategy
	 */
	public void sendMessages(CDAPMessage[] cdapMessages, UpdateStrategy updateStrategy);
}