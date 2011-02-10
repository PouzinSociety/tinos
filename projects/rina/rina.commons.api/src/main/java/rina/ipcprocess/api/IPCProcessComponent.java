package rina.ipcprocess.api;

/**
 * A component of an IPC process
 * @author eduardgrasa
 */
public interface IPCProcessComponent {
	
	/**
	 * The IPC process where this IPC Process component belongs
	 * @param ipcProcess
	 */
	public void setIPCProcess(IPCProcess ipcProcess);

}
