package rina.ipcservice.impl.ribobjects;

import java.util.Calendar;

import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * Handles the operations related to the "daf.management.naming.apname" objects
 * @author eduardgrasa
 *
 */
public class ApplicationProcessNameRIBObject extends BaseRIBObject{
	
	private ApplicationProcessNamingInfo apNamingInfo = null;
	
	public ApplicationProcessNameRIBObject(IPCProcess ipcProcess){
		super(ipcProcess, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.APNAME, 
				"apnaminginfo", Calendar.getInstance().getTimeInMillis());
	}

	@Override
	public RIBObject read(String objectClass, String objectName, long objectInstance) throws RIBDaemonException{
		return this;
	}

	@Override
	public void write(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		if (!(object instanceof ApplicationProcessNamingInfo)){
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
					"Object class ("+object.getClass().getName()+") does not match object name "+objectName);
		}
		
		this.apNamingInfo = (ApplicationProcessNamingInfo) object;
	}
	
	@Override
	public Object getObjectValue(){
		return apNamingInfo;
	}

}
