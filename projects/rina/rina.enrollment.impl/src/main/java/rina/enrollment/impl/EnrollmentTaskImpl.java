package rina.enrollment.impl;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.BaseCDAPSessionManager;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.encoding.api.BaseEncoder;
import rina.encoding.api.Encoder;
import rina.enrollment.api.AddressManager;
import rina.enrollment.api.BaseEnrollmentTask;
import rina.enrollment.api.Neighbor;
import rina.enrollment.impl.ribobjects.AddressRIBObject;
import rina.enrollment.impl.ribobjects.NeighborSetRIBObject;
import rina.enrollment.impl.ribobjects.EnrollmentRIBObject;
import rina.enrollment.impl.ribobjects.OperationalStatusRIBObject;
import rina.enrollment.impl.statemachines.BaseEnrollmentStateMachine;
import rina.enrollment.impl.statemachines.EnrolleeStateMachine;
import rina.enrollment.impl.statemachines.EnrollerStateMachine;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.IPCException;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
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
	 * Stores the enrollee state machines, one per remote IPC process that this IPC 
	 * process is enrolled to.
	 */
	private Map<String, BaseEnrollmentStateMachine> enrollmentStateMachines = null;
	
	/**
	 * Stores the enrollment requests that have to be replied back
	 */
	private Map<String, PendingEnrollmentRequest> ongoingInitiateEnrollmentRequests = null;
	
	/**
	 * The class that manages the address allocation
	 */
	private AddressManager addressManager = null;
	
	/**
	 * The maximum time to wait between steps of the enrollment sequence (in ms)
	 */
	private long timeout = 0;
	
	private RIBDaemon ribDaemon = null;
	private Encoder encoder = null;
	private RMT rmt = null;
	private CDAPSessionManager cdapSessionManager = null;

	public EnrollmentTaskImpl(){
		enrollmentStateMachines = new Hashtable<String, BaseEnrollmentStateMachine>();
		ongoingInitiateEnrollmentRequests = new Hashtable<String, PendingEnrollmentRequest>();
		try{
			timeout = Long.parseLong(System.getProperty(BaseEnrollmentTask.ENROLLMENT_TASK_TIMEOUT_PROPERTY));
		}catch(Exception ex){
			log.info("Property "+BaseEnrollmentTask.ENROLLMENT_TASK_TIMEOUT_PROPERTY+" not found or invalid, " +
					"using default timeout ("+BaseEnrollmentTask.DEFAULT_TIMEOUT+" ms)");
			timeout = BaseEnrollmentTask.DEFAULT_TIMEOUT;
		}
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
			RIBObject ribObject = new NeighborSetRIBObject(this, ipcProcess);
			ribDaemon.addRIBObject(ribObject);
			ribObject = new EnrollmentRIBObject(this, ipcProcess);
			ribDaemon.addRIBObject(ribObject);
			ribObject = new OperationalStatusRIBObject(this, ipcProcess);
			ribDaemon.addRIBObject(ribObject);
			ribObject = new AddressRIBObject(ipcProcess, this);
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
	private BaseEnrollmentStateMachine getEnrollmentStateMachine(CDAPSessionDescriptor cdapSessionDescriptor, boolean remove) throws Exception{
		try{
			ApplicationProcessNamingInfo myNamingInfo = this.getIPCProcess().getApplicationProcessNamingInfo();
			ApplicationProcessNamingInfo sourceNamingInfo = cdapSessionDescriptor.getSourceApplicationProcessNamingInfo();
			ApplicationProcessNamingInfo destinationNamingInfo = cdapSessionDescriptor.getDestinationApplicationProcessNamingInfo();
			
			if (myNamingInfo.getApplicationProcessName().equals(sourceNamingInfo.getApplicationProcessName()) && 
					myNamingInfo.getApplicationProcessInstance().equals(sourceNamingInfo.getApplicationProcessInstance())){
				return getEnrollmentStateMachine(destinationNamingInfo, cdapSessionDescriptor.getPortId(), remove);
			}else{
				throw new Exception("This IPC process is not the intended recipient of the CDAP message");
			}
		}catch(RIBDaemonException ex){
			log.error(ex);
			return null;
		}
	}
	
	/**
	 * Gets and/or removes an existing enrollment state machine from the list of enrollment state machines
	 * @param apNamingInfo
	 * @param remove
	 * @return
	 */
	public BaseEnrollmentStateMachine getEnrollmentStateMachine(ApplicationProcessNamingInfo apNamingInfo, int portId, boolean remove){
		if (remove){
			return enrollmentStateMachines.remove(apNamingInfo.getProcessKey()+"-"+portId);
		}else{
			return enrollmentStateMachines.get(apNamingInfo.getProcessKey()+"-"+portId);
		}
	}
	
	/**
	 * Finds out if the ICP process is already enrolled to the IPC process identified by 
	 * the provided apNamingInfo
	 * @param apNamingInfo
	 * @return
	 */
	private boolean isEnrolledTo(ApplicationProcessNamingInfo apNamingInfo){
		Iterator<Entry<String, BaseEnrollmentStateMachine>> iterator  = enrollmentStateMachines.entrySet().iterator();
		
		while(iterator.hasNext()){
			if (iterator.next().getValue().getRemotePeerNamingInfo().getProcessKey().equals(apNamingInfo.getProcessKey())){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Creates an enrollment state machine with the remote IPC process identified by the apNamingInfo
	 * @param apNamingInfo
	 * @param enrollee true if this IPC process is the one that initiated the 
	 * enrollment sequence (i.e. it is the application process that wants to 
	 * join the DIF)
	 * @return
	 */
	private BaseEnrollmentStateMachine createEnrollmentStateMachine(ApplicationProcessNamingInfo apNamingInfo, int portId, boolean enrollee) throws Exception{
		CDAPSessionManager cdapSessionManager = (CDAPSessionManager) getIPCProcess().getIPCProcessComponent(BaseCDAPSessionManager.getComponentName());
		RIBDaemon ribDaemon = (RIBDaemon) getIPCProcess().getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		Encoder encoder = (Encoder) getIPCProcess().getIPCProcessComponent(BaseEncoder.getComponentName());
		BaseEnrollmentStateMachine enrollmentStateMachine = null;
		
		if (apNamingInfo.getApplicationEntityName() == null || 
				apNamingInfo.getApplicationEntityName().equals(BaseEnrollmentStateMachine.DEFAULT_ENROLLMENT)){
			if (enrollee){
				enrollmentStateMachine = new EnrolleeStateMachine(ribDaemon, cdapSessionManager, encoder, 
					apNamingInfo, this, timeout);
			}else{
				enrollmentStateMachine = new EnrollerStateMachine(ribDaemon, cdapSessionManager, encoder, 
						apNamingInfo, this, timeout);
			}
			
			enrollmentStateMachines.put(apNamingInfo.getProcessKey()+"-"+portId, enrollmentStateMachine);
			log.debug("Created a new Enrollment state machine for remote IPC process: " + apNamingInfo.getProcessKey());
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
		Neighbor candidate = null;
		ApplicationProcessNamingInfo candidateNamingInfo = null;
		EnrolleeStateMachine enrollmentStateMachine = null;
		PendingEnrollmentRequest pendingEnrollmentRequest = null;
		CDAPMessage responseMessage = null;
		int portId = 0;
		
		//1 Check that we're not already enrolled to the IPC Process
		try{
			candidate = (Neighbor) encoder.decode(cdapMessage.getObjValue().getByteval(), Neighbor.class);
			candidateNamingInfo = new ApplicationProcessNamingInfo(candidate.getApplicationProcessName(), 
					candidate.getApplicationProcessInstance(), BaseEnrollmentStateMachine.DEFAULT_ENROLLMENT, null);
		}catch(Exception ex){
			log.error(ex);
			try{
				responseMessage = cdapSessionManager.getCreateObjectResponseMessage(cdapSessionDescriptor.getPortId(), 
						null, cdapMessage.getObjClass(), cdapMessage.getObjInst(), cdapMessage.getObjName(), null, -2, 
						ex.getMessage(), cdapMessage.getInvokeID());
				ribDaemon.sendMessage(responseMessage, cdapSessionDescriptor.getPortId(), null);
			}catch(Exception e) {
				log.error(e);
			}
			return;
		}
		
		if (this.isEnrolledTo(candidateNamingInfo)){
			String message = "Already enrolled to IPC Process "+candidateNamingInfo.getProcessKey();
			log.error(message);
			try{
				responseMessage = cdapSessionManager.getCreateObjectResponseMessage(cdapSessionDescriptor.getPortId(), 
						null, cdapMessage.getObjClass(), cdapMessage.getObjInst(), cdapMessage.getObjName(), null, -2, 
						message, cdapMessage.getInvokeID());
				ribDaemon.sendMessage(responseMessage, cdapSessionDescriptor.getPortId(), null);
			}catch(Exception ex) {
				log.error(ex);
			}
			return;
		}
		
		
		//2 Tell the RMT to allocate a new flow to the IPC process  (will return a port Id)
		try{
			portId = rmt.allocateFlow(candidateNamingInfo, null);
		}catch(Exception ex){
			log.error(ex);
			//This should never happen, log the error to fix it.
			return;
		}
		
		//3 Tell the enrollment task to create a new Enrollment state machine
		try{
			enrollmentStateMachine = (EnrolleeStateMachine) this.createEnrollmentStateMachine(candidateNamingInfo, portId, true);
		}catch(Exception ex){
			//Should never happen, fix it!
			log.error(ex);
			return;
		}
		
		//4 Store the request. When the enrollment sequence has completed, either successfully or not, will reply back to the requester.
		pendingEnrollmentRequest = new PendingEnrollmentRequest(cdapMessage, cdapSessionDescriptor, portId);
		ongoingInitiateEnrollmentRequests.put(candidateNamingInfo.getProcessKey(), pendingEnrollmentRequest);
		
		//5 Tell the enrollment state machine to initiate the enrollment (will require an M_CONNECT message and a port Id)
		try{
			enrollmentStateMachine.initiateEnrollment(candidate, portId);
		}catch(IPCException ex){
			log.error(ex);
			ongoingInitiateEnrollmentRequests.remove(candidateNamingInfo.getProcessKey());
			try{
				responseMessage = cdapSessionManager.getCreateObjectResponseMessage(cdapSessionDescriptor.getPortId(), 
						null, cdapMessage.getObjClass(), cdapMessage.getObjInst(), cdapMessage.getObjName(), null, -2, 
						ex.getMessage(), cdapMessage.getInvokeID());
				ribDaemon.sendMessage(responseMessage, cdapSessionDescriptor.getPortId(), null);
			}catch(Exception e) {
				log.error(e);
			}
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

		//1 Find out if we are already enrolled to the remote IPC process
		if (this.isEnrolledTo(cdapSessionDescriptor.getDestinationApplicationProcessNamingInfo())){
			try{
				String message = "Received an enrollment request for an IPC process I'm already enrolled to";
				log.error(message);
				int portId = cdapSessionDescriptor.getPortId();
				CDAPMessage errorMessage = 
						cdapSessionManager.getOpenConnectionResponseMessage(portId, cdapMessage.getAuthMech(), null, cdapMessage.getSrcAEInst(), cdapMessage.getSrcAEName(), 
								cdapMessage.getSrcApInst(), cdapMessage.getSrcApName(), -2, message, null, cdapMessage.getDestAEName(), cdapMessage.getDestApInst(), 
								cdapMessage.getDestApName(), cdapMessage.getInvokeID());
				sendErrorMessageAndDeallocateFlow(errorMessage, portId);
			}catch(Exception e){
				log.error(e);
			}
			
			return;
		}
		
		//2 Initiate the enrollment
		try{

			EnrollerStateMachine enrollmentStateMachine = (EnrollerStateMachine) this.createEnrollmentStateMachine(
					cdapSessionDescriptor.getDestinationApplicationProcessNamingInfo(), 
					cdapSessionDescriptor.getPortId(), false);
			enrollmentStateMachine.connect(cdapMessage, cdapSessionDescriptor.getPortId());
		}catch(Exception ex){
			log.error(ex.getMessage());
			try{
				int portId = cdapSessionDescriptor.getPortId();
				CDAPMessage errorMessage =
					cdapSessionManager.getOpenConnectionResponseMessage(portId, cdapMessage.getAuthMech(), null, cdapMessage.getSrcAEInst(), cdapMessage.getSrcAEName(), 
							cdapMessage.getSrcApInst(), cdapMessage.getSrcApName(), -2, ex.getMessage(), null, cdapMessage.getDestAEName(), cdapMessage.getDestApInst(), 
							cdapMessage.getDestApName(), cdapMessage.getInvokeID());
				sendErrorMessageAndDeallocateFlow(errorMessage, portId);
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
			EnrolleeStateMachine enrollmentStateMachine = (EnrolleeStateMachine) this.getEnrollmentStateMachine(cdapSessionDescriptor, false);
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
			BaseEnrollmentStateMachine enrollmentStateMachine = this.getEnrollmentStateMachine(cdapSessionDescriptor, true);
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
			BaseEnrollmentStateMachine enrollmentStateMachine = this.getEnrollmentStateMachine(cdapSessionDescriptor, true);
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
	
	/**
	 * Called by the RIB Daemon when the flow supporting the CDAP session with the remote peer
	 * has been deallocated
	 * @param cdapSessionDescriptor
	 */
	public void flowDeallocated(CDAPSessionDescriptor cdapSessionDescriptor){
		try{
			BaseEnrollmentStateMachine enrollmentStateMachine = this.getEnrollmentStateMachine(cdapSessionDescriptor, true);
			if (enrollmentStateMachine == null){
				//Do nothing, we had already cleaned up
				return;
			}else{
				enrollmentStateMachine.flowDeallocated(cdapSessionDescriptor);
			}
		}catch(Exception ex){
			log.error(ex);
		}
	}
	
	/**
	 * Called by the enrollment state machine when the enrollment sequence fails
	 * @param remotePeer
	 * @param portId
	 * @param enrollee
	 * @param sendMessage
	 * @param reason
	 */
	 public void enrollmentFailed(ApplicationProcessNamingInfo remotePeerNamingInfo, int portId, 
			 String reason, boolean enrollee, boolean sendReleaseMessage){
		 log.error("An error happened during enrollment of remote IPC Process "+ 
					remotePeerNamingInfo.getProcessKey()+ " because of " +reason+". Aborting the operation");
		 //1 Remove enrollment state machine from the store
		 this.getEnrollmentStateMachine(remotePeerNamingInfo, portId, true);
		 
		 //2 Send message and deallocate flow if required
		 if(sendReleaseMessage){
			 try{
					CDAPMessage errorMessage = cdapSessionManager.getReleaseConnectionRequestMessage(portId, null, false);
					sendErrorMessageAndDeallocateFlow(errorMessage, portId);
				}catch(Exception ex){
					log.error(ex);
				}
		 }
		 
		 //3 Reply to the entity that has triggered the enrollment if enrollee
		 if (enrollee){
			PendingEnrollmentRequest pendingEnrollmentRequest = ongoingInitiateEnrollmentRequests.remove(remotePeerNamingInfo.getProcessKey());
			if (pendingEnrollmentRequest == null){
				log.error("Did not find a pending enrollment request");
				return;
			}
			
			CDAPMessage requestMessage = pendingEnrollmentRequest.getCdapMessage();
			CDAPMessage responseMessage = null;
			try{
				int secondPortId = pendingEnrollmentRequest.getCdapSessionDescriptor().getPortId();
				responseMessage = cdapSessionManager.getCreateObjectResponseMessage(portId, null, requestMessage.getObjClass(), 
						requestMessage.getObjInst(), requestMessage.getObjName(), null, -2, reason, requestMessage.getInvokeID());
				ribDaemon.sendMessage(responseMessage, secondPortId, null);
			}catch(Exception ex){
				log.error(ex);
			}
		 }
	 }
	 
	 /**
		 * Called by the enrollment state machine when the enrollment request has been completed, either successfully or unsuccessfully
		 * @param candidate the IPC process we were trying to enroll to
		 * @param enrollee true if this IPC process is the one that initiated the 
		 * enrollment sequence (i.e. it is the application process that wants to 
		 * join the DIF)
		 */
		public synchronized void enrollmentCompleted(Neighbor dafMember, boolean enrollee){
			//1 Tell the RMT it can start listening for remote IPC processes
			if (enrollee){
				RMT rmt = (RMT) this.getIPCProcess().getIPCProcessComponent(BaseRMT.getComponentName());
				rmt.startListening();
			}
			
			//Reply back to the entity that requested the enrollment
			if (enrollee){			
				ApplicationProcessNamingInfo candidateNamingInfo = new ApplicationProcessNamingInfo(dafMember.getApplicationProcessName(), 
						dafMember.getApplicationProcessInstance(), BaseEnrollmentStateMachine.DEFAULT_ENROLLMENT, null);
				PendingEnrollmentRequest pendingEnrollmentRequest = ongoingInitiateEnrollmentRequests.remove(candidateNamingInfo.getProcessKey());
				if (pendingEnrollmentRequest == null){
					log.error("Did not find a pending enrollment request");
					return;
				}
				
				CDAPMessage requestMessage = pendingEnrollmentRequest.getCdapMessage();
				CDAPMessage responseMessage = null;
				try{
					int portId = pendingEnrollmentRequest.getCdapSessionDescriptor().getPortId();
					responseMessage = cdapSessionManager.getCreateObjectResponseMessage(portId, null, requestMessage.getObjClass(), 
							requestMessage.getObjInst(), requestMessage.getObjName(), null, 0, null, requestMessage.getInvokeID());
					ribDaemon.sendMessage(responseMessage, portId, null);
				}catch(Exception ex){
					log.error(ex);
				}
			}
		}
	
	/**
	 * Sends the CDAP Message and calls the RMT to deallocate the flow identified by portId
	 * @param cdapMessage
	 * @param portId
	 */
	private void sendErrorMessageAndDeallocateFlow(CDAPMessage cdapMessage, int portId){
		try{
			ribDaemon.sendMessage(cdapMessage, portId, null);
			rmt.deallocateFlow(portId);
		}catch(Exception ex){
			log.error(ex.getMessage());
		}
	}
	
	/**
	 * Returns the address manager, the object that manages the allocation and usage 
	 * of addresses within a DIF
	 * @return
	 */
	public AddressManager getAddressManager(){
		if (this.addressManager == null){
			this.addressManager = new SimpleAddressManager(this);
		}
		
		return this.addressManager;
	}
}
