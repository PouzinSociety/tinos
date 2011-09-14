package rina.flowallocator.impl.ribobjects;

import java.util.Calendar;

import rina.flowallocator.api.QoSCube;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.RIBDaemonException;

public class QoSCubeRIBObject extends BaseRIBObject{

	private QoSCube qosCube = null;
	
	public QoSCubeRIBObject(IPCProcess ipcProcess, String objectName, QoSCube qosCube) {
		super(ipcProcess, objectName, null, Calendar.getInstance().getTimeInMillis());
		this.qosCube = qosCube;
	}
	
	@Override
	public Object read(String objectClass, String objectName, long objectInstance) throws RIBDaemonException{
		return qosCube;
	}
	
	@Override
	public void write(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		if (!(object instanceof QoSCube)){
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
					"Object class ("+object.getClass().getName()+") does not match object name "+objectName);
		}
		
		this.qosCube = (QoSCube) object;
	}
	
	@Override
	public void delete(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		this.getParent().removeChild(objectName);
		this.getRIBDaemon().delete(this.getObjectClass(), this.getObjectName(), this.getObjectInstance(), object);
	}
	
	@Override
	public Object getObjectValue(){
		return qosCube;
	}
}
