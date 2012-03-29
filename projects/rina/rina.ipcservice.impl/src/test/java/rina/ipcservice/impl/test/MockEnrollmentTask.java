package rina.ipcservice.impl.test;

import java.util.List;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.enrollment.api.BaseEnrollmentTask;
import rina.enrollment.api.Neighbor;
import rina.applicationprocess.api.ApplicationProcessNamingInfo;

public class MockEnrollmentTask extends BaseEnrollmentTask{

	public void connect(CDAPMessage arg0, CDAPSessionDescriptor arg1) {
		// TODO Auto-generated method stub
		
	}

	public void connectResponse(CDAPMessage arg0, CDAPSessionDescriptor arg1) {
		// TODO Auto-generated method stub
		
	}

	public void enrollmentCompleted(Neighbor arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}

	public void enrollmentFailed(ApplicationProcessNamingInfo arg0, int arg1,
			String arg2, boolean arg3, boolean arg4) {
		// TODO Auto-generated method stub
		
	}

	public void flowDeallocated(CDAPSessionDescriptor arg0) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Starts the enrollment program
	 * @param cdapMessage
	 * @param cdapSessionDescriptor
	 */
	public void initiateEnrollment(Neighbor candidate){
		// TODO Auto-generated method stub
		
	}

	public void release(CDAPMessage arg0, CDAPSessionDescriptor arg1) {
		// TODO Auto-generated method stub
		
	}

	public void releaseResponse(CDAPMessage arg0, CDAPSessionDescriptor arg1) {
		// TODO Auto-generated method stub
		
	}

	public List<String> getEnrolledIPCProcessNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isEnrolledTo(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
