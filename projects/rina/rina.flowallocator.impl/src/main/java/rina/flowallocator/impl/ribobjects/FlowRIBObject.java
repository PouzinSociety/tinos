package rina.flowallocator.impl.ribobjects;

import java.util.Calendar;

import rina.flowallocator.api.FlowAllocatorInstance;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;

public class FlowRIBObject extends BaseRIBObject{
	
	private FlowAllocatorInstance flowAllocatorInstance = null;
	
	public FlowRIBObject(IPCProcess ipcProcess, String objectName, FlowAllocatorInstance flowAllocatorInstance){
		super(ipcProcess, objectName, "flow", Calendar.getInstance().getTimeInMillis());
		this.flowAllocatorInstance = flowAllocatorInstance;
	}
	
	@Override
	public Object getObjectValue(){
		return flowAllocatorInstance.getFlow();
	}
}
