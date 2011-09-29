package rina.ipcprocess.api;

import java.util.List;

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
	
	/**
	 * Return a list of the existing IPC processes
	 * @return
	 */
	public List<IPCProcess> listIPCProcesses();
}
