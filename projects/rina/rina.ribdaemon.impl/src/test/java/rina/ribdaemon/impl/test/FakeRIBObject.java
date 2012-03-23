package rina.ribdaemon.impl.test;

import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;

public class FakeRIBObject extends BaseRIBObject{

	public FakeRIBObject(IPCProcess ipcProcess, String objectClass,
			long objectInstance, String objectName) {
		super(ipcProcess, objectClass, objectInstance, objectName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object getObjectValue() {
		// TODO Auto-generated method stub
		return null;
	}

}
