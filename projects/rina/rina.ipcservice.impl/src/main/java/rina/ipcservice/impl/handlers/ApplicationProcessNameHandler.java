package rina.ipcservice.impl.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.impl.IPCProcessImpl;
import rina.ribdaemon.api.RIBDaemonException;

/**
 * Handles the operations related to the "daf.management.naming.apname" objects
 * @author eduardgrasa
 *
 */
public class ApplicationProcessNameHandler extends BaseIPCProcessRIBHandler{
	
	private static final Log log = LogFactory.getLog(ApplicationProcessNameHandler.class);
	
	public ApplicationProcessNameHandler(IPCProcessImpl ipcProcess){
		super(ipcProcess);
	}

	public void create(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		throw new RIBDaemonException(RIBDaemonException.OPERATION_NOT_ALLOWED_AT_THIS_OBJECT, "Operation create not allowed for objectName "+objectName);
	}

	public void delete(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		ipcProcess.setApplicationProcessNamingInfo(null);
	}

	public Object read(String objectClass, String objectName, long objectInstance) throws RIBDaemonException{
		return ipcProcess.getApplicationProcessNamingInfo();
	}


	public void write(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		if (!(object instanceof ApplicationProcessNamingInfo)){
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
					"Object class ("+object.getClass().getName()+") does not match object name "+objectName);
		}
	}

}
