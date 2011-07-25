package rina.ipcservice.impl.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.ApplicationProcessNameSynonym;
import rina.ipcservice.impl.IPCProcessImpl;
import rina.ribdaemon.api.RIBDaemonException;

/**
 * Handles the operations related to the "daf.management.naming.currentsynonym" objects
 * @author eduardgrasa
 *
 */
public class CurrentSynonymHandler extends BaseIPCProcessRIBHandler{
	
	private static final Log log = LogFactory.getLog(CurrentSynonymHandler.class);
	
	public CurrentSynonymHandler(IPCProcessImpl ipcProcess){
		super(ipcProcess);
	}

	public void create(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		throw new RIBDaemonException(RIBDaemonException.OPERATION_NOT_ALLOWED_AT_THIS_OBJECT, "Operation create not allowed for objectName "+objectName);
	}

	public void delete(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		ipcProcess.setCurrentSynonym(null);
	}

	public Object read(String objectClass, String objectName, long objectInstance) throws RIBDaemonException{
		ApplicationProcessNameSynonym synonym = new ApplicationProcessNameSynonym();
		synonym.setApplicationProcessName(ipcProcess.getApplicationProcessName());
		synonym.setSynonym(ipcProcess.getCurrentSynonym());
		return synonym;
	}

	public void write(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		if (!(object instanceof byte[])){
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
					"Object class ("+object.getClass().getName()+") does not match object name "+objectName);
		}
	}

}
