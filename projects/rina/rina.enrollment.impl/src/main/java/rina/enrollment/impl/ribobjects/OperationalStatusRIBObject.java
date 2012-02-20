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
import rina.ribdaemon.api.RIBObjectNames;

/**
 * Handles the operations related to the "daf.management.operationalStatus" objects
 * @author eduardgrasa
 *
 */
public class OperationalStatusRIBObject extends BaseRIBObject{
	
	private static final Log log = LogFactory.getLog(OperationalStatusRIBObject.class);

	private EnrollmentTaskImpl enrollmentTask = null;
	private Boolean started = false;
	private CDAPSessionManager cdapSessionManager = null;

	public OperationalStatusRIBObject(EnrollmentTaskImpl enrollmentTaskImpl, IPCProcess ipcProcess){
		super(ipcProcess, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.OPERATIONAL_STATUS, "operationstatus", ObjectInstanceGenerator.getObjectInstance());
		this.enrollmentTask = enrollmentTaskImpl;
		this.cdapSessionManager = (CDAPSessionManager) getIPCProcess().getIPCProcessComponent(BaseCDAPSessionManager.getComponentName());
	}
	
	@Override
	public void start(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor){
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
		
		enrollmentStateMachine.start(cdapMessage, cdapSessionDescriptor);
		this.started = true;
	}
	
	@Override
	public Object getObjectValue(){
		return started;
	}

}
