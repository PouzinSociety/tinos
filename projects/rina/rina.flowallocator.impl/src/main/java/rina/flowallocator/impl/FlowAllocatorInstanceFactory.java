package rina.flowallocator.impl;

import rina.ipcservice.api.AllocateRequest;

/**
 * Creates and manages flow allocator instances
 * @author eduardgrasa
 *
 */
public class FlowAllocatorInstanceFactory {

	public FlowAllocatorInstance createFlowAllocatorInstance(AllocateRequest allocateRequest){
		FlowAllocatorInstance fai = new FlowAllocatorInstance(allocateRequest);
		return fai;
	}
}
