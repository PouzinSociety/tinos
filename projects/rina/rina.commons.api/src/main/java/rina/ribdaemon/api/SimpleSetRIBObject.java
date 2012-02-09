package rina.ribdaemon.api;

import java.util.ArrayList;
import java.util.List;

import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;

/**
 * A RIB object that is a container (set) for other 
 * child RIB objects.
 *
 */
public class SimpleSetRIBObject extends BaseRIBObject{
	
	private String childObjectClass = null;
	
	public SimpleSetRIBObject(IPCProcess ipcProcess, String objectName, String objectClass, String childObjectClass) {
		super(ipcProcess, objectName, objectClass, ObjectInstanceGenerator.getObjectInstance());
		this.childObjectClass = childObjectClass;
	}

	@Override
	public void create(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException{
		SimpleSetMemberRIBObject ribObject = new SimpleSetMemberRIBObject(this.getIPCProcess(), objectName, childObjectClass, object);
		this.addChild(ribObject);
		getRIBDaemon().addRIBObject(ribObject);
	}

	@Override
	public void delete(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		String childName = null;
		List<String> childrenNames = new ArrayList<String>();
		
		for(int i=0; i<this.getChildren().size(); i++){
			childName = this.getChildren().get(i).getObjectName();
			childrenNames.add(childName);
			getRIBDaemon().delete(null, childName, 0, null);
		}
		
		for(int i=0; i<childrenNames.size(); i++){
			this.removeChild(childrenNames.get(i));
		}
	}

	@Override
	public RIBObject read(String objectClass, String objectName, long objectInstance) throws RIBDaemonException{
		return this;
	}
	
	@Override
	public Object getObjectValue(){
		return null;
	}
}