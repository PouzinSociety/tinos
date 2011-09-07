package rina.ipcprocess.api;

import rina.ipcservice.api.ApplicationProcessNamingInfo;

/**
 * Creates, stores and destroys instances of IPC processes
 * @author eduardgrasa
 *
 */
public interface IPCProcessFactory {
	
	/**
	 * Creates a new IPC process
	 * TODO add more stuff probably
	 * @param namingInfo the name of this new IPC process
	 */
	public IPCProcess createIPCProcess(ApplicationProcessNamingInfo namingInfo) throws Exception;
	
	/**
	 * Destroys an existing IPC process
	 * TODO add more stuff probably
	 * @param namingInfo the name of this IPC process
	 */
	public void destroyIPCProcess(ApplicationProcessNamingInfo namingInfo) throws Exception;
	
	/**
	 * Destroys an existing IPC process
	 * TODO add more stuff probably
	 * @param IPCService ipcProcess
	 */
	public void destroyIPCProcess(IPCProcess ipcProcess) throws Exception;
	
	/**
	 * Get an existing IPC process
	 * TODO add more stuff probably
	 * @param namingInfo the name of this new IPC process
	 */
	public IPCProcess getIPCProcess(ApplicationProcessNamingInfo namingInfo);
}
