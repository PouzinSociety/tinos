package rina.flowallocator.impl;

import java.util.HashMap;
import java.util.Map;

import rina.flowallocator.api.FlowAllocator;
import rina.flowallocator.api.FlowAllocatorFactory;
import rina.applicationprocess.api.ApplicationProcessNamingInfo;

public class FlowAllocatorFactoryImpl implements FlowAllocatorFactory{

	private Map<String, FlowAllocator> flowAllocatorRespository = null;
	
	public FlowAllocatorFactoryImpl(){
		flowAllocatorRespository = new HashMap<String, FlowAllocator>();
	}
	
	public FlowAllocator createFlowAllocator(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		FlowAllocatorImpl flowAllocator = null;
		try {
			flowAllocator = new FlowAllocatorImpl();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		flowAllocatorRespository.put(ipcProcessNamingInfo.getEncodedString(), flowAllocator);
		return flowAllocator;
	}

	public void destroyFlowAllocator(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		FlowAllocator flowAllocator = (FlowAllocatorImpl) flowAllocatorRespository.remove(ipcProcessNamingInfo.
				getEncodedString());
		flowAllocator.stop();
	}

	public FlowAllocator getFlowAllocator(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		return flowAllocatorRespository.get(ipcProcessNamingInfo.getEncodedString());
	}

}
