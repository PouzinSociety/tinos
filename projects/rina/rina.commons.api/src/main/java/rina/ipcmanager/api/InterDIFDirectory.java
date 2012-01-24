package rina.ipcmanager.api;

import rina.ipcservice.api.ApplicationProcessNamingInfo;

/**
 * Maps application process names to the DIF from where they are available
 * @author eduardgrasa
 *
 */
public interface InterDIFDirectory {

	/**
	 * Maps application process names to the DIF from where they are available
	 * @param apNamingInfo The destination application process naming info (AP Name and optionally AP Instance)
	 * @return
	 */
	public String mapApplicationProcessNamingInfoToDIFName(ApplicationProcessNamingInfo apNamingInfo);
}
