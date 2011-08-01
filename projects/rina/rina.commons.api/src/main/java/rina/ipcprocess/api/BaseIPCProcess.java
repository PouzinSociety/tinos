package rina.ipcprocess.api;

import java.util.Hashtable;
import java.util.Map;

/**
 * Base IPC Process class that implements the component management
 * operations.
 * @author eduardgrasa
 *
 */
public abstract class BaseIPCProcess implements IPCProcess{
	
	private Map<String, IPCProcessComponent> ipcProcessComponents = null;
	
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
}
