package rina.ribdaemon.api;

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
	public void delete(String objectClass, String objectName, long objectInstance, Object objectValue) throws RIBDaemonException {
		RIBObject ribObject = null;
		
		while(this.getChildren().size() > 0){
			ribObject = this.getChildren().remove(0);
			this.getRIBDaemon().removeRIBObject(ribObject);
		}
	}

	@Override
	public RIBObject read(String objectClass, String objectName, long objectInstance) throws RIBDaemonException{
		return this;
	}
	
	@Override
	public Object getObjectValue(){
		Object[] result = new Object[this.getChildren().size()];
		for(int i=0; i<result.length; i++){
			result[i] = this.getChildren().get(i).getObjectValue();
		}
		
		return result;
	}
}