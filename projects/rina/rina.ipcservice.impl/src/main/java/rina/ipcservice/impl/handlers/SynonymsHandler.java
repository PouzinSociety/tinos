package rina.ipcservice.impl.handlers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.ApplicationProcessNameSynonym;
import rina.ipcservice.impl.IPCProcessImpl;
import rina.ribdaemon.api.RIBDaemonException;

/**
 * Handles the operations related to the "daf.management.naming.synonyms" objects
 * @author eduardgrasa
 *
 */
public class SynonymsHandler extends BaseIPCProcessRIBHandler{
	
	private static final Log log = LogFactory.getLog(SynonymsHandler.class);
	
	public SynonymsHandler(IPCProcessImpl ipcProcess){
		super(ipcProcess);
	}

	public void create(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException{
		//TODO don't have synonym list yet	
	}

	public void delete(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		//TODO don't have synonym list yet
	}

	public Object read(String objectClass, String objectName, long objectInstance) throws RIBDaemonException{
		ApplicationProcessNameSynonym synonym = null;
		List<ApplicationProcessNameSynonym> result = new ArrayList<ApplicationProcessNameSynonym>();
		synonym = new ApplicationProcessNameSynonym();
		synonym.setApplicationProcessName(ipcProcess.getApplicationProcessName());
		result.add(synonym);
		return result;
	}

	public void write(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		//TODO don't have synonym list yet
	}

}
