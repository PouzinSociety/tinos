package rina.ipcservice.impl.test;

import rina.flowallocator.api.FlowAllocator;
import rina.flowallocator.api.FlowAllocatorFactory;
import rina.ipcservice.api.ApplicationProcessNamingInfo;

public class MockFlowAllocatorFactory implements FlowAllocatorFactory{
	
	FlowAllocator mockFlowAllocator = null;

	public FlowAllocator createFlowAllocator(ApplicationProcessNamingInfo arg0) {
		mockFlowAllocator = new MockFlowAllocator();
		return mockFlowAllocator;
	}

	public void destroyFlowAllocator(ApplicationProcessNamingInfo arg0) {
		mockFlowAllocator = null;
	}

	public FlowAllocator getFlowAllocator(ApplicationProcessNamingInfo arg0) {
		return mockFlowAllocator;
	}

}
