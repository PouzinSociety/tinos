package rina.flowallocator.impl.ribobjects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.flowallocator.api.DirectoryForwardingTable;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.ObjectInstanceGenerator;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * Handles the operations related to the "daf.management.naming.currentsynonym" objects
 * @author eduardgrasa
 *
 */
public class DirectoryForwardingTableRIBObject extends BaseRIBObject{
	
	private static final Log log = LogFactory.getLog(DirectoryForwardingTableRIBObject.class);
	
	private DirectoryForwardingTable directoryForwardingTable = null;
	
	public DirectoryForwardingTableRIBObject(IPCProcess ipcProcess, DirectoryForwardingTable directoryForwardingTable){
		super(ipcProcess, RIBObjectNames.SEPARATOR + RIBObjectNames.DIF + RIBObjectNames.SEPARATOR + RIBObjectNames.RESOURCE_ALLOCATION + RIBObjectNames.SEPARATOR +
				RIBObjectNames.FLOW_ALLOCATOR + RIBObjectNames.SEPARATOR + RIBObjectNames.DIRECTORY_FORWARDING_TABLE, "directoryforwardingentry set", 
				ObjectInstanceGenerator.getObjectInstance());
		this.directoryForwardingTable = directoryForwardingTable;
	}

	@Override
	public RIBObject read(String objectClass, String objectName, long objectInstance) throws RIBDaemonException{
		return this;
	}

	@Override
	public void write(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		if (!(object instanceof DirectoryForwardingTable)){
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
					"Object class ("+object.getClass().getName()+") does not match object name "+objectName);
		}
		
		this.directoryForwardingTable = (DirectoryForwardingTable) object;
	}
	
	@Override
	public Object getObjectValue(){
		return directoryForwardingTable;
	}

}
