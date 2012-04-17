package rina.flowallocator.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.flowallocator.api.DirectoryForwardingTable;
import rina.flowallocator.api.DirectoryForwardingTableEntry;
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
	
	public long getAddress(ApplicationProcessNamingInfo applicationProcessNamingInfo) {
		String objectName = DIRECTORY_FORWARDING_ENTRY_SET_RIB_OBJECT_NAME + 
				RIBObjectNames.SEPARATOR + applicationProcessNamingInfo.getEncodedString();
		DirectoryForwardingTableEntry entry = null;
		
		log.debug("Looking for application process: " + applicationProcessNamingInfo.getEncodedString());
		
		try{
			RIBObject ribObject = ribDaemon.read(DIRECTORY_FORWARDING_TABLE_ENTRY_RIB_OBJECT_CLASS, objectName);
			entry = (DirectoryForwardingTableEntry) ribObject.getObjectValue();
			return entry.getAddress();
		}catch(RIBDaemonException ex){
			return 0L;
		}
		
	}

}
