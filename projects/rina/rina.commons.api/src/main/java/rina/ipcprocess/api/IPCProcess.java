package rina.ipcprocess.api;

import java.util.Map;

import rina.ipcmanager.api.IPCManager;
import rina.ipcservice.api.APService;

/**
 * Represents an IPC Process. Holds together the different components of the IPC 
 * process
 * @author eduardgrasa
 *
 */
public interface IPCProcess{
	
	/* IPC Process Component management */
	public Map<String, IPCProcessComponent> getIPCProcessComponents();
	
	public IPCProcessComponent getIPCProcessComponent(String componentName);
	
	public void addIPCProcessComponent(IPCProcessComponent ipcProcessComponent);
	
	public IPCProcessComponent removeIPCProcessComponent(String componentName);
	
	public void setIPCProcessCompnents(Map<String, IPCProcessComponent> ipcProcessComponents);
	
	/* IPC Manager */
	/**
	 * Set the IPCManager of this system
	 * @param ipcManager
	 */
	public void setIPCManager(IPCManager ipcManager);
	
	/**
	 * Get the IPCManager of this system
	 * @return
	 */
	public IPCManager getIPCManager();
	
	/**
	 * Get the class that handles the interaction with 
	 * the applications in this system
	 * @return
	 */
	public APService getAPService();
	
	/**
	 * Lifecicle event, invoked to tell the IPC process it is about to be destroyed.
	 * The IPC Process implementation must do any necessary cleanup inside this 
	 * operation.
	 */
	public void destroy();
	
	/* Information from RIB objects managed by the IPC Process */
	public Long getAddress();
}
