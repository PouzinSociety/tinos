package rina.ipcprocess.api;

/**
 * Base class for an IPC Process component
 * @author eduardgrasa
 *
 */
public abstract class BaseIPCProcessComponent implements IPCProcessComponent{
	
	private IPCProcess ipcProcess = null;
	
	public void setIPCProcess(IPCProcess ipcProcess) {
		this.ipcProcess = ipcProcess;
	}
	
	public IPCProcess getIPCProcess(){
		return this.ipcProcess;
	}

}
