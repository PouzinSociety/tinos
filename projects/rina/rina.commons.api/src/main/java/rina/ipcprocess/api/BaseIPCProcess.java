package rina.ipcprocess.api;

import java.util.Hashtable;
import java.util.Map;

import rina.ipcmanager.api.IPCManager;
import rina.ipcservice.api.APService;

/**
 * Base IPC Process class that implements the component management
 * operations.
 * @author eduardgrasa
 *
 */
public abstract class BaseIPCProcess implements IPCProcess{
	
	private Map<String, IPCProcessComponent> ipcProcessComponents = null;
	
	private IPCManager ipcManager = null;
	
	public BaseIPCProcess(){
		ipcProcessComponents = new Hashtable<String, IPCProcessComponent>();
	}
	
	public Map<String, IPCProcessComponent> getIPCProcessComponents(){
		return ipcProcessComponents;
	}
	
	public IPCProcessComponent getIPCProcessComponent(String componentName){
		return ipcProcessComponents.get(componentName);
	}
	
	public void addIPCProcessComponent(IPCProcessComponent ipcProcessComponent){
		ipcProcessComponents.put(ipcProcessComponent.getName(), ipcProcessComponent);
		ipcProcessComponent.setIPCProcess(this);
	}
	
	public IPCProcessComponent removeIPCProcessComponent(String componentName){
		IPCProcessComponent component = ipcProcessComponents.remove(componentName);
		component.setIPCProcess(null);
		return component;
	}
	
	public void setIPCProcessCompnents(Map<String, IPCProcessComponent> ipcProcessComponents){
		this.ipcProcessComponents = ipcProcessComponents;
	}
	
	public void setIPCManager(IPCManager ipcManager){
		this.ipcManager = ipcManager;
	}
	
	public IPCManager getIPCManager(){
		return this.ipcManager;
	}
	
	/**
	 * Get the class that handles the interaction with 
	 * the applications in this system
	 * @return
	 */
	public APService getAPService(){
		return this.ipcManager.getAPService();
	}
}
