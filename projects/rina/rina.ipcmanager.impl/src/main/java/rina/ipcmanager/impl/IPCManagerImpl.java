package rina.ipcmanager.impl;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.WhatevercastName;
import rina.ipcmanager.impl.console.IPCManagerConsole;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcprocess.api.IPCProcessFactory;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBObjectNames;

public class IPCManagerImpl {
	private static final Log log = LogFactory.getLog(IPCManagerImpl.class);
	
	private IPCManagerConsole console = null;
	
	/**
	 * The thread pool implementation
	 */
	private ExecutorService executorService = null;
	private static int MAXWORKERTHREADS = 10;
	
	/**
	 * The IPC Process factory
	 */
	private IPCProcessFactory ipcProcessFactory = null;
	
	public IPCManagerImpl(){
		console = new IPCManagerConsole(this);
		this.executorService = Executors.newFixedThreadPool(MAXWORKERTHREADS);
		executorService.execute(console);
		log.debug("IPC Manager started");
	}
	
	public void setIPCProcessFactory(IPCProcessFactory ipcProcessFactory){
		this.ipcProcessFactory = ipcProcessFactory;
	}
	
	public void createIPCProcess(String difName, String applicationProcessName, String applicationProcessInstance) throws Exception{
		ApplicationProcessNamingInfo apNamingInfo = new ApplicationProcessNamingInfo(applicationProcessName, applicationProcessInstance, null, null);
		IPCProcess ipcProcess = ipcProcessFactory.createIPCProcess(apNamingInfo);
		
		WhatevercastName dan = new WhatevercastName();
		dan.setName(difName);
		dan.setRule("All members");

		RIBDaemon ribDaemon = (RIBDaemon) ipcProcess.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		ribDaemon.create(null, RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + 
				RIBObjectNames.WHATEVERCAST_NAMES + RIBObjectNames.SEPARATOR + "1", 0, dan);
	}
	
	public void destroyIPCProcesses(String applicationProcessName, String applicationProcessInstance) throws Exception{
		ApplicationProcessNamingInfo apNamingInfo = new ApplicationProcessNamingInfo(applicationProcessName, applicationProcessInstance, null, null);
		ipcProcessFactory.destroyIPCProcess(apNamingInfo);
	}

}
