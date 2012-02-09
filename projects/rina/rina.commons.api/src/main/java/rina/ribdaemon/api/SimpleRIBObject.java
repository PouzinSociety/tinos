package rina.ribdaemon.api;

import rina.ipcprocess.api.IPCProcess;

/**
 * A simple RIB object that just acts as a wrapper 
 * for an object value
 * @author eduardgrasa
 *
 */
public class SimpleRIBObject extends BaseRIBObject{

	private Object value = null;
	
	public SimpleRIBObject(IPCProcess ipcProcess, String objectName, String objectClass, Object value) {
		super(ipcProcess, objectName, objectClass, ObjectInstanceGenerator.getObjectInstance());
		this.value = value;
	}
	
	@Override
	public RIBObject read(String objectClass, String objectName, long objectInstance) throws RIBDaemonException{
		return this;
	}
	
	@Override
	public void write(String objectClass, String objectName, long objectInstance, Object value) throws RIBDaemonException {		
		this.value = value;
	}

	@Override
	public Object getObjectValue() {
		return value;
	}

}
