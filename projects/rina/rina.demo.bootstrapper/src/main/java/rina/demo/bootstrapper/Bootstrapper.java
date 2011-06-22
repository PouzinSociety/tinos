package rina.demo.bootstrapper;

import rina.applicationprocess.api.WhatevercastName;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcprocess.api.IPCProcessFactory;
import rina.ipcservice.api.ApplicationProcessNamingInfo;

/**
 * Instantiates a new IPC Process
 * @author eduardgrasa
 *
 */
public class Bootstrapper {
	
	/**
	 * Injected by Spring
	 * @param ipcProcessFactory
	 */
	public void setIPCProcessFactory(IPCProcessFactory ipcProcessFactory){
		ApplicationProcessNamingInfo apNamingInfo = new ApplicationProcessNamingInfo("i2CAT-Barcelona", "1", null, null);
		IPCProcess ipcProcess = ipcProcessFactory.createIPCProcess(apNamingInfo);
		WhatevercastName dan = new WhatevercastName();
		dan.setName("RINA-Demo.DIF");
		dan.setRule("All members");
		try{
			ipcProcess.addWhatevercastName(dan);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

}
