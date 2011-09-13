package rina.enrollment.impl.ribobjects;

import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.ApplicationProcessNameSynonym;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.enrollment.impl.EnrollmentTaskImpl;
import rina.enrollment.impl.statemachines.EnrollmentStateMachine.State;
import rina.enrollment.impl.statemachines.EnrollmentStateMachine;
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
	
	private static final Log log = LogFactory.getLog(CurrentSynonymRIBObject.class);
	
	private ApplicationProcessNameSynonym synonym = null;
	private EnrollmentTaskImpl enrollmentTask = null;
	
	public CurrentSynonymRIBObject(IPCProcess ipcProcess, EnrollmentTaskImpl enrollmentTask){
		super(ipcProcess, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.CURRENT_SYNONYM, 
				null, Calendar.getInstance().getTimeInMillis());
		this.enrollmentTask = enrollmentTask;
	}
	
	@Override
	public void read(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException{
		EnrollmentStateMachine enrollmentStateMachine = null;
		
		try{
			enrollmentStateMachine = enrollmentTask.getEnrollmentStateMachine(cdapSessionDescriptor);
		}catch(Exception ex){
			log.error(ex);
			try{
				enrollmentTask.getRIBDaemon().sendMessage(CDAPMessage.getReleaseConnectionRequestMessage(null, 0), cdapSessionDescriptor.getPortId(), null);
			}catch(Exception e){
				log.error(e);
			}
		}

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
	
	@Override
	public Object getObjectValue(){
		return synonym;
	}

}
