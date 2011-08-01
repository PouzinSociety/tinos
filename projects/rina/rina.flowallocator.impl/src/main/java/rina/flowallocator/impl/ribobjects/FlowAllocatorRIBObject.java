package rina.flowallocator.impl.ribobjects;

import java.util.Calendar;

import rina.flowallocator.api.FlowAllocator;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.RIBObjectNames;

public class FlowAllocatorRIBObject extends BaseRIBObject{
	
	private FlowAllocator flowAllocator = null;
	
	public FlowAllocatorRIBObject(IPCProcess ipcProcess, FlowAllocator flowAllocator){
		super(ipcProcess, RIBObjectNames.DIF + RIBObjectNames.SEPARATOR + RIBObjectNames.RESOURCE_ALLOCATION + RIBObjectNames.SEPARATOR +
				RIBObjectNames.FLOW_ALLOCATOR, null, Calendar.getInstance().getTimeInMillis());
		this.flowAllocator = flowAllocator;
	}
	

}
