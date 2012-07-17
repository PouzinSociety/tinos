package rina.flowallocator.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.flowallocator.api.DirectoryForwardingTable;
import rina.flowallocator.api.DirectoryForwardingTableEntry;
import rina.ipcservice.api.APService;
import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;

public class DirectoryForwardingTableImpl implements DirectoryForwardingTable{
	
	private static final Log log = LogFactory.getLog(DirectoryForwardingTableImpl.class);

	private RIBDaemon ribDaemon = null;
	
	public DirectoryForwardingTableImpl(RIBDaemon ribDaemon){
		this.ribDaemon = ribDaemon;
	}
	
	/**
	 * Returns the address of the IPC process where the application process is, or 
	 * null otherwise
	 * @param apNamingInfo
	 * @return
	 */
	public long getAddress(ApplicationProcessNamingInfo applicationProcessNamingInfo) {
		DirectoryForwardingTableEntry entry = this.getEntry(applicationProcessNamingInfo);
		if (entry!= null){
			return entry.getAddress();
		}else{
			return 0L;
		}
		
	}
	
	/**
	 * Returns the callback to the application registered with this application process naming information
	 * @param applicationProcessNamingInfo
	 * @return
	 */
	public APService getLocalApplicationCallback(ApplicationProcessNamingInfo applicationProcessNamingInfo) {
		DirectoryForwardingTableEntry entry = this.getEntry(applicationProcessNamingInfo);
		if (entry!= null){
			return entry.getLocalApplicationCallback();
		}else{
			return null;
		}
	}
	
	private DirectoryForwardingTableEntry getEntry(ApplicationProcessNamingInfo applicationProcessNamingInfo){
		String objectName = DIRECTORY_FORWARDING_ENTRY_SET_RIB_OBJECT_NAME + 
		RIBObjectNames.SEPARATOR + applicationProcessNamingInfo.getEncodedString();
		DirectoryForwardingTableEntry entry = null;

		log.debug("Looking for application process: " + applicationProcessNamingInfo.getEncodedString());

		try{
			RIBObject ribObject = ribDaemon.read(DIRECTORY_FORWARDING_TABLE_ENTRY_RIB_OBJECT_CLASS, objectName);
			entry = (DirectoryForwardingTableEntry) ribObject.getObjectValue();
			return entry;
		}catch(RIBDaemonException ex){
			return null;
		}
	}

}
