package rina.ipcservice.impl.ribobjects;

import java.util.Calendar;

import rina.applicationprocess.api.WhatevercastName;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;

public class WhatevercastNameRIBObject extends BaseRIBObject{

	private WhatevercastName whatevercastName = null;
	
	public WhatevercastNameRIBObject(IPCProcess ipcProcess, String objectName, WhatevercastName whatevercastName) {
		super(ipcProcess, objectName, "whatname", Calendar.getInstance().getTimeInMillis());
		this.whatevercastName = whatevercastName;
	}
	
	@Override
	public RIBObject read(String objectClass, String objectName, long objectInstance) throws RIBDaemonException{
		return this;
	}
	
	@Override
	public void write(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		if (!(object instanceof WhatevercastName)){
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
					"Object class ("+object.getClass().getName()+") does not match object name "+objectName);
		}
		
		this.whatevercastName = (WhatevercastName) object;
	}
	
	@Override
	public void delete(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		this.getParent().removeChild(objectName);
		this.getRIBDaemon().delete(this.getObjectClass(), this.getObjectName(), this.getObjectInstance(), object);
	}
	
	@Override
	public Object getObjectValue(){
		return whatevercastName;
	}
}
