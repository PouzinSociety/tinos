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
		synchronized(flowAllocatorRespository){
			flowAllocatorRespository.put(ipcProcessNamingInfo.getEncodedString(), flowAllocator);
		}
		return flowAllocator;
	}

	public void destroyFlowAllocator(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		FlowAllocator flowAllocator = null;
		
		synchronized(flowAllocatorRespository){
			flowAllocator = (FlowAllocatorImpl) flowAllocatorRespository.remove(ipcProcessNamingInfo.
				getEncodedString());
		}
		
		if (flowAllocator != null){
			flowAllocator.stop();
		}
	}

	public FlowAllocator getFlowAllocator(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		synchronized(flowAllocatorRespository){
			return flowAllocatorRespository.get(ipcProcessNamingInfo.getEncodedString());
		}
	}

}
