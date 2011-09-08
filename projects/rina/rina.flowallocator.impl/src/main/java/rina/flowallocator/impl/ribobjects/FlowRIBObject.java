package rina.flowallocator.impl.ribobjects;

import java.util.Calendar;

import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;

public class FlowRIBObject extends BaseRIBObject{
	
	public FlowRIBObject(IPCProcess ipcProcess, String objectName){
		super(ipcProcess, objectName, null, Calendar.getInstance().getTimeInMillis());
	}
	
	@Override
	public Object getObjectValue(){
		return null;
	}

}
