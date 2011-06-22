package rina.enrollment.impl;

import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.Opcode;
import rina.enrollment.api.BaseEnrollmentTask;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.MessageSubscriber;
import rina.ribdaemon.api.MessageSubscription;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;

/**
 * Current limitations: Adresses of IPC processes are allocated forever (until we lose the connection with them)
 * @author eduardgrasa
 *
 */
public class EnrollmentTaskImpl extends BaseEnrollmentTask implements MessageSubscriber{
	
	private static final Log log = LogFactory.getLog(EnrollmentTaskImpl.class);
	
	/**
	 * Stores the enrollment state machines, one per remote IPC process that this IPC 
	 * process is enrolled to.
	 */
	private Map<ApplicationProcessNamingInfo, EnrollmentStateMachine> enrollmentStateMachines = null;

	public EnrollmentTaskImpl(){
		enrollmentStateMachines = new Hashtable<ApplicationProcessNamingInfo, EnrollmentStateMachine>();
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
			MessageSubscription messageSubscription = new MessageSubscription();
			messageSubscription.setOpCode(Opcode.M_CONNECT);
			ribDaemon.subscribeToMessages(messageSubscription, this);
			
			messageSubscription = new MessageSubscription();
			messageSubscription.setOpCode(Opcode.M_CONNECT_R);
			ribDaemon.subscribeToMessages(messageSubscription, this);
			
			messageSubscription = new MessageSubscription();
			messageSubscription.setOpCode(Opcode.M_RELEASE);
			ribDaemon.subscribeToMessages(messageSubscription, this);
			
			messageSubscription = new MessageSubscription();
			messageSubscription.setOpCode(Opcode.M_RELEASE_R);
			ribDaemon.subscribeToMessages(messageSubscription, this);
		}catch(RIBDaemonException ex){
			ex.printStackTrace();
			log.error("Could not subscribe to RIB Daemon:" +ex.getMessage());
		}
	}
	
	/**
	 * Called by the RIB Daemon
	 */
	public void messageReceived(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) {
		log.debug("Received cdapMessage from portId "+cdapSessionDescriptor.getPortId()+" and opcode "+cdapMessage.getOpCode());
		
		EnrollmentStateMachine enrollmentStateMachine = this.getEnrollmentStateMachine(cdapSessionDescriptor);
		if (enrollmentStateMachine == null){
			log.error("Got a CDAP message that is not for me: "+cdapMessage.toString());
			return;
		}
		
		//TODO pass the message to the EnrollmentStateMachine and let it do the job
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
		
		if (myNamingInfo.equals(destinationNamingInfo)){
			enrollmentStateMachine = enrollmentStateMachines.get(destinationNamingInfo);
			if (enrollmentStateMachine == null){
				enrollmentStateMachine = this.createEnrollmentStateMachine(destinationNamingInfo);
			}
		}else if (myNamingInfo.equals(destinationNamingInfo)){
			enrollmentStateMachine = enrollmentStateMachines.get(sourceNamingInfo);
			if (enrollmentStateMachine == null){
				enrollmentStateMachine = this.createEnrollmentStateMachine(sourceNamingInfo);
			}
		}else{
			return null;
		}
		
		return enrollmentStateMachine;
	}
	
	/**
	 * Creates an enrollment state machine with the remote IPC process identified by the apNamingInfo
	 * @param apNamingInfo
	 * @return
	 */
	private EnrollmentStateMachine createEnrollmentStateMachine(ApplicationProcessNamingInfo apNamingInfo){
		EnrollmentStateMachine enrollmentStateMachine = new EnrollmentStateMachine();
		enrollmentStateMachines.put(apNamingInfo, enrollmentStateMachine);
		return enrollmentStateMachine;
	}
}
