package rina.enrollment.impl.ribobjects;

import rina.applicationprocess.api.DAFMember;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.ObjectInstanceGenerator;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;

public class DIFMemberRIBObject extends BaseRIBObject{

	private DAFMember member = null;
	
	public DIFMemberRIBObject(IPCProcess ipcProcess, String objectName, DAFMember member) {
		super(ipcProcess, objectName, "dafmember", ObjectInstanceGenerator.getObjectInstance());
		this.member = member;
	}
	
	@Override
	public RIBObject read(String objectClass, String objectName, long objectInstance) throws RIBDaemonException{
		return this;
	}
	
	@Override
	public void write(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		if (!(object instanceof DAFMember)){
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
					"Object class ("+object.getClass().getName()+") does not match object name "+objectName);
		}
		
		this.member = (DAFMember) object;
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
		return member;
	}
}
