package rina.enrollment.impl.ribobjects;

import java.util.Calendar;

import rina.applicationprocess.api.ApplicationProcessNameSynonym;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.enrollment.impl.EnrollmentStateMachine;
import rina.enrollment.impl.EnrollmentTaskImpl;
import rina.enrollment.impl.EnrollmentStateMachine.State;
import rina.ipcprocess.api.IPCProcess;
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
	private EnrollmentTaskImpl enrollmentTask = null;
	
	public CurrentSynonymRIBObject(IPCProcess ipcProcess, EnrollmentTaskImpl enrollmentTask){
		super(ipcProcess, RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.CURRENT_SYNONYM, 
				null, Calendar.getInstance().getTimeInMillis());
		this.enrollmentTask = enrollmentTask;
	}
	
	@Override
	public void read(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException{
		EnrollmentStateMachine enrollmentStateMachine = enrollmentTask.getEnrollmentStateMachine(cdapSessionDescriptor);
		if (enrollmentStateMachine.getState().equals(State.WAITING_READ_ADDRESS)){
			enrollmentStateMachine.read(cdapMessage, cdapSessionDescriptor);
		}else{
			super.read(cdapMessage, cdapSessionDescriptor);
		}
		
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
