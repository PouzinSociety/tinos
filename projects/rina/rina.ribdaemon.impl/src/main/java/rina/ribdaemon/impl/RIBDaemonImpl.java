package rina.ribdaemon.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.BaseCDAPSessionManager;
import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.MessageSubscriber;
import rina.ribdaemon.api.MessageSubscription;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.UpdateStrategy;

/**
 * RIBDaemon that stores the objects in memory
 * @author eduardgrasa
 *
 */
public class RIBDaemonImpl extends BaseRIBDaemon{
	
	private static final Log log = LogFactory.getLog(RIBDaemonImpl.class);
	
	/** All the message subscribers **/
	private CDAPSubscriptionManager cdapSubscriptionManager = null;
	
	/** A simple in memory store **/
	private InMemoryStore store = null;
	
	/** Create, retrieve and delete CDAP sessions **/
	private CDAPSessionManager cdapSessionManager = null;
	
	public RIBDaemonImpl(){
		cdapSubscriptionManager = new CDAPSubscriptionManager();
		store = new InMemoryStore();
	}
	
	private CDAPSessionManager getCDAPSessionManager(){
		if (this.cdapSessionManager == null){
			this.cdapSessionManager = (CDAPSessionManager) getIPCProcess().getIPCProcessComponent(BaseCDAPSessionManager.getComponentName());
		}
		
		return this.cdapSessionManager;
	}

	/**
	 * Invoked by the RMT when it detects a CDAP message. The RIB Daemon has to process the CDAP message and, 
	 * if valid, it will either pass it to interested subscribers and/or write to storage and/or modify other 
	 * tasks data structures. It may be the case that the CDAP message is not addressed to an application 
	 * entity within this IPC process, then the RMT may decide to rely the message to the right destination 
	 * (after consulting an adequate forwarding table).
	 * @param cdapMessage
	 */
	public void cdapMessageDelivered(byte[] encodedCDAPMessage, int portId){
		CDAPMessage cdapMessage = null;
		CDAPSessionDescriptor cdapSessionDescriptor = null;
		
		//1 Decode the message and obtain the CDAP session descriptor
		try{
			cdapMessage = getCDAPSessionManager().messageReceived(encodedCDAPMessage, portId);
			cdapSessionDescriptor = getCDAPSessionManager().getCDAPSession(portId).getSessionDescriptor();
		}catch(CDAPException ex){
			log.error("Error decoding CDAP message: " + ex.getMessage());
			ex.printStackTrace();
			return;
		}
		
		//2 Process the message (send to subscribed people, maybe something else)
		//TODO subscribers will be called sequentially in this thread now, can improve 
		//this later (have a thread pool and call subscribers from different threads)
		List<MessageSubscriber> messageSubscribers = cdapSubscriptionManager.getSubscribersForMessage(cdapMessage);
		for(int i=0; i<messageSubscribers.size(); i++){
			messageSubscribers.get(i).messageReceived(cdapMessage, cdapSessionDescriptor);
		}
	}

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
	public Object read(String objectClass, long objectInstance, String objectName, Object template) throws RIBDaemonException{
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Store an object to the RIB. This may cause a new object to be created in the RIB, or an existing object to be updated.
	 * @param objectClass optional if objectInstance specified, mandatory otherwise. A string identifying the class of the object
	 * @param objectInstance objectInstance optional if objectClass specified, mandatory otherwise. An id that uniquely identifies the object within a RIB
	 * @param objectName optional if objectClass specified, ignored otherwise. An id that uniquely identifies an object within an objectClass
	 * @param objectToWrite the object to be written to the RIB
	 * @throws RIBDaemonException if there are problems performing the "write" operation to the RIB
	 */
	public synchronized void write(String objectClass, long objectInstance, String objectName, Object objectToWrite) throws RIBDaemonException{
		// TODO Auto-generated method stub

	}

	/**
	 * Remove an object from the RIB
	 * @param objectClass optional if objectInstance specified, mandatory otherwise. A string identifying the class of the object
	 * @param objectInstance objectInstance optional if objectClass specified, mandatory otherwise. An id that uniquely identifies the object within a RIB
	 * @param objectName optional if objectClass specified, ignored otherwise. An id that uniquely identifies an object within an objectClass
	 * @throws RIBDaemonException if there are problems removinb the objects from the RIB
	 */
	public synchronized void remove(String objectClass, long objectInstance, String objectName) throws RIBDaemonException{
		
	}

	/**
	 * Send an information update, consisting on a set of CDAP messages, using the updateStrategy update strategy
	 * (on demand, scheduled)
	 * @param cdapMessages
	 * @param updateStrategy
	 */
	public synchronized void sendMessages(CDAPMessage[] cdapMessage, UpdateStrategy arg1) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Interested MessageSubscribers will be called when CDAP that comply with the 
	 * filter defined by the non-default attributes of the messageSubscription class are received.
	 * @param messageSubscription
	 * @param messageSubscriber
	 * @throws Exception if there's something wrong with the messageSubscription or messageSubscriber is null
	 */
	public synchronized void subscribeToMessages(MessageSubscription messageSubscription, MessageSubscriber messageSubscriber) throws RIBDaemonException {
		cdapSubscriptionManager.subscribeToMessages(messageSubscription, messageSubscriber);
	}

	/**
	 * MessageSubscribers will stop being called when CDAP messages that comply with the 
	 * filter defined by the non-default attributes of the messageSubscription class are received.
	 * @param messageSubscription
	 * @param messageSubscriber
	 * @throws Exception if there's something wrong with the messageSubscription or messageSubscriber is null, or the 
	 * messageSubscription does not exist
	 */
	public synchronized void unsubscribeFromMessages(MessageSubscription messageSubscription, MessageSubscriber messageSubscriber) throws RIBDaemonException {
		cdapSubscriptionManager.unsubscribeFromMessages(messageSubscription, messageSubscriber);
	}
}
