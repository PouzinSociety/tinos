package rina.cdap.api;

import rina.cdap.api.message.CDAPMessage;

/**
 * Handles CDAP messages
 * @author eduardgrasa
 *
 */
public interface CDAPMessageHandler {
	
	/**
	 * Handles a CDAP message (does something with it)
	 * @param cdapMessage The CDAP message received
	 * @param cdapSessionDescriptor Describes the CDAP session to where the CDAP message belongs
	 */
	public void processMessage(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor);
}
