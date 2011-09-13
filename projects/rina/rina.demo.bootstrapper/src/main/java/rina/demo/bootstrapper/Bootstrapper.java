package rina.demo.bootstrapper;

import rina.applicationprocess.api.WhatevercastName;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcprocess.api.IPCProcessFactory;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBObjectNames;

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
		/*try{
			ApplicationProcessNamingInfo apNamingInfo = new ApplicationProcessNamingInfo("i2CAT-Barcelona", "1", null, null);
			IPCProcess ipcProcess = ipcProcessFactory.createIPCProcess(apNamingInfo);
			WhatevercastName dan = new WhatevercastName();
			dan.setName("RINA-Demo.DIF");
			dan.setRule("All members");

			RIBDaemon ribDaemon = (RIBDaemon) ipcProcess.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
			ribDaemon.create(null, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + 
					RIBObjectNames.WHATEVERCAST_NAMES + RIBObjectNames.SEPARATOR + "1", 0, dan);
		}catch(Exception ex){
			ex.printStackTrace();
		}*/
	}

}
