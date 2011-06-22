package rina.demo.bootstrapper;

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
		ApplicationProcessNamingInfo apNamingInfo = new ApplicationProcessNamingInfo("Edu-Barcelona", null, null, null);
		ipcProcessFactory.createIPCProcess(apNamingInfo);
	}

}
