package rina.enrollment.impl;

import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.BaseCDAPSessionManager;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.Opcode;
import rina.encoding.api.BaseEncoder;
import rina.encoding.api.Encoder;
import rina.enrollment.api.BaseEnrollmentTask;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBHandler;
import rina.rmt.api.BaseRMT;
import rina.rmt.api.RMT;

/**
 * Current limitations: Adresses of IPC processes are allocated forever (until we lose the connection with them)
 * @author eduardgrasa
 *
 */
public class EnrollmentTaskImpl extends BaseEnrollmentTask implements RIBHandler{
	
	private static final Log log = LogFactory.getLog(EnrollmentTaskImpl.class);
	
	/**
	 * Stores the enrollment state machines, one per remote IPC process that this IPC 
	 * process is enrolled to.
	 */
	private Map<String, EnrollmentStateMachine> enrollmentStateMachines = null;

	public EnrollmentTaskImpl(){
		enrollmentStateMachines = new Hashtable<String, EnrollmentStateMachine>();
	}
	
	@Override
	public void setIPCProcess(IPCProcess ipcProcess){
		super.setIPCProcess(ipcProcess);
		subscribeToRIBDaemon();
	}
	
	/**
	 * Subscribe to all M_CONNECTs, M_CONNECT_R, M_RELEASE and M_RELEASE_R
	 */
	private void subscribeToRIBDaemon(){
		RIBDaemon ribDaemon = (RIBDaemon) getIPCProcess().getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		try{
			ribDaemon.addRIBHandler(this, "daf.management.currentSynonym");
			ribDaemon.addRIBHandler(this, "daf.management.enrollment");
			ribDaemon.addRIBHandler(this, "dif.management.operationalStatus");
		}catch(RIBDaemonException ex){
			ex.printStackTrace();
			log.error("Could not subscribe to RIB Daemon:" +ex.getMessage());
		}
	}
	
	public void processOperation(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException {
		log.debug("Received cdapMessage from portId "+cdapSessionDescriptor.getPortId()+" and opcode "+cdapMessage.getOpCode());

		EnrollmentStateMachine enrollmentStateMachine = this.getEnrollmentStateMachine(cdapSessionDescriptor);
		if (enrollmentStateMachine == null){
			log.error("Got a CDAP message that is not for me: "+cdapMessage.toString());
			return;
		}

		enrollmentStateMachine.processCDAPMessage(cdapMessage, cdapSessionDescriptor.getPortId());
	}

	public Object processOperation(Opcode opcode, String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Returns the enrollment state machine associated to the cdap descriptor. If none can be found a new one is created.
	 * @param cdapSessionDescriptor
	 * @return
	 */
	private EnrollmentStateMachine getEnrollmentStateMachine(CDAPSessionDescriptor cdapSessionDescriptor){
		ApplicationProcessNamingInfo myNamingInfo = getIPCProcess().getApplicationProcessNamingInfo();
		ApplicationProcessNamingInfo sourceNamingInfo = cdapSessionDescriptor.getSourceApplicationProcessNamingInfo();
		ApplicationProcessNamingInfo destinationNamingInfo = cdapSessionDescriptor.getDestinationApplicationProcessNamingInfo();
		EnrollmentStateMachine enrollmentStateMachine = null;
		
		if (myNamingInfo.equals(destinationNamingInfo) || getIPCProcess().containsWhatevercastName(destinationNamingInfo.getApplicationProcessName())){
			enrollmentStateMachine = getEnrollmentStateMachine(sourceNamingInfo);
			if (enrollmentStateMachine == null){
				enrollmentStateMachine = this.createEnrollmentStateMachine(sourceNamingInfo);
			}
		}else if (myNamingInfo.equals(sourceNamingInfo) || getIPCProcess().containsWhatevercastName(sourceNamingInfo.getApplicationProcessName())){
			enrollmentStateMachine = getEnrollmentStateMachine(destinationNamingInfo);
			if (enrollmentStateMachine == null){
				enrollmentStateMachine = this.createEnrollmentStateMachine(destinationNamingInfo);
			}
		}else{
			return null;
		}
		
		return enrollmentStateMachine;
	}
	
	private EnrollmentStateMachine getEnrollmentStateMachine(ApplicationProcessNamingInfo apNamingInfo){
		return enrollmentStateMachines.get(apNamingInfo.getApplicationProcessName()+"-"+apNamingInfo.getApplicationProcessInstance());
	}
	
	/**
	 * Creates an enrollment state machine with the remote IPC process identified by the apNamingInfo
	 * @param apNamingInfo
	 * @return
	 */
	private EnrollmentStateMachine createEnrollmentStateMachine(ApplicationProcessNamingInfo apNamingInfo){
		CDAPSessionManager cdapSessionManager = (CDAPSessionManager) getIPCProcess().getIPCProcessComponent(BaseCDAPSessionManager.getComponentName());
		RMT rmt = (RMT) getIPCProcess().getIPCProcessComponent(BaseRMT.getComponentName());
		Encoder encoder = (Encoder) getIPCProcess().getIPCProcessComponent(BaseEncoder.getComponentName());

		EnrollmentStateMachine enrollmentStateMachine = new EnrollmentStateMachine(rmt, cdapSessionManager, encoder);
		enrollmentStateMachines.put(apNamingInfo.getApplicationProcessName() +"-"+apNamingInfo.getApplicationProcessInstance(), enrollmentStateMachine);
		log.debug("Created a new Enrollment state machine for remote IPC process: "
				+apNamingInfo.getApplicationProcessName()+" "+apNamingInfo.getApplicationProcessInstance());
		return enrollmentStateMachine;
	}
	
}
