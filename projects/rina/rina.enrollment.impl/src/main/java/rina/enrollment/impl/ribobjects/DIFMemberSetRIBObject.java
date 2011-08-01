package rina.enrollment.impl.ribobjects;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.ApplicationProcessNameSynonym;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.enrollment.impl.EnrollmentTaskImpl;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;
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
		super(ipcProcess, RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.ENROLLMENT + RIBObjectNames.SEPARATOR + RIBObjectNames.MEMBERS, 
				null, Calendar.getInstance().getTimeInMillis());
		this.enrollmentTask = enrollmentTask;
	}
	
	@Override
	public Object read(String objectClass, String objectName, long objectInstance) throws RIBDaemonException{
		List<ApplicationProcessNameSynonym> members = new ArrayList<ApplicationProcessNameSynonym>();
		
		for(int i=0; i<this.getChildren().size(); i++){
			RIBObject ribObject = this.getChildren().get(i);
			ApplicationProcessNameSynonym member = 
				(ApplicationProcessNameSynonym) ribObject.read(ribObject.getObjectClass(), ribObject.getObjectName(), ribObject.getObjectInstance());
			members.add(member);
		}
		
		return members;
	}
	
	/**
	 * Called by the DIF Management System, IPC Manager or other to cause this IPC process to enroll to another 
	 * IPC process
	 */
	@Override
	public void create(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException{
		//TODO
			//1 Check that we're not already enrolled to the IPC Process
			//2 Tell the RMT to allocate a new flow to the IPC process  (will return a port Id)
			//3 Tell the enrollment task to create a new Enrollment state machine (or get one if we had already enrolled wit the remote IPC process in the past)
			//4 Tell the enrollment state machine to initiate the enrollment (will require an M_CONNECT message and a port Id)
			//5 Call the local create operation, to update the RIB
			//6 When the enrollment state machine has finished enrolling the remote IPC process (or an error has occurred), I have to be notified
			//and then issue the create response request
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
		if (!(object instanceof ApplicationProcessNameSynonym)){
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
					"Object class ("+object.getClass().getName()+") does not match object name "+objectName);
		}
		
		DIFMemberRIBObject ribObject = new DIFMemberRIBObject(this.getIPCProcess(), objectName, (ApplicationProcessNameSynonym) object);
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
}
