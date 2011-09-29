package rina.cdap.api;

import rina.cdap.api.message.CDAPMessage;
import rina.ipcprocess.api.IPCProcessComponent;

/**
 * Manages the creation/deletion of CDAP sessions within an IPC process
 * @author eduardgrasa
 *
 */
public interface CDAPSessionManager extends IPCProcessComponent{
	
	/**
	 * Depending on the message received, it will create a new CDAP state machine (CDAP Session), or update 
	 * an existing one, or terminate one.
	 * @param encodedCDAPMessage
	 * @param portId
	 * @return Decoded CDAP Message
	 * @throws CDAPException if the message is not consistent with the appropriate CDAP state machine
	 */
	public CDAPMessage messageReceived(byte[] encodedCDAPMessage, int portId) throws CDAPException;
	
	/**
	 * Encodes the next CDAP message to be sent, and checks against the 
	 * CDAP state machine that this is a valid message to be sent
	 * @param cdapMessage The cdap message to be serialized
	 * @param portId 
	 * @return encoded version of the CDAP Message
	 * @throws CDAPException
	 */
	public byte[] encodeNextMessageToBeSent(CDAPMessage cdapMessage, int portId) throws CDAPException;
	
	/**
	 * Update the CDAP state machine because we've sent a message through the
	 * flow identified by portId
	 * @param cdapMessage The CDAP message to be serialized
	 * @param portId 
	 * @return encoded version of the CDAP Message
	 * @throws CDAPException
	 */
	public void messageSent(CDAPMessage cdapMessage, int portId) throws CDAPException;
	
	/**
	 * Get a CDAP session that matches the portId
	 * @param portId
	 * @return
	 */
	public CDAPSession getCDAPSession(int portId);
	
	/**
	 * Called by the CDAPSession state machine when the cdap session is terminated
	 * @param portId
	 */
	public void removeCDAPSession(int portId);
	
	/**
	 * Encodes a CDAP message. It just converts a CDAP message into a byte 
	 * array, without caring about what session this CDAP message belongs to (and 
	 * therefore it doesn't update any CDAP session state machine). Called by 
	 * functions that have to relay CDAP messages, and need to 
	 * encode its contents to make the relay decision and maybe modify some 
	 * message values.
	 * @param cdapMessage
	 * @return
	 * @throws CDAPException
	 */
	public byte[] encodeCDAPMessage(CDAPMessage cdapMessage) throws CDAPException;
	
	/**
	 * Decodes a CDAP message. It just converts the byte array into a CDAP 
	 * message, without caring about what session this CDAP message belongs to (and 
	 * therefore it doesn't update any CDAP session state machine). Called by 
	 * functions that have to relay CDAP messages, and need to serialize/
	 * decode its contents to make the relay decision and maybe modify some 
	 * @param cdapMessage
	 * @return
	 * @throws CDAPException
	 */
	public CDAPMessage decodeCDAPMessage(byte[] cdapMessage) throws CDAPException;
	
	/**
	 * Return the portId of the (N-1) Flow that supports the CDAP Session
	 * with the IPC process identified by destinationApplicationProcessName and destinationApplicationProcessInstance
	 * @param destinationApplicationProcessName
	 * @param destinationApplicationProcessInstance
	 * @throws CDAPException
	 */
	public int getPortId(String destinationApplicationProcessName, String destinationApplicationProcessInstance) throws CDAPException;
}
