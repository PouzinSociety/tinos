package rina.ribdaemon.api;

import rina.ipcprocess.api.BaseIPCProcessComponent;

/**
 * Provides the component name for the RIB Daemon
 * @author eduardgrasa
 *
 */
public abstract class BaseRIBDaemon extends BaseIPCProcessComponent implements RIBDaemon {

	public static final String getComponentName(){
		return RIBDaemon.class.getName();
	}
	
	public String getName() {
		return BaseRIBDaemon.getComponentName();
	}

}
