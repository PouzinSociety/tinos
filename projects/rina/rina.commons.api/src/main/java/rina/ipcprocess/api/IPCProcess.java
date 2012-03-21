package rina.ipcprocess.api;

import java.util.List;
import java.util.Map;

import rina.applicationprocess.api.WhatevercastName;
import rina.efcp.api.DataTransferConstants;
import rina.enrollment.api.Neighbor;
import rina.flowallocator.api.QoSCube;
import rina.flowallocator.api.message.Flow;
import rina.ipcmanager.api.IPCManager;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.ApplicationProcessNamingInfo;

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
	
	/**
	 * Will call the execute operation of the IPCManager in order to execute a runnable.
	 * Classes implementing IPCProcess should not create its own thread pool, but use 
	 * the one managed by the IPCManager instead.
	 * @param runnable
	 */
	public void execute(Runnable runnable);
	
	/* Convenience methods to get information from the RIB */
	public ApplicationProcessNamingInfo getApplicationProcessNamingInfo();
	public String getApplicationProcessName();
	public String getApplicationProcessInstance();
	public List<WhatevercastName> getWhatevercastNames();
	public String getDIFName();
	public List<Neighbor> getNeighbors();
	public Long getAddress();
	public Boolean getOperationalStatus();
	public List<QoSCube> getQoSCubes();
	public List<Flow> getAllocatedFlows();
	public DataTransferConstants getDataTransferConstants();
}
