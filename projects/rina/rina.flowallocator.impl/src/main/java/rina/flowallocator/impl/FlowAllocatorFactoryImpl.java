package rina.flowallocator.impl;

import java.util.HashMap;
import java.util.Map;

import rina.flowallocator.api.FlowAllocator;
import rina.flowallocator.api.FlowAllocatorFactory;
import rina.ipcservice.api.ApplicationProcessNamingInfo;

public class FlowAllocatorFactoryImpl implements FlowAllocatorFactory{

	private Map<ApplicationProcessNamingInfo, FlowAllocator> dataTransferAERespository = null;
	
	public FlowAllocatorFactoryImpl(){
		dataTransferAERespository = new HashMap<ApplicationProcessNamingInfo, FlowAllocator>();
	}
	
	public FlowAllocator createFlowAllocator(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		FlowAllocator flowAllocator = null;
		try {
			flowAllocator = new FlowAllocatorImpl();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dataTransferAERespository.put(ipcProcessNamingInfo, flowAllocator);
		return flowAllocator;
	}

	public void destroyFlowAllocator(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		dataTransferAERespository.remove(ipcProcessNamingInfo);
	}

	public FlowAllocator getFlowAllocator(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		return dataTransferAERespository.get(ipcProcessNamingInfo);
	}

}
