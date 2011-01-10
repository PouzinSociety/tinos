package rina.ribdaemon.api;

import rina.cdap.api.message.CDAPMessage;

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
	 * Interested MessageSubscribers will be called when CDAP that comply with the 
	 * filter defined by the non-default attributes of the messageSubscription class are received.
	 * @param messageSubscription
	 * @param messageSubscriber
	 * @throws Exception if there's something wrong with the messageSubscription or messageSubscriber is null
	 */
	public void subscribeToMessages(MessageSubscription messageSubscription, MessageSubscriber messageSubscriber) throws Exception;
	
	/**
	 * Uninterested MessageSubscribers will be called when CDAP that comply with the 
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