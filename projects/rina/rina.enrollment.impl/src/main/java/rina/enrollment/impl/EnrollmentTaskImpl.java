package rina.enrollment.impl;

import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.DAFMember;
import rina.cdap.api.BaseCDAPSessionManager;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.encoding.api.BaseEncoder;
import rina.encoding.api.Encoder;
import rina.enrollment.api.BaseEnrollmentTask;
import rina.enrollment.impl.ribobjects.CurrentSynonymRIBObject;
import rina.enrollment.impl.ribobjects.DIFMemberSetRIBObject;
import rina.enrollment.impl.ribobjects.EnrollmentRIBObject;
import rina.enrollment.impl.ribobjects.OperationalStatusRIBObject;
import rina.enrollment.impl.statemachines.DefaultEnrollmentStateMachine;
import rina.enrollment.impl.statemachines.EnrollmentStateMachine;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;
import rina.rmt.api.BaseRMT;
import rina.rmt.api.RMT;

/**
 * Current limitations: Addresses of IPC processes are allocated forever (until we lose the connection with them)
 * @author eduardgrasa
 *
 */
public class EnrollmentTaskImpl extends BaseEnrollmentTask {
	
	private static final Log log = LogFactory.getLog(EnrollmentTaskImpl.class);
	
	/**
	 * Stores the enrollment state machines, one per remote IPC process that this IPC 
	 * process is enrolled to.
	 */
	private Map<String, EnrollmentStateMachine> enrollmentStateMachines = null;
	
	/**
	 * Stores the enrollment requests that have to be replied back
	 */
	private Map<String, PendingEnrollmentRequest> ongoingInitiateEnrollmentRequests = null;
	
	private RIBDaemon ribDaemon = null;
	private Encoder encoder = null;
	private RMT rmt = null;
	private CDAPSessionManager cdapSessionManager = null;

	public EnrollmentTaskImpl(){
		enrollmentStateMachines = new Hashtable<String, EnrollmentStateMachine>();
		ongoingInitiateEnrollmentRequests = new Hashtable<String, PendingEnrollmentRequest>();
	}
	
	@Override
	public void setIPCProcess(IPCProcess ipcProcess){
		super.setIPCProcess(ipcProcess);
		this.ribDaemon = (RIBDaemon) getIPCProcess().getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		this.encoder = (Encoder) getIPCProcess().getIPCProcessComponent(BaseEncoder.getComponentName());
		this.rmt = (RMT) getIPCProcess().getIPCProcessComponent(BaseRMT.getComponentName());
		this.cdapSessionManager = (CDAPSessionManager) getIPCProcess().getIPCProcessComponent(BaseCDAPSessionManager.getComponentName());
		populateRIB(ipcProcess);
	}
	
	/**
	 * Subscribe to all M_CONNECTs, M_CONNECT_R, M_RELEASE and M_RELEASE_R
	 */
	private void populateRIB(IPCProcess ipcProcess){
		try{
			RIBObject ribObject = new DIFMemberSetRIBObject(this, ipcProcess);
			ribDaemon.addRIBObject(ribObject);
			ribObject = new EnrollmentRIBObject(this, ipcProcess);
			ribDaemon.addRIBObject(ribObject);
			ribObject = new OperationalStatusRIBObject(this, ipcProcess);
			ribDaemon.addRIBObject(ribObject);
			ribObject = new CurrentSynonymRIBObject(ipcProcess, this);
			ribDaemon.addRIBObject(ribObject);
		}catch(RIBDaemonException ex){
			ex.printStackTrace();
			log.error("Could not subscribe to RIB Daemon:" +ex.getMessage());
		}
	}
	
	public RIBDaemon getRIBDaemon(){
		return this.ribDaemon;
	}
	
	/**
	 * Returns the enrollment state machine associated to the cdap descriptor.
	 * @param cdapSessionDescriptor
	 * @return
	 */
	private EnrollmentStateMachine getEnrollmentStateMachine(CDAPSessionDescriptor cdapSessionDescriptor) throws Exception{
		try{
			ApplicationProcessNamingInfo myNamingInfo = (ApplicationProcessNamingInfo) ribDaemon.read(null, 
					RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + RIBObjectNames.SEPARATOR + 
					RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.APNAME, 0).getObjectValue();
			ApplicationProcessNamingInfo sourceNamingInfo = cdapSessionDescriptor.getSourceApplicationProcessNamingInfo();
			ApplicationProcessNamingInfo destinationNamingInfo = cdapSessionDescriptor.getDestinationApplicationProcessNamingInfo();
			
			if (myNamingInfo.getApplicationProcessName().equals(sourceNamingInfo.getApplicationProcessName()) && 
					myNamingInfo.getApplicationProcessInstance().equals(sourceNamingInfo.getApplicationProcessInstance())){
				return getEnrollmentStateMachine(destinationNamingInfo);
			}else{
				throw new Exception("This IPC process is not the intended recipient of the CDAP message");
			}
		}catch(RIBDaemonException ex){
			log.error(ex);
			return null;
		}
	}
	
	public EnrollmentStateMachine getEnrollmentStateMachine(ApplicationProcessNamingInfo apNamingInfo){
		return enrollmentStateMachines.get(apNamingInfo.getApplicationProcessName()+
				apNamingInfo.getApplicationProcessInstance()+apNamingInfo.getApplicationEntityName());
	}
	
	private boolean isEnrolledTo(String applicationProcessName, String applicationProcessInstance){
		EnrollmentStateMachine enrollmentStateMachine = enrollmentStateMachines.get(applicationProcessName+"-"+applicationProcessInstance);
		if (enrollmentStateMachine == null){
			return false;
		}
		if (!enrollmentStateMachine.getState().equals(EnrollmentStateMachine.State.NULL)){
			return true;
		}
		
		return false;
	}
	
	/**
	 * Creates an enrollment state machine with the remote IPC process identified by the apNamingInfo
	 * @param apNamingInfo
	 * @return
	 */
	private EnrollmentStateMachine createEnrollmentStateMachine(ApplicationProcessNamingInfo apNamingInfo) throws Exception{
		CDAPSessionManager cdapSessionManager = (CDAPSessionManager) getIPCProcess().getIPCProcessComponent(BaseCDAPSessionManager.getComponentName());
		RIBDaemon ribDaemon = (RIBDaemon) getIPCProcess().getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		Encoder encoder = (Encoder) getIPCProcess().getIPCProcessComponent(BaseEncoder.getComponentName());
		EnrollmentStateMachine enrollmentStateMachine = null;
		
		if (apNamingInfo.getApplicationEntityName().equals(DefaultEnrollmentStateMachine.DEFAULT_ENROLLMENT)){
			enrollmentStateMachine = new DefaultEnrollmentStateMachine(ribDaemon, cdapSessionManager, encoder, apNamingInfo, this);
			enrollmentStateMachines.put(apNamingInfo.getApplicationProcessName() + 
					apNamingInfo.getApplicationProcessInstance() + apNamingInfo.getApplicationEntityName(), enrollmentStateMachine);
			log.debug("Created a new Enrollment state machine for remote IPC process: "
					+ apNamingInfo.getApplicationProcessName()+" "+apNamingInfo.getApplicationProcessInstance() 
					+ " " + apNamingInfo.getApplicationEntityName());
			return enrollmentStateMachine;
		}
		
		throw new Exception("Unknown application entity for enrollment: "+apNamingInfo.getApplicationEntityName());
	}
	
	/**
	 * Called by the DIFMemberSetRIBObject when a CREATE request for a new member is received
	 * @param cdapMessage
	 * @param cdapSessionDescriptor
	 */
	public synchronized void initiateEnrollment(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor){
		//TODO
		DAFMember candidate = null;
		ApplicationProcessNamingInfo candidateNamingInfo = null;
		EnrollmentStateMachine enrollmentStateMachine = null;
		PendingEnrollmentRequest pendingEnrollmentRequest = null;
		int portId = 0;
		
		//1 Check that we're not already enrolled to the IPC Process
		try{
			candidate = (DAFMember) encoder.decode(cdapMessage.getObjValue().getByteval(), DAFMember.class.toString());
		}catch(Exception ex){
			log.error(ex);
			//TODO return M_CREATE_R with error code answer.
			return;
		}
		
		if (this.isEnrolledTo(candidate.getApplicationProcessName(), candidate.getApplicationProcessInstance())){
			log.error("Already enrolled to IPC Process "+candidate.getApplicationProcessName()+"-"+candidate.getApplicationProcessInstance());
			//TODO return M_CREATE_R with error code answer
			return;
		}
		
		
		//2 Tell the RMT to allocate a new flow to the IPC process  (will return a port Id)
		candidateNamingInfo = new ApplicationProcessNamingInfo(candidate.getApplicationProcessName(), candidate.getApplicationProcessInstance());
		candidateNamingInfo.setApplicationEntityName(DefaultEnrollmentStateMachine.DEFAULT_ENROLLMENT);
		try{
			portId = rmt.allocateFlow(candidateNamingInfo, null);
		}catch(Exception ex){
			log.error(ex);
			//This should never happen, log the error to fix it.
			return;
		}
		
		//3 Tell the enrollment task to create a new Enrollment state machine (or get one if we had already enrolled with the remote IPC process in the past)
		enrollmentStateMachine = this.getEnrollmentStateMachine(candidateNamingInfo);
		if (enrollmentStateMachine == null){
			try{
				enrollmentStateMachine = this.createEnrollmentStateMachine(candidateNamingInfo);
			}catch(Exception ex){
				//Should never happen, fix it!
				log.error(ex);
				return;
			}
		}
		
		//4 Store the request. When the enrollment sequence has completed, either successfully or not, will reply back to the requester.
		pendingEnrollmentRequest = new PendingEnrollmentRequest(cdapMessage, cdapSessionDescriptor, portId);
		ongoingInitiateEnrollmentRequests.put(candidate.getApplicationProcessName()+"-"+candidate.getApplicationProcessInstance(), pendingEnrollmentRequest);
		
		//5 Tell the enrollment state machine to initiate the enrollment (will require an M_CONNECT message and a port Id)
		enrollmentStateMachine.initiateEnrollment(candidate, portId);
	}
	
	/**
	 * Called by the enrollment state machine when the enrollment request has been completed, either successfully or unsuccessfully
	 * @param candidate the IPC process we were trying to enroll to
	 * @param result the result of the operation (0 = successful, >0 errors)
	 * @param resultReason if result >0, a String explaining what was the problem
	 */
	public synchronized void enrollmentCompleted(DAFMember candidate, int result, String resultReason){
		CDAPMessage responseMessage = null;
		CDAPMessage requestMessage = null;
		PendingEnrollmentRequest pendingEnrollmentRequest = ongoingInitiateEnrollmentRequests.remove(
				candidate.getApplicationProcessName()+"-"+candidate.getApplicationProcessInstance());
		
		if (pendingEnrollmentRequest == null){
			log.error("Did not found a pending enrollment request");
			return;
		}
		
		requestMessage = pendingEnrollmentRequest.getCdapMessage();
		if (result == 0){
			try{
				ribDaemon.create(requestMessage.getObjClass(), requestMessage.getObjName(), requestMessage.getObjInst(), candidate);
			}catch(RIBDaemonException ex){
				log.error(ex);
				//This must not happen, log the error and fix it
				return;
			}
		}else{
			try{
				rmt.deallocateFlow(pendingEnrollmentRequest.getPortId());
			}catch(Exception ex){
				log.error(ex);
				//This must not happen, log the error and fix it
				return;
			}
		}
		
		try{
			int portId = pendingEnrollmentRequest.getCdapSessionDescriptor().getPortId();
			responseMessage = cdapSessionManager.getCreateObjectResponseMessage(portId, null, requestMessage.getObjClass(), 
					requestMessage.getObjInst(), requestMessage.getObjName(), null, result, resultReason, requestMessage.getInvokeID());
			ribDaemon.sendMessage(responseMessage, portId, null);
		}catch(Exception ex){
			log.error(ex);
		}
	}

	/**
	 * Called by the RIB Daemon when an M_CONNECT message is received
	 * @param CDAPMessage the cdap message received
	 * @param CDAPSessionDescriptor contains the data about the CDAP session (including the portId)
	 */
	public void connect(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) {
		log.debug("Received M_CONNECT cdapMessage from portId "+cdapSessionDescriptor.getPortId());
		cdapSessionDescriptor.setDestAEName(cdapSessionDescriptor.getSrcAEName());

		try{
			EnrollmentStateMachine enrollmentStateMachine = this.getEnrollmentStateMachine(cdapSessionDescriptor);
			if (enrollmentStateMachine == null){
				enrollmentStateMachine = this.createEnrollmentStateMachine(cdapSessionDescriptor.getDestinationApplicationProcessNamingInfo());
			}
			enrollmentStateMachine.connect(cdapMessage, cdapSessionDescriptor.getPortId());
		}catch(Exception ex){
			//Error creating or getting the enrollment state machine
			log.error(ex.getMessage());
			try{
				int portId = cdapMessage.getInvokeID();
				ribDaemon.sendMessage(
						cdapSessionManager.getOpenConnectionResponseMessage(portId, cdapMessage.getAuthMech(), null, cdapMessage.getSrcAEInst(), cdapMessage.getSrcAEName(), 
								cdapMessage.getSrcApInst(), cdapMessage.getSrcApName(), -2, ex.getMessage(), null, cdapMessage.getDestAEName(), cdapMessage.getDestApInst(), 
								cdapMessage.getDestApName(), cdapMessage.getInvokeID()), portId, null);
			}catch(Exception e){
				log.error(e);
			}
		}
	}

	/**
	 * Called by the RIB Daemon when an M_CONNECT_R message is received
	 * @param CDAPMessage the cdap message received
	 * @param CDAPSessionDescriptor contains the data about the CDAP session (including the portId)
	 */
	public void connectResponse(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) {
		log.debug("Received M_CONNECT_R cdapMessage from portId "+cdapSessionDescriptor.getPortId());

		try{
			EnrollmentStateMachine enrollmentStateMachine = this.getEnrollmentStateMachine(cdapSessionDescriptor);
			enrollmentStateMachine.connectResponse(cdapMessage, cdapSessionDescriptor);
		}catch(Exception ex){
			//Error getting the enrollment state machine
			log.error(ex.getMessage());
			try{
				int portId = cdapSessionDescriptor.getPortId();
				ribDaemon.sendMessage(cdapSessionManager.getReleaseConnectionRequestMessage(portId, null, false), portId, null);
			}catch(Exception e){
				log.error(e);
			}
		}
	}

	/**
	 * Called by the RIB Daemon when an M_RELEASE message is received
	 * @param CDAPMessage the cdap message received
	 * @param CDAPSessionDescriptor contains the data about the CDAP session (including the portId)
	 */
	public void release(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor){
		log.debug("Received M_RELEASE cdapMessage from portId "+cdapSessionDescriptor.getPortId());

		try{
			EnrollmentStateMachine enrollmentStateMachine = this.getEnrollmentStateMachine(cdapSessionDescriptor);
			enrollmentStateMachine.release(cdapMessage, cdapSessionDescriptor);
		}catch(Exception ex){
			//Error getting the enrollment state machine
			log.error(ex.getMessage());
			try{
				int portId = cdapSessionDescriptor.getPortId();
				ribDaemon.sendMessage(cdapSessionManager.getReleaseConnectionRequestMessage(portId, null, false), portId, null);
			}catch(Exception e){
				log.error(e);
			}
		}
	}

	/**
	 * Called by the RIB Daemon when an M_RELEASE_R message is received
	 * @param CDAPMessage the cdap message received
	 * @param CDAPSessionDescriptor contains the data about the CDAP session (including the portId)
	 */
	public void releaseResponse(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor){
		log.debug("Received M_RELEASE_R cdapMessage from portId "+cdapSessionDescriptor.getPortId());

		try{
			EnrollmentStateMachine enrollmentStateMachine = this.getEnrollmentStateMachine(cdapSessionDescriptor);
			enrollmentStateMachine.releaseResponse(cdapMessage, cdapSessionDescriptor);
		}catch(Exception ex){
			//Error getting the enrollment state machine
			log.error(ex.getMessage());
			try{
				int portId = cdapSessionDescriptor.getPortId();
				ribDaemon.sendMessage(cdapSessionManager.getReleaseConnectionRequestMessage(portId, null, false), portId, null);
			}catch(Exception e){
				log.error(e);
			}
		}
	}
}
