package rina.ipcprocess.api;

import java.util.List;
import java.util.Map;

import rina.applicationprocess.api.ApplicationProcess;

/**
 * Represents an IPC Process. Holds together the different components of the IPC 
 * process
 * @author eduardgrasa
 *
 */
public interface IPCProcess extends ApplicationProcess{
	
	/* IPC Process Component management */
	public Map<String, IPCProcessComponent> getIPCProcessComponents();
	
	public IPCProcessComponent getIPCProcessComponent(String componentName);
	
	public void addIPCProcessComponent(IPCProcessComponent ipcProcessComponent);
	
	public IPCProcessComponent removeIPCProcessComponent(String componentName);
	
	public void setIPCProcessCompnents(Map<String, IPCProcessComponent> ipcProcessComponents);
	
	/**
	 * Lifecicle event, invoked to tell the IPC process it is about to be destroyed.
	 * The IPC Process implementation must do any necessary cleanup inside this 
	 * operation.
	 */
	public void destroy();
	
	/**
	 * Deliver a set of sdus to the application process bound to portId
	 * @param sdus
	 * @param portId
	 */
	public void deliverSDUsToApplicationProcess(List<byte[]> sdus, int portId);
	
	/**
	 * Call the applicationProcess deallocate.deliver operation
	 * @param portId
	 */
	public void deliverDeallocateRequestToApplicationProcess(int portId);
}
