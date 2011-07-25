package rina.enrollment.impl.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.enrollment.impl.EnrollmentStateMachine;
import rina.enrollment.impl.EnrollmentTaskImpl;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBHandler;

/**
 * Handles the operations related to the "daf.management.enrollment" objects
 * @author eduardgrasa
 *
 */
public class EnrollmentHandler implements RIBHandler{
	
	private static final Log log = LogFactory.getLog(EnrollmentHandler.class);
	
	private EnrollmentTaskImpl enrollmentTask = null;
	
	public EnrollmentHandler(EnrollmentTaskImpl enrollmentTaskImpl){
		this.enrollmentTask = enrollmentTaskImpl;
	}
	
	public void read(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException {
		EnrollmentStateMachine enrollmentStateMachine = enrollmentTask.getEnrollmentStateMachine(cdapSessionDescriptor);
		if (enrollmentStateMachine == null){
			log.error("Got a CDAP message that is not for me: "+cdapMessage.toString());
			return;
		}
		
		enrollmentStateMachine.read(cdapMessage, cdapSessionDescriptor);
	}

	public void cancelRead(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException {
		EnrollmentStateMachine enrollmentStateMachine = enrollmentTask.getEnrollmentStateMachine(cdapSessionDescriptor);
		if (enrollmentStateMachine == null){
			log.error("Got a CDAP message that is not for me: "+cdapMessage.toString());
			return;
		}
		enrollmentStateMachine.cancelread(cdapMessage, cdapSessionDescriptor);
	}

	public void create(CDAPMessage arg0, CDAPSessionDescriptor arg1)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void create(String arg0, String arg1, long arg2, Object arg3)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void delete(CDAPMessage arg0, CDAPSessionDescriptor arg1)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void delete(String arg0, String arg1, long arg2, Object arg3)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public Object read(String arg0, String arg1, long arg2)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		return null;
	}

	public void start(CDAPMessage arg0, CDAPSessionDescriptor arg1)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void start(String arg0, String arg1, long arg2, Object arg3)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void stop(CDAPMessage arg0, CDAPSessionDescriptor arg1)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void stop(String arg0, String arg1, long arg2, Object arg3)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void write(CDAPMessage arg0, CDAPSessionDescriptor arg1)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void write(String arg0, String arg1, long arg2, Object arg3)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

}
