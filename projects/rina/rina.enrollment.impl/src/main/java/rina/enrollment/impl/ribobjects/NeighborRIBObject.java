package rina.enrollment.impl.ribobjects;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.enrollment.api.Neighbor;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.ObjectInstanceGenerator;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;

public class NeighborRIBObject extends BaseRIBObject{

	private Neighbor neighbor = null;
	
	public NeighborRIBObject(IPCProcess ipcProcess, String objectName, Neighbor neighbor) {
		super(ipcProcess, objectName, Neighbor.NEIGHBOR_RIB_OBJECT_CLASS, ObjectInstanceGenerator.getObjectInstance());
		this.neighbor = neighbor;
	}
	
	@Override
	public RIBObject read(String objectClass, String objectName, long objectInstance) throws RIBDaemonException{
		return this;
	}
	
	@Override
	public void write(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		if (!(object instanceof Neighbor)){
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
					"Object class ("+object.getClass().getName()+") does not match object name "+objectName);
		}
		
		this.neighbor = (Neighbor) object;
	}
	
	@Override
	public void delete(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor){
		//1 Check that we're enrolled to the IPC Process
		//2 Tell the enrollment task to get the enrollment state machine
		//3 Tell the enrollment task to initiate the de-enrollment sequence (basically issue an M_RELEASE)
		//4 Tell the RMT to deallocate the flow
		//5 call the local delete operation, to update the RIB
		//5 Send a response to the caller upon successful completion or an error occurrence
	}
	
	@Override
	public void delete(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		this.getParent().removeChild(objectName);
	}
	
	@Override
	public Object getObjectValue(){
		return neighbor;
	}
}
