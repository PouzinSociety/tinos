package rina.enrollment.impl.ribobjects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.BaseCDAPSessionManager;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.enrollment.impl.EnrollmentTaskImpl;
import rina.enrollment.impl.statemachines.EnrollmentStateMachine;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.ObjectInstanceGenerator;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * Handles the operations related to the "daf.management.enrollment" objects
 * @author eduardgrasa
 *
 */
public class EnrollmentRIBObject extends BaseRIBObject{
	
	private static final Log log = LogFactory.getLog(EnrollmentRIBObject.class);
	
	private EnrollmentTaskImpl enrollmentTask = null;
	private CDAPSessionManager cdapSessionManager = null;
	
	public EnrollmentRIBObject(EnrollmentTaskImpl enrollmentTaskImpl, IPCProcess ipcProcess){
		super(ipcProcess, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.ENROLLMENT, "enrollment", ObjectInstanceGenerator.getObjectInstance());
		this.enrollmentTask = enrollmentTaskImpl;
		this.cdapSessionManager = (CDAPSessionManager) getIPCProcess().getIPCProcessComponent(BaseCDAPSessionManager.getComponentName());
	}
	
	@Override
	public void read(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException {
		EnrollmentStateMachine enrollmentStateMachine = null;
		
		try{
			enrollmentStateMachine = enrollmentTask.getEnrollmentStateMachine(cdapSessionDescriptor.getDestinationApplicationProcessNamingInfo(), false);
		}catch(Exception ex){
			log.error(ex);
			try{
				enrollmentTask.getRIBDaemon().sendMessage(cdapSessionManager.getReleaseConnectionRequestMessage(cdapSessionDescriptor.getPortId(), null, false), 
						cdapSessionDescriptor.getPortId(), null);
			}catch(Exception e){
				log.error(e);
			}
		}
		
		if (enrollmentStateMachine == null){
			log.error("Got a CDAP message that is not for me: "+cdapMessage.toString());
			return;
		}
		
		enrollmentStateMachine.read(cdapMessage, cdapSessionDescriptor);
	}

	@Override
	public void cancelRead(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException {
		EnrollmentStateMachine enrollmentStateMachine = null;
		
		try{
			enrollmentStateMachine = enrollmentTask.getEnrollmentStateMachine(cdapSessionDescriptor.getDestinationApplicationProcessNamingInfo(), false);
		}catch(Exception ex){
			log.error(ex);
			try{
				enrollmentTask.getRIBDaemon().sendMessage(cdapSessionManager.getReleaseConnectionRequestMessage(cdapSessionDescriptor.getPortId(), null, false), 
						cdapSessionDescriptor.getPortId(), null);
			}catch(Exception e){
				log.error(e);
			}
		}
		
		if (enrollmentStateMachine == null){
			log.error("Got a CDAP message that is not for me: "+cdapMessage.toString());
			return;
		}
		enrollmentStateMachine.cancelread(cdapMessage, cdapSessionDescriptor);
	}
	
	@Override
	public Object getObjectValue(){
		return null;
	}
}
