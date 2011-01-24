package rina.cdap.api;

import java.util.List;

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
}
