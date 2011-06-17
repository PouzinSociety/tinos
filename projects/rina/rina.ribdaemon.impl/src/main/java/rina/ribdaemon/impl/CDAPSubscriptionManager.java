package rina.ribdaemon.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rina.cdap.api.message.CDAPMessage;
import rina.ribdaemon.api.MessageSubscriber;
import rina.ribdaemon.api.MessageSubscription;
import rina.ribdaemon.api.RIBDaemonException;

/**
 * Manages subscriptions to CDAP messages
 * @author eduardgrasa
 *
 */
public class CDAPSubscriptionManager {

	/** All the message subscribers **/
	private Map<MessageSubscription, List<MessageSubscriber>> messageSubscribers = null;
	
	public CDAPSubscriptionManager(){
		messageSubscribers = new HashMap<MessageSubscription, List<MessageSubscriber>>();
	}
	
	/**
	 * Interested MessageSubscribers will be called when CDAP that comply with the 
	 * filter defined by the non-default attributes of the messageSubscription class are received.
	 * @param messageSubscription
	 * @param messageSubscriber
	 * @throws Exception if there's something wrong with the messageSubscription or messageSubscriber is null
	 */
	public void subscribeToMessages(MessageSubscription messageSubscription, MessageSubscriber messageSubscriber) throws RIBDaemonException{
		if (messageSubscription == null || messageSubscriber == null){
			throw new RIBDaemonException(RIBDaemonException.MALFORMED_MESSAGE_SUBSCRIPTION_REQUEST);
		}
		
		List<MessageSubscriber> subscribers = messageSubscribers.get(messageSubscription);
		if (subscribers == null){
			subscribers = new ArrayList<MessageSubscriber>();
			subscribers.add(messageSubscriber);
			messageSubscribers.put(messageSubscription, subscribers);
		}else{
			subscribers.add(messageSubscriber);
		}
	}
	
	/**
	 * MessageSubscribers will stop being called when CDAP messages that comply with the 
	 * filter defined by the non-default attributes of the messageSubscription class are received.
	 * @param messageSubscription
	 * @param messageSubscriber
	 * @throws Exception if there's something wrong with the messageSubscription or messageSubscriber is null, or the 
	 * messageSubscription does not exist
	 */
	public void unsubscribeFromMessages(MessageSubscription messageSubscription, MessageSubscriber messageSubscriber) throws RIBDaemonException {
		if (messageSubscription == null || messageSubscriber == null){
			throw new RIBDaemonException(RIBDaemonException.MALFORMED_MESSAGE_UNSUBSCRIPTION_REQUEST);
		}
		
		List<MessageSubscriber> subscribers = messageSubscribers.get(messageSubscription);
		if (subscribers == null){
			throw new RIBDaemonException(RIBDaemonException.SUBSCRIBER_WAS_NOT_SUBSCRIBED);
		}
		
		subscribers.remove(messageSubscriber);
		if (subscribers.size() == 0){
			messageSubscribers.remove(messageSubscription);
		}
	}
	
	/**
	 * Return a list of the subscribers subscribed to the provided CDAP message
	 * @param cdapMessage
	 * @return
	 */
	public List<MessageSubscriber> getSubscribersForMessage(CDAPMessage cdapMessage){
		Iterator<MessageSubscription> messageSubscriptions = this.messageSubscribers.keySet().iterator();
		List<MessageSubscriber> currentMessageSubscribers = new ArrayList<MessageSubscriber>();
		MessageSubscription currentSubscription = null;
		
		while(messageSubscriptions.hasNext()){
			currentSubscription = messageSubscriptions.next();
			if (currentSubscription.isSubscribedToMessage(cdapMessage)){
				currentMessageSubscribers.addAll(this.messageSubscribers.get(currentSubscription));
			}
		}
		
		return currentMessageSubscribers;
	}
}
