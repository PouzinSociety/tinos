package rina.enrollment.impl.ribobjects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.BaseCDAPSessionManager;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.enrollment.impl.EnrollmentTaskImpl;
import rina.enrollment.impl.statemachines.BaseEnrollmentStateMachine;
import rina.enrollment.impl.statemachines.EnrolleeStateMachine;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcprocess.api.IPCProcess.OperationalStatus;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.ObjectInstanceGenerator;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * Handles the operations related to the "daf.management.operationalStatus" objects
 * @author eduardgrasa
 *
 */
public class OperationalStatusRIBObject extends BaseRIBObject{
	
	private static final Log log = LogFactory.getLog(OperationalStatusRIBObject.class);

	private EnrollmentTaskImpl enrollmentTask = null;
	private OperationalStatus status = OperationalStatus.STOPPED;
	private CDAPSessionManager cdapSessionManager = null;

	public OperationalStatusRIBObject(EnrollmentTaskImpl enrollmentTaskImpl, IPCProcess ipcProcess){
		super(ipcProcess, RIBObjectNames.OPERATIONAL_STATUS_RIB_OBJECT_CLASS, 
				ObjectInstanceGenerator.getObjectInstance(), RIBObjectNames.OPERATIONAL_STATUS_RIB_OBJECT_NAME);
		this.enrollmentTask = enrollmentTaskImpl;
		this.cdapSessionManager = (CDAPSessionManager) getIPCProcess().getIPCProcessComponent(BaseCDAPSessionManager.getComponentName());
	}
	
	@Override
	public void start(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor){
		EnrolleeStateMachine enrollmentStateMachine = null;
		
		try{
			enrollmentStateMachine = (EnrolleeStateMachine) this.getEnrollmentStateMachine(cdapSessionDescriptor);
		}catch(Exception ex){
			log.error(ex);
			sendErrorMessage(cdapSessionDescriptor);
		}
		
		if (enrollmentStateMachine == null){
			log.error("Got a CDAP message that is not for me: "+cdapMessage.toString());
			return;
		}
		
		synchronized(this){
			this.status = OperationalStatus.STARTED;
		}
	}
	
	@Override
	public synchronized void start(Object object) throws RIBDaemonException {
		this.status = OperationalStatus.STARTED;
	}
	
	@Override
	public synchronized void stop(Object object) throws RIBDaemonException {
		this.status = OperationalStatus.STOPPED;
	}
	
	private BaseEnrollmentStateMachine getEnrollmentStateMachine(CDAPSessionDescriptor cdapSessionDescriptor){
		BaseEnrollmentStateMachine enrollmentStateMachine = enrollmentTask.getEnrollmentStateMachine(
				cdapSessionDescriptor.getDestinationApplicationProcessNamingInfo().getApplicationProcessName(), 
				cdapSessionDescriptor.getPortId(), false);
		return enrollmentStateMachine;
	}
	
	private void sendErrorMessage(CDAPSessionDescriptor cdapSessionDescriptor){
		try{
			enrollmentTask.getRIBDaemon().sendMessage(cdapSessionManager.getReleaseConnectionRequestMessage(cdapSessionDescriptor.getPortId(), null, false), 
					cdapSessionDescriptor.getPortId(), null);
		}catch(Exception e){
			log.error(e);
		}
	}
	
	@Override
	public synchronized Object getObjectValue(){
		return status;
	}

}
