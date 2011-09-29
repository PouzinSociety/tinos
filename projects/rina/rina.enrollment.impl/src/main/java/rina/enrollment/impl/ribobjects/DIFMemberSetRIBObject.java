package rina.enrollment.impl.ribobjects;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.DAFMember;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.enrollment.impl.EnrollmentTaskImpl;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.ObjectInstanceGenerator;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * Handles all the operations for the "daf.management.enrollment.members" objectname
 * @author eduardgrasa
 *
 */
public class DIFMemberSetRIBObject extends BaseRIBObject{
	
	private static final Log log = LogFactory.getLog(DIFMemberSetRIBObject.class);
	private EnrollmentTaskImpl enrollmentTask = null;
	
	public DIFMemberSetRIBObject(EnrollmentTaskImpl enrollmentTask, IPCProcess ipcProcess){
		super(ipcProcess, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.ENROLLMENT + RIBObjectNames.SEPARATOR + RIBObjectNames.MEMBERS, 
				"dafmember set", ObjectInstanceGenerator.getObjectInstance());
		this.enrollmentTask = enrollmentTask;
	}
	
	@Override
	public RIBObject read(String objectClass, String objectName, long objectInstance) throws RIBDaemonException{
		return this;
	}
	
	/**
	 * Called by the DIF Management System, IPC Manager or other to cause this IPC process to enroll to another 
	 * IPC process
	 */
	@Override
	public void create(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException{
		enrollmentTask.initiateEnrollment(cdapMessage, cdapSessionDescriptor);
	}

	/**
	 * Called by the DIF Management System, IPC Manager or other to cause this IPC process to break the enrollment with another 
	 * IPC process
	 */
	@Override
	public void delete(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException{
		//TODO, cancel enrollment with all the IPC processes?
	}
	
	@Override
	public void create(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException{
		if (!(object instanceof DAFMember)){
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
					"Object class ("+object.getClass().getName()+") does not match object name "+objectName);
		}
		
		DIFMemberRIBObject ribObject = new DIFMemberRIBObject(this.getIPCProcess(), objectName, (DAFMember) object);
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
	public Object getObjectValue(){
		return null;
	}
}
