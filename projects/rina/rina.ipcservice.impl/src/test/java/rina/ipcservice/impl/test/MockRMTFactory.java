package rina.ipcservice.impl.test;

import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.rmt.api.RMT;
import rina.rmt.api.RMTFactory;

public class MockRMTFactory implements RMTFactory{
	
	private RMT mockRMT = null;

	public RMT createRMT(ApplicationProcessNamingInfo arg0) {
		mockRMT = new MockRMT();
		return mockRMT;
	}

	public void destroyRMT(ApplicationProcessNamingInfo arg0) {
		mockRMT = null;
	}

	public RMT getRMT(ApplicationProcessNamingInfo arg0) {
		return mockRMT;
	}

}
