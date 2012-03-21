package rina.ipcprocess.api;

import java.util.ArrayList;
import java.util.Hashtable;
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
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;

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
	
	public ApplicationProcessNamingInfo getApplicationProcessNamingInfo(){
		ApplicationProcessNamingInfo result = null;
		RIBDaemon ribDaemon = null;
		
		try{
			ribDaemon = (RIBDaemon) this.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
			result = (ApplicationProcessNamingInfo) ribDaemon.read(null, 
					ApplicationProcessNamingInfo.APPLICATION_PROCESS_NAMING_INFO_RIB_OBJECT_NAME, 0).getObjectValue();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		return result;
	}
	
	public String getApplicationProcessName(){
		String result = null;
		ApplicationProcessNamingInfo apNamingInfo = this.getApplicationProcessNamingInfo();
		if (apNamingInfo != null){
			result = apNamingInfo.getApplicationProcessName();
		}
		
		return result;
	}
	
	public String getApplicationProcessInstance(){
		String result = null;
		ApplicationProcessNamingInfo apNamingInfo = this.getApplicationProcessNamingInfo();
		if (apNamingInfo != null){
			result = apNamingInfo.getApplicationProcessInstance();
		}
		
		return result;
	}
	
	public List<WhatevercastName> getWhatevercastNames(){
		List<WhatevercastName> result = new ArrayList<WhatevercastName>();
		RIBDaemon ribDaemon = null;
		RIBObject ribObject = null;
		RIBObject childRibObject = null;
		
		try{
			ribDaemon = (RIBDaemon) this.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
			ribObject = ribDaemon.read(null, WhatevercastName.WHATEVERCAST_NAME_SET_RIB_OBJECT_NAME, 0);
			if (ribObject != null && ribObject.getChildren() != null){
				for(int i=0; i<ribObject.getChildren().size(); i++){
					childRibObject = ribObject.getChildren().get(i);
					result.add((WhatevercastName)childRibObject.getObjectValue());
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}

		return result;
	}
	
	public String getDIFName(){
		String result = null;
		WhatevercastName whatevercastName = null;
		RIBDaemon ribDaemon = null;
		
		try{
			ribDaemon = (RIBDaemon) this.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
			whatevercastName = (WhatevercastName) ribDaemon.read(null, 
					WhatevercastName.DIF_NAME_WHATEVERCAST_OBJECT_NAME , 0).getObjectValue();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		if (whatevercastName != null){
			result = whatevercastName.getName();
		}
		
		return result;
	}
	
	/**
	 * Returns the list of IPC processes that are part of the DIF this IPC Process is part of
	 * @return
	 */
	public List<Neighbor> getNeighbors(){
		List<Neighbor> result = new ArrayList<Neighbor>();
		RIBDaemon ribDaemon = null;
		RIBObject ribObject = null;
		RIBObject childRibObject = null;
		
		try{
			ribDaemon = (RIBDaemon) this.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
			ribObject = ribDaemon.read(null, Neighbor.NEIGHBOR_SET_RIB_OBJECT_NAME, 0);
			if (ribObject != null && ribObject.getChildren() != null){
				for(int i=0; i<ribObject.getChildren().size(); i++){
					childRibObject = ribObject.getChildren().get(i);
					result.add((Neighbor)childRibObject.getObjectValue());
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}

		return result;
	}
	
	/**
	 * Return the current address of the IPC process
	 */
	public Long getAddress(){
		Long result = null;
		RIBDaemon ribDaemon = null;
		
		try{
			ribDaemon = (RIBDaemon) this.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
			result = (Long) ribDaemon.read(null, RIBObjectNames.ADDRESS_RIB_OBJECT_NAME, 0).getObjectValue();
		}catch(Exception ex){
			ex.printStackTrace();
		}

		return result;
	}
	
	/**
	 * Return the current operational status of the IPC process
	 */
	public Boolean getOperationalStatus(){
		Boolean result = null;
		RIBDaemon ribDaemon = null;
		
		try{
			ribDaemon = (RIBDaemon) this.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
			result = (Boolean) ribDaemon.read(null, RIBObjectNames.OPERATIONAL_STATUS_RIB_OBJECT_NAME, 0).getObjectValue();
		}catch(Exception ex){
			ex.printStackTrace();
		}

		return result;
	}
	
	/**
	 * Returns the QoS cubes available in this IPC Process. 
	 * It gets the information from the RIB.
	 * @return
	 */
	public List<QoSCube> getQoSCubes(){
		List<QoSCube> result = new ArrayList<QoSCube>();
		RIBDaemon ribDaemon = null;
		RIBObject ribObject = null;
		RIBObject childRibObject = null;
		
		try{
			ribDaemon = (RIBDaemon) this.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
			ribObject = ribDaemon.read(null, QoSCube.QOSCUBE_SET_RIB_OBJECT_NAME, 0);
			if (ribObject != null && ribObject.getChildren() != null){
				for(int i=0; i<ribObject.getChildren().size(); i++){
					childRibObject = ribObject.getChildren().get(i);
					result.add((QoSCube)childRibObject.getObjectValue());
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}

		return result;
	}
	
	/**
	 * Returns the list of flows that are currently allocated in this IPC
	 * process. It gets the information from the RIB.
	 * @return
	 */
	public List<Flow> getAllocatedFlows(){
		List<Flow> result = new ArrayList<Flow>();
		RIBDaemon ribDaemon = null;
		RIBObject ribObject = null;
		RIBObject childRibObject = null;
		
		try{
			ribDaemon = (RIBDaemon) this.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
			ribObject = ribDaemon.read(null, Flow.FLOW_SET_RIB_OBJECT_NAME, 0);
			if (ribObject != null && ribObject.getChildren() != null){
				for(int i=0; i<ribObject.getChildren().size(); i++){
					childRibObject = ribObject.getChildren().get(i);
					result.add((Flow)childRibObject.getObjectValue());
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}

		return result;
	}
	
	public DataTransferConstants getDataTransferConstants(){
		DataTransferConstants result = null;
		RIBDaemon ribDaemon = null;
		
		try{
			ribDaemon = (RIBDaemon) this.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
			result = (DataTransferConstants) ribDaemon.read(null, 
					DataTransferConstants.DATA_TRANSFER_CONSTANTS_RIB_OBJECT_NAME, 0).getObjectValue();
		}catch(Exception ex){
			ex.printStackTrace();
		}

		return result;
	}
}
