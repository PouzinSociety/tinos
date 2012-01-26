package rina.flowallocator.impl;

import java.util.HashMap;
import java.util.Map;

import rina.flowallocator.api.FlowAllocator;
import rina.flowallocator.api.FlowAllocatorFactory;
import rina.ipcmanager.api.IPCManager;
import rina.ipcservice.api.ApplicationProcessNamingInfo;

public class FlowAllocatorFactoryImpl implements FlowAllocatorFactory{

	private Map<ApplicationProcessNamingInfo, FlowAllocator> flowAllocatorRespository = null;
	private IPCManager ipcManager = null;
	
	public FlowAllocatorFactoryImpl(){
		flowAllocatorRespository = new HashMap<ApplicationProcessNamingInfo, FlowAllocator>();
	}
	
	public void setIPCManager(IPCManager ipcManager){
		this.ipcManager = ipcManager;
	}
	
	public FlowAllocator createFlowAllocator(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		FlowAllocatorImpl flowAllocator = null;
		try {
			flowAllocator = new FlowAllocatorImpl();
			flowAllocator.setAPService(ipcManager.getAPService());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		flowAllocatorRespository.put(ipcProcessNamingInfo, flowAllocator);
		return flowAllocator;
	}

	public void destroyFlowAllocator(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		FlowAllocator flowAllocator = (FlowAllocatorImpl) flowAllocatorRespository.remove(ipcProcessNamingInfo);
		flowAllocator.stop();
	}

	public FlowAllocator getFlowAllocator(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		return flowAllocatorRespository.get(ipcProcessNamingInfo);
	}

}
