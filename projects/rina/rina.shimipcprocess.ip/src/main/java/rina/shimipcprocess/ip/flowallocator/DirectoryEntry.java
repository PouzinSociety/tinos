package rina.shimipcprocess.ip.flowallocator;

import rina.applicationprocess.api.ApplicationProcessNamingInfo;

/**
 * The directory that contains the IP address and port 
 * of the registered applications that the Shim IPC Process
 * knows
 * @author eduardgrasa
 *
 */
public class DirectoryEntry {
	
	/**
	 * The application process naming information
	 */
	private ApplicationProcessNamingInfo apNamingInfo = null;
	
	/**
	 * The hostname of the shim IPC Process the application is registered at
	 */
	private String hostname = null;
	
	/**
	 * The TCP/UPD port the shim IPC Process is listening to on behalf of 
	 * the registered application
	 */
	private int portNumber = 0;

	public ApplicationProcessNamingInfo getApNamingInfo() {
		return apNamingInfo;
	}

	public void setApNamingInfo(ApplicationProcessNamingInfo apNamingInfo) {
		this.apNamingInfo = apNamingInfo;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}
}
