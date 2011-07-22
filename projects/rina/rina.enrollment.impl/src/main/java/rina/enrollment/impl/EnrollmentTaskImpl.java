package rina.enrollment.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.ApplicationProcessNameSynonym;
import rina.cdap.api.BaseCDAPSessionManager;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.encoding.api.BaseEncoder;
import rina.encoding.api.Encoder;
import rina.enrollment.api.BaseEnrollmentTask;
import rina.enrollment.impl.handlers.DIFMembersHandler;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBHandler;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * Current limitations: Addresses of IPC processes are allocated forever (until we lose the connection with them)
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
	
	/**
	 * The list of application processes this AP is enrolled to, with 
	 * their addresses
	 */
	private List<ApplicationProcessNameSynonym> members = null;
	
	/** Handles the operations on "daf.management.enrollment.members" objects **/
	private DIFMembersHandler difMembersHandler = null;

	public EnrollmentTaskImpl(){
		enrollmentStateMachines = new Hashtable<String, EnrollmentStateMachine>();
		members = new ArrayList<ApplicationProcessNameSynonym>();
		difMembersHandler = new DIFMembersHandler(this);
	}
	
	/**
	 * Add a member to the list
	 * @param apNameSynonym
	 */
	public void addMember(ApplicationProcessNameSynonym apNameSynonym){
		members.add(apNameSynonym);
	}
	
	/**
	 * Remove a member from the list
	 * @param apNameSynonym
	 */
	public void removeMember(ApplicationProcessNameSynonym apNameSynonym){
		members.remove(apNameSynonym);
	}
	
	public List<ApplicationProcessNameSynonym> getMembers(){
		return this.members;
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
			ribDaemon.addRIBHandler(this, RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.ENROLLMENT);
			ribDaemon.addRIBHandler(this, RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.ENROLLMENT + RIBObjectNames.SEPARATOR + RIBObjectNames.MEMBERS);
			ribDaemon.addRIBHandler(this, RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.OPERATIONAL_STATUS);
		}catch(RIBDaemonException ex){
			ex.printStackTrace();
			log.error("Could not subscribe to RIB Daemon:" +ex.getMessage());
		}
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
		RIBDaemon ribDaemon = (RIBDaemon) getIPCProcess().getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		Encoder encoder = (Encoder) getIPCProcess().getIPCProcessComponent(BaseEncoder.getComponentName());

		EnrollmentStateMachine enrollmentStateMachine = new EnrollmentStateMachine(ribDaemon, cdapSessionManager, encoder, apNamingInfo, this);
		enrollmentStateMachines.put(apNamingInfo.getApplicationProcessName() +"-"+apNamingInfo.getApplicationProcessInstance(), enrollmentStateMachine);
		log.debug("Created a new Enrollment state machine for remote IPC process: "
				+apNamingInfo.getApplicationProcessName()+" "+apNamingInfo.getApplicationProcessInstance());
		return enrollmentStateMachine;
	}

	/**
	 * Called by the RIB Daemon when an M_CONNECT message is received
	 * @param CDAPMessage the cdap message received
	 * @param CDAPSessionDescriptor contains the data about the CDAP session (including the portId)
	 */
	public void connect(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) {
		log.debug("Received M_CONNECT cdapMessage from portId "+cdapSessionDescriptor.getPortId());

		EnrollmentStateMachine enrollmentStateMachine = this.getEnrollmentStateMachine(cdapSessionDescriptor);
		if (enrollmentStateMachine == null){
			log.error("Got a CDAP message that is not for me: "+cdapMessage.toString());
			return;
		}

		enrollmentStateMachine.connect(cdapMessage, cdapSessionDescriptor.getPortId());
	}

	/**
	 * Called by the RIB Daemon when an M_CONNECT_R message is received
	 * @param CDAPMessage the cdap message received
	 * @param CDAPSessionDescriptor contains the data about the CDAP session (including the portId)
	 */
	public void connectResponse(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) {
		log.debug("Received M_CONNECT_R cdapMessage from portId "+cdapSessionDescriptor.getPortId());

		EnrollmentStateMachine enrollmentStateMachine = this.getEnrollmentStateMachine(cdapSessionDescriptor);
		if (enrollmentStateMachine == null){
			log.error("Got a CDAP message that is not for me: "+cdapMessage.toString());
			return;
		}

		enrollmentStateMachine.connectResponse(cdapMessage, cdapSessionDescriptor);
	}

	/**
	 * Called by the RIB Daemon when an M_RELEASE message is received
	 * @param CDAPMessage the cdap message received
	 * @param CDAPSessionDescriptor contains the data about the CDAP session (including the portId)
	 */
	public void release(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor){
		log.debug("Received M_RELEASE cdapMessage from portId "+cdapSessionDescriptor.getPortId());

		EnrollmentStateMachine enrollmentStateMachine = this.getEnrollmentStateMachine(cdapSessionDescriptor);
		if (enrollmentStateMachine == null){
			log.error("Got a CDAP message that is not for me: "+cdapMessage.toString());
			return;
		}

		enrollmentStateMachine.release(cdapMessage, cdapSessionDescriptor);
	}

	/**
	 * Called by the RIB Daemon when an M_RELEASE_R message is received
	 * @param CDAPMessage the cdap message received
	 * @param CDAPSessionDescriptor contains the data about the CDAP session (including the portId)
	 */
	public void releaseResponse(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor){
		log.debug("Received M_RELEASE_R cdapMessage from portId "+cdapSessionDescriptor.getPortId());

		EnrollmentStateMachine enrollmentStateMachine = this.getEnrollmentStateMachine(cdapSessionDescriptor);
		if (enrollmentStateMachine == null){
			log.error("Got a CDAP message that is not for me: "+cdapMessage.toString());
			return;
		}

		enrollmentStateMachine.releaseResponse(cdapMessage, cdapSessionDescriptor);
	}

	/* RIBHANDLER Operations */
	public void read(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException {
		if (cdapMessage.getObjName().equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + RIBObjectNames.SEPARATOR + RIBObjectNames.ENROLLMENT)){
			EnrollmentStateMachine enrollmentStateMachine = this.getEnrollmentStateMachine(cdapSessionDescriptor);
			if (enrollmentStateMachine == null){
				log.error("Got a CDAP message that is not for me: "+cdapMessage.toString());
				return;
			}
			enrollmentStateMachine.read(cdapMessage, cdapSessionDescriptor);
		}else if (cdapMessage.getObjName().equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + RIBObjectNames.SEPARATOR 
				+ RIBObjectNames.ENROLLMENT + RIBObjectNames.SEPARATOR + RIBObjectNames.MEMBERS)){
			difMembersHandler.read(cdapMessage, cdapSessionDescriptor);
		}
	}
	
	public Object read(String objectClass, String objectName, long objectInstance) throws RIBDaemonException {
		if (objectName.equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + RIBObjectNames.SEPARATOR 
				+ RIBObjectNames.ENROLLMENT + RIBObjectNames.SEPARATOR + RIBObjectNames.MEMBERS)){
			return this.members;
		}
		
		return null;
	}
	
	public void cancelRead(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException {
		if (cdapMessage.getObjName().equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + RIBObjectNames.SEPARATOR + RIBObjectNames.ENROLLMENT)){
			EnrollmentStateMachine enrollmentStateMachine = this.getEnrollmentStateMachine(cdapSessionDescriptor);
			if (enrollmentStateMachine == null){
				log.error("Got a CDAP message that is not for me: "+cdapMessage.toString());
				return;
			}
			enrollmentStateMachine.cancelread(cdapMessage, cdapSessionDescriptor);
		}
		
	}

	public void create(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException {
		if (cdapMessage.getObjName().equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + RIBObjectNames.SEPARATOR 
				+ RIBObjectNames.ENROLLMENT + RIBObjectNames.SEPARATOR + RIBObjectNames.MEMBERS)){
			difMembersHandler.create(cdapMessage, cdapSessionDescriptor);
		}
	}

	public void create(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void delete(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException {
		if (cdapMessage.getObjName().equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + RIBObjectNames.SEPARATOR 
				+ RIBObjectNames.ENROLLMENT + RIBObjectNames.SEPARATOR + RIBObjectNames.MEMBERS)){
			difMembersHandler.delete(cdapMessage, cdapSessionDescriptor);
		}
	}

	public void delete(String arg0, String arg1, long arg2, Object arg3) throws RIBDaemonException {
		// TODO Auto-generated method stub
	}

	public void start(CDAPMessage arg0, CDAPSessionDescriptor arg1) throws RIBDaemonException {
		//Do nothing
	}

	public void start(String arg0, String arg1, long arg2, Object arg3) throws RIBDaemonException {
		//Do nothing
	}

	public void stop(CDAPMessage arg0, CDAPSessionDescriptor arg1) throws RIBDaemonException {
		//Do nothing
	}

	public void stop(String arg0, String arg1, long arg2, Object arg3) throws RIBDaemonException {
		//Do nothing
	}

	public void write(CDAPMessage arg0, CDAPSessionDescriptor arg1) throws RIBDaemonException {
		//Do nothing
	}

	public void write(String arg0, String arg1, long arg2, Object arg3) throws RIBDaemonException {
		//Do nothing
	}
	
}
