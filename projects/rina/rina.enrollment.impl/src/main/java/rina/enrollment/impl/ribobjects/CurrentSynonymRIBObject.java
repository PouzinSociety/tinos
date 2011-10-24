package rina.enrollment.impl.ribobjects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.BaseCDAPSessionManager;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.enrollment.impl.EnrollmentTaskImpl;
import rina.enrollment.impl.statemachines.EnrollmentStateMachine.State;
import rina.enrollment.impl.statemachines.EnrollmentStateMachine;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.ObjectInstanceGenerator;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * Handles the operations related to the "daf.management.naming.currentsynonym" objects
 * @author eduardgrasa
 *
 */
public class CurrentSynonymRIBObject extends BaseRIBObject{
	
	private static final Log log = LogFactory.getLog(CurrentSynonymRIBObject.class);
	
	private Long synonym = null;
	private EnrollmentTaskImpl enrollmentTask = null;
	private CDAPSessionManager cdapSessionManager = null;
	
	public CurrentSynonymRIBObject(IPCProcess ipcProcess, EnrollmentTaskImpl enrollmentTask){
		super(ipcProcess, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.CURRENT_SYNONYM, 
				"synonym", ObjectInstanceGenerator.getObjectInstance());
		this.enrollmentTask = enrollmentTask;
		this.cdapSessionManager = (CDAPSessionManager) getIPCProcess().getIPCProcessComponent(BaseCDAPSessionManager.getComponentName());
	}
	
	@Override
	public void read(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException{
		EnrollmentStateMachine enrollmentStateMachine = null;
		
		try{
			enrollmentStateMachine = enrollmentTask.getEnrollmentStateMachine(cdapSessionDescriptor.getDestinationApplicationProcessNamingInfo());
		}catch(Exception ex){
			log.error(ex);
			try{
				enrollmentTask.getRIBDaemon().sendMessage(cdapSessionManager.getReleaseConnectionRequestMessage(cdapSessionDescriptor.getPortId(), null, false), 
						cdapSessionDescriptor.getPortId(), null);
			}catch(Exception e){
				log.error(e);
			}
		}

		if (enrollmentStateMachine.getState().equals(State.WAITING_READ_ADDRESS)){
			enrollmentStateMachine.read(cdapMessage, cdapSessionDescriptor);
		}else{
			super.read(cdapMessage, cdapSessionDescriptor);
		}

	}

	@Override
	public RIBObject read(String objectClass, String objectName, long objectInstance) throws RIBDaemonException{
		return this;
	}

	@Override
	public void write(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		if (!(object instanceof Long)){
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
					"Object class ("+object.getClass().getName()+") does not match object name "+objectName);
		}
		
		this.synonym = (Long) object;
	}
	
	@Override
	public Object getObjectValue(){
		return synonym;
	}

}
