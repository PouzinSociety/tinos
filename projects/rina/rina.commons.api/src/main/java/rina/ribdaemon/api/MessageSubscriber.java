package rina.ribdaemon.api;

import rina.cdap.api.message.CDAPMessage;

/**
 * 
 * @author eduardgrasa
 */
public interface MessageSubscriber {
	
	/**
	 * Called when a cdapMessage related to an object class we've subscribed to
	 * is received by the RIB Daemon
	 * @param cdapMessage
	 */
	public void messageReceived(CDAPMessage cdapMessage);
}
