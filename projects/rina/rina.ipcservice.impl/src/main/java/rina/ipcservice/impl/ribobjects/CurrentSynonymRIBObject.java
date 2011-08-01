package rina.ipcservice.impl.ribobjects;

import java.util.Calendar;

import rina.applicationprocess.api.ApplicationProcessNameSynonym;
import rina.ipcservice.impl.IPCProcessImpl;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * Handles the operations related to the "daf.management.naming.currentsynonym" objects
 * @author eduardgrasa
 *
 */
public class CurrentSynonymRIBObject extends BaseRIBObject{
	
	private ApplicationProcessNameSynonym synonym = null;
	
	public CurrentSynonymRIBObject(IPCProcessImpl ipcProcess){
		super(ipcProcess, RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.CURRENT_SYNONYM, 
				null, Calendar.getInstance().getTimeInMillis());
	}

	@Override
	public Object read(String objectClass, String objectName, long objectInstance) throws RIBDaemonException{
		return synonym;
	}

	@Override
	public void write(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		if (!(object instanceof ApplicationProcessNameSynonym)){
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
					"Object class ("+object.getClass().getName()+") does not match object name "+objectName);
		}
		
		this.synonym = (ApplicationProcessNameSynonym) object;
	}

}
