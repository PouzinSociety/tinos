package rina.ribdaemon.impl.test;

import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;

public class FakeRIBObject extends BaseRIBObject{

	public FakeRIBObject(IPCProcess ipcProcess, String objectName,
			String objectClass, long objectInstance) {
		super(ipcProcess, objectName, objectClass, objectInstance);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object getObjectValue() {
		// TODO Auto-generated method stub
		return null;
	}

}
