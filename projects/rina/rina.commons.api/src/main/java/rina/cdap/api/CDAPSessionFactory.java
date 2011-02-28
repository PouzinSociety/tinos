package rina.cdap.api;

import java.util.List;

import rina.cdap.api.message.CDAPMessage;

/**
 * Manages the creation/deletion of CDAP sessions within an IPC process
 * @author eduardgrasa
 *
 */
public interface CDAPSessionFactory {
	/**
	 * Create a new CDAP Session
	 * @return
	 */
	public CDAPSession createCDAPSession();
	
	/**
	 * Get all the open CDAP sessions
	 * @return
	 */
	public List<CDAPSession> getAllCDAPSessions();
	
	/**
	 * Get a CDAP session that matches the sessionID
	 * @param sessionID
	 * @return
	 */
	public CDAPSession getCDAPSession(String sessionID);
	
	/**
	 * Remove a CDAP session if it is not connected
	 * @param sessionID
	 * @throws CDAPException if the CDAP session is connected
	 */
	public void removeCDAPSession(String sessionID) throws CDAPException;
	
	/**
	 * Serializes a CDAP message. It just converts a CDAP message into a byte 
	 * array, without caring about what session this CDAP message belongs to (and 
	 * therefore it doesn't update any CDAP session state machine). Called by 
	 * functions that have to relay CDAP messages, and need to serialize/
	 * deserialize its contents to make the relay decision and maybe modify some 
	 * message values.
	 * @param cdapMessage
	 * @return
	 * @throws CDAPException
	 */
	public byte[] serializeCDAPMessage(CDAPMessage cdapMessage) throws CDAPException;
	
	/**
	 * Deserializes a CDAP message. It just converts the byte array into a CDAP 
	 * message, without caring about what session this CDAP message belongs to (and 
	 * therefore it doesn't update any CDAP session state machine). Called by 
	 * functions that have to relay CDAP messages, and need to serialize/
	 * deserialize its contents to make the relay decision and maybe modify some 
	 * @param cdapMessage
	 * @return
	 * @throws CDAPException
	 */
	public CDAPMessage deserializeCDAPMessage(byte[] cdapMessage) throws CDAPException;
}
