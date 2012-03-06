package rina.ipcservice.impl.test;

import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonFactory;

public class MockRIBDaemonFactory implements RIBDaemonFactory{

	private RIBDaemon mockRIBDaemon = null;
	
	public RIBDaemon createRIBDaemon(ApplicationProcessNamingInfo arg0) {
		mockRIBDaemon = new MockRIBDaemon();
		return mockRIBDaemon;
	}

	public void destroyRIBDaemon(ApplicationProcessNamingInfo arg0) {
		mockRIBDaemon = null;
	}

	public RIBDaemon getRIBDaemon(ApplicationProcessNamingInfo arg0) {
		return mockRIBDaemon;
	}

}
