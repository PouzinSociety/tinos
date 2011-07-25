package rina.ipcservice.impl.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.WhatevercastName;
import rina.ipcservice.impl.IPCProcessImpl;
import rina.ribdaemon.api.RIBDaemonException;

/**
 * Handles the operations related to the "daf.management.naming.whatevercastnames" objects
 * @author eduardgrasa
 *
 */
public class WhatevercastNamesHandler extends BaseIPCProcessRIBHandler{
	
	private static final Log log = LogFactory.getLog(WhatevercastNamesHandler.class);
	
	public WhatevercastNamesHandler(IPCProcessImpl ipcProcess){
		super(ipcProcess);
	}

	public void create(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException{
		if (!(object instanceof WhatevercastName)){
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
					"Object class ("+object.getClass().getName()+") does not match object name "+objectName);
		}
		ipcProcess.getWhatevercastNames().add((WhatevercastName) object);
	}

	public void delete(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		ipcProcess.getWhatevercastNames().removeAll(ipcProcess.getWhatevercastNames());
	}

	public Object read(String objectClass, String objectName, long objectInstance) throws RIBDaemonException{
		return ipcProcess.getWhatevercastNames();
	}

	public void write(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		throw new RIBDaemonException(RIBDaemonException.OPERATION_NOT_ALLOWED_AT_THIS_OBJECT, "Operation M_WRITE not allowed for objectName "+objectName);
	}

}
