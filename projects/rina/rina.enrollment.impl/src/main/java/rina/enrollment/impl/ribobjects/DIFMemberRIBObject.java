package rina.enrollment.impl.ribobjects;

import java.util.Calendar;

import rina.applicationprocess.api.ApplicationProcessNameSynonym;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.RIBDaemonException;

public class DIFMemberRIBObject extends BaseRIBObject{

	private ApplicationProcessNameSynonym member = null;
	
	public DIFMemberRIBObject(IPCProcess ipcProcess, String objectName, ApplicationProcessNameSynonym member) {
		super(ipcProcess, objectName, null, Calendar.getInstance().getTimeInMillis());
		this.member = member;
	}
	
	@Override
	public Object read(String objectClass, String objectName, long objectInstance) throws RIBDaemonException{
		return member;
	}
	
	@Override
	public void write(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		if (!(object instanceof ApplicationProcessNameSynonym)){
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
					"Object class ("+object.getClass().getName()+") does not match object name "+objectName);
		}
		
		this.member = (ApplicationProcessNameSynonym) object;
	}
	
	@Override
	public void delete(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		this.getParent().removeChild(objectName);
	}
}
