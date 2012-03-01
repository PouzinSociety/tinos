package rina.enrollment.impl.test.addressmanager;

import rina.applicationprocess.api.DAFMember;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.enrollment.api.AddressManager;
import rina.enrollment.api.BaseEnrollmentTask;
import rina.ipcservice.api.ApplicationProcessNamingInfo;

public class MockEnrollmentTask extends BaseEnrollmentTask{

	public void connect(CDAPMessage arg0, CDAPSessionDescriptor arg1) {
		// TODO Auto-generated method stub
		
	}

	public void connectResponse(CDAPMessage arg0, CDAPSessionDescriptor arg1) {
		// TODO Auto-generated method stub
		
	}

	public void enrollmentCompleted(DAFMember arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}

	public void enrollmentFailed(ApplicationProcessNamingInfo arg0, int arg1,
			String arg2, boolean arg3, boolean arg4) {
		// TODO Auto-generated method stub
		
	}

	public void flowDeallocated(CDAPSessionDescriptor arg0) {
		// TODO Auto-generated method stub
		
	}

	public AddressManager getAddressManager() {
		// TODO Auto-generated method stub
		return null;
	}

	public void initiateEnrollment(CDAPMessage arg0, CDAPSessionDescriptor arg1) {
		// TODO Auto-generated method stub
		
	}

	public void release(CDAPMessage arg0, CDAPSessionDescriptor arg1) {
		// TODO Auto-generated method stub
		
	}

	public void releaseResponse(CDAPMessage arg0, CDAPSessionDescriptor arg1) {
		// TODO Auto-generated method stub
		
	}

}
