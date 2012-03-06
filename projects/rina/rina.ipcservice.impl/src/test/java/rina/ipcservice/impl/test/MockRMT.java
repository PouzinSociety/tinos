package rina.ipcservice.impl.test;

import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.QualityOfServiceSpecification;
import rina.rmt.api.BaseRMT;

public class MockRMT extends BaseRMT{

	public int allocateFlow(ApplicationProcessNamingInfo arg0,
			QualityOfServiceSpecification arg1) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	public void deallocateFlow(int arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public String getIPAddressFromApplicationNamingInformation(String arg0,
			String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public void sendCDAPMessage(int arg0, byte[] arg1) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void sendEFCPPDU(byte[] arg0) {
		// TODO Auto-generated method stub
		
	}

	public void startListening() {
		// TODO Auto-generated method stub
		
	}

}
