package rina.ribdaemon.api;

import rina.ipcprocess.api.IPCProcess;

/**
 * A Simple RIB object that is a member of a set
 * (for example whatevercast names, qos cubes, ...).
 * As with the Simple RIB Object, the RIB Object is 
 * just a container for the data
 * @author eduardgrasa
 *
 */
public class SimpleSetMemberRIBObject extends SimpleRIBObject{

	public SimpleSetMemberRIBObject(IPCProcess ipcProcess, String objectName,
			String objectClass, Object value) {
		super(ipcProcess, objectName, objectClass, value);
	}

	@Override
	public void delete(String objectClass, String objectName, long objectInstance) throws RIBDaemonException {
		this.getParent().removeChild(objectName);
	}
}
