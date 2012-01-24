package rina.ipcmanager.impl.test;

import rina.ipcmanager.api.InterDIFDirectory;
import rina.ipcservice.api.ApplicationProcessNamingInfo;

public class MockInterDIFDirectory implements InterDIFDirectory{

	public String mapApplicationProcessNamingInfoToDIFName(ApplicationProcessNamingInfo apNamingInfo) {
		return "test.DIF";
	}

}
