package rina.enrollment.impl.handlers;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.enrollment.impl.EnrollmentTaskImpl;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBHandler;

/**
 * Handles the operations related to the "daf.management.operationalStatus" objects
 * @author eduardgrasa
 *
 */
public class OperationalStatusHandler implements RIBHandler{

	private EnrollmentTaskImpl enrollmentTask = null;

	public OperationalStatusHandler(EnrollmentTaskImpl enrollmentTaskImpl){
		this.enrollmentTask = enrollmentTaskImpl;
	}

	public void cancelRead(CDAPMessage arg0, CDAPSessionDescriptor arg1)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
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

	public void read(CDAPMessage arg0, CDAPSessionDescriptor arg1)
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
