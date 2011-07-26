package rina.enrollment.impl.ribobjects;

import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.enrollment.impl.EnrollmentStateMachine;
import rina.enrollment.impl.EnrollmentTaskImpl;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * Handles the operations related to the "daf.management.enrollment" objects
 * @author eduardgrasa
 *
 */
public class EnrollmentRIBObject extends BaseRIBObject{
	
	private static final Log log = LogFactory.getLog(EnrollmentRIBObject.class);
	
	private EnrollmentTaskImpl enrollmentTask = null;
	
	public EnrollmentRIBObject(EnrollmentTaskImpl enrollmentTaskImpl, IPCProcess ipcProcess){
		super(ipcProcess, RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.ENROLLMENT, null, Calendar.getInstance().getTimeInMillis());
		this.enrollmentTask = enrollmentTaskImpl;
	}
	
	@Override
	public void read(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException {
		EnrollmentStateMachine enrollmentStateMachine = enrollmentTask.getEnrollmentStateMachine(cdapSessionDescriptor);
		if (enrollmentStateMachine == null){
			log.error("Got a CDAP message that is not for me: "+cdapMessage.toString());
			return;
		}
		
		enrollmentStateMachine.read(cdapMessage, cdapSessionDescriptor);
	}

	@Override
	public void cancelRead(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException {
		EnrollmentStateMachine enrollmentStateMachine = enrollmentTask.getEnrollmentStateMachine(cdapSessionDescriptor);
		if (enrollmentStateMachine == null){
			log.error("Got a CDAP message that is not for me: "+cdapMessage.toString());
			return;
		}
		enrollmentStateMachine.cancelread(cdapMessage, cdapSessionDescriptor);
	}
}
