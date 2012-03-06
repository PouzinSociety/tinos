package rina.ipcservice.impl.test;

import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.CDAPSessionManagerFactory;

public class MockCDAPSessionManagerFactory implements CDAPSessionManagerFactory{
	
	private CDAPSessionManager mockCDAPSessionManager = null;

	public CDAPSessionManager createCDAPSessionManager() {
		mockCDAPSessionManager = new MockCDAPSessionManager();
		return mockCDAPSessionManager;
	}

}
