package rina.flowallocator.api;

import rina.ipcprocess.api.BaseIPCProcessComponent;

/**
 * Provides the component name for the Flow Allocator
 * @author eduardgrasa
 *
 */
public abstract class BaseFlowAllocator extends BaseIPCProcessComponent implements FlowAllocator{
	
	public static final String FLOW_ALLOCATOR_PORT_PROPERTY = "rina.flowallocator.port";
	public static final int DEFAULT_PORT = 32770;
	
	public static final String getComponentName(){
		return FlowAllocator.class.getName();
	}

	public String getName(){
		return BaseFlowAllocator.getComponentName();
	}
}
