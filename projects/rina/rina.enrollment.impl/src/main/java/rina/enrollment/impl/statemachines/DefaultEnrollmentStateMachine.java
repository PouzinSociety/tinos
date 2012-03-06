package rina.enrollment.impl.statemachines;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.DAFMember;
import rina.applicationprocess.api.WhatevercastName;
import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPMessageHandler;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.AuthTypes;
import rina.cdap.api.message.CDAPMessage.Flags;
import rina.efcp.api.DataTransferConstants;
import rina.encoding.api.Encoder;
import rina.enrollment.api.EnrollmentTask;
import rina.flowallocator.api.QoSCube;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.IPCException;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * Implements the enrollment logics for enrolling with a particular remote IPC process
 * @author eduardgrasa
 *
 */
public class DefaultEnrollmentStateMachine implements CDAPMessageHandler, EnrollmentStateMachine{

	private static final Log log = LogFactory.getLog(DefaultEnrollmentStateMachine.class);

	public static final String DEFAULT_ENROLLMENT = "default_enrollment";
	public static final String CONNECT_IN_NOT_NULL = "Received a CONNECT message while not in NULL state";
	public static final String READ_RESPONSE_IN_BAD_STATE = "Received a READ response in a wrong state";
	public static final String READ_IN_BAD_STATE = "Received a READ message in a wrong state";
	public static final String CANCEL_READ_IN_BAD_STATE = "Received a CANCEL READ message in a wrong state";
	public static final String UNSUCCESSFUL_REPLY = "Received an unsuccessful response message";
	public static final String WRONG_OBJECT_NAME = "Received a wrong objectName or objectName is null";
	public static final String START_RESPONSE_IN_BAD_STATE = "Received a START response in a wrong state";
	public static final String READ_ADDRESS_RESPONSE_TIMEOUT = "Timeout waiting for read address response";
	public static final String READ_INITIALIZATION_DATA_TIMEOUT = "Timeout waiting for read enrollment data";
	public static final String START_RESPONSE_TIMEOUT = "Timeout waiting for start response";
	public static final String CONNECT_RESPONSE_TIMEOUT = "Timeout waiting for connect response";
	public static final String READ_ADDRESS_TIMEOUT = "Timeout waiting for read address";
	public static final String READ_INITIALIZATION_DATA_RESPONSE_TIMEOUT = "Timeout waiting for read initialization data";
	public static final String START_TIMEOUT = "Timeout waiting for start";

	private State state = State.NULL;

	/**
	 * The RMT to post the return messages
	 */
	private RIBDaemon ribDaemon = null;

	/**
	 * The CDAPSessionManager, to encode/decode cdap messages
	 */
	private CDAPSessionManager cdapSessionManager = null;

	/**
	 * The encoded to encode/decode the object values in CDAP messages
	 */
	private Encoder encoder = null;

	/**
	 * Runnable that will send all the M_READ_R to initialize the remote party in a separate thread
	 */
	private EnrollmentInitializer enrollmentInitializer = null;

	/**
	 * The timer that will execute the different timer tasks of this class
	 */
	private Timer timer = null;

	/**
	 * The timer task used by the timer
	 */
	private TimerTask timerTask = null;

	/**
	 * The portId to use
	 */
	private int portId = 0;

	/**
	 * The naming information of the remote IPC process
	 */
	private ApplicationProcessNamingInfo remoteNamingInfo = null;

	/**
	 * The address of the remote IPC Process being enrolled
	 */
	private DAFMember remotePeer = null;

	/**
	 * The enrollment task
	 */
	private EnrollmentTask enrollmentTask = null;

	/**
	 * True if this IPC process is the one that initiated the 
	 * enrollment sequence (i.e. it is the application process that wants to 
	 * join the DIF)
	 */
	private boolean enrollee = false;

	/**
	 * The maximum time to wait between steps of the enrollment sequence (in ms)
	 */
	private long timeout = 0;
	
	public DefaultEnrollmentStateMachine(RIBDaemon ribDaemon, CDAPSessionManager cdapSessionManager, Encoder encoder, 
			ApplicationProcessNamingInfo remoteNamingInfo, EnrollmentTask enrollmentTask, boolean enrollee, long timeout){
		this.ribDaemon = ribDaemon;
		this.cdapSessionManager = cdapSessionManager;
		this.encoder = encoder;
		this.remoteNamingInfo = remoteNamingInfo;
		this.enrollmentTask = enrollmentTask;
		this.remotePeer = new DAFMember();
		this.enrollee = enrollee;
		this.timeout = timeout;
	}

	/**
	 * Returns true if this IPC process is the one that initiated the 
	 * enrollment sequence (i.e. it is the application process that wants to 
	 * join the DIF)
	 * @return
	 */
	public boolean isEnrollee(){
		return enrollee;
	}

	/**
	 * Return the naming information of the remote peer we are trying to enroll
	 * @return
	 */
	public ApplicationProcessNamingInfo getRemotePeerNamingInfo(){
		return remoteNamingInfo;
	}

	protected synchronized void setState(State state){
		this.state = state;
	}

	protected EnrollmentTask getEnrollmentTask(){
		return this.enrollmentTask;
	}

	public State getState(){
		return this.state;
	}

	protected void setRemoteAddress(long address){
		this.remotePeer.setSynonym(address);
	}

	public long getRemoteAddress(){
		return this.remotePeer.getSynonym();
	}

	public ApplicationProcessNamingInfo getRemoteNamingInfo(){
		return this.remoteNamingInfo;
	}

	protected RIBDaemon getRIBDaemon(){
		return this.ribDaemon;
	}

	public Encoder getEncoder(){
		return this.encoder;
	}

	public int getPortId(){
		return this.portId;
	}

	/**
	 * An M_CONNECT message has been received
	 * @param cdapMessage
	 * @param cdapSessionDescriptor
	 */
	public void connect(CDAPMessage cdapMessage, int portId) {
		switch(state){
		case NULL:
			handleNullState(cdapMessage, portId);
			break;
		default:
			enrollmentTask.enrollmentFailed(this.remoteNamingInfo, portId, CONNECT_IN_NOT_NULL, enrollee, true);
			break;
		}
	}

	private void handleNullState(CDAPMessage cdapMessage, int portId){
		CDAPMessage outgoingCDAPMessage = null;
		this.portId = portId;
		log.debug(portId);

		log.debug("Trying to enroll IPC process "+cdapMessage.getSrcApName()+" "+cdapMessage.getSrcApInst());
		remotePeer.setApplicationProcessName(cdapMessage.getSrcApName());
		remotePeer.setApplicationProcessInstance(cdapMessage.getSrcApInst());

		//TODO authenticate sender
		log.debug("Authentication successfull");

		//Send M_CONNECT_R
		try{
			outgoingCDAPMessage = cdapSessionManager.getOpenConnectionResponseMessage(portId, cdapMessage.getAuthMech(), cdapMessage.getAuthValue(), cdapMessage.getSrcAEInst(), 
					DefaultEnrollmentStateMachine.DEFAULT_ENROLLMENT, cdapMessage.getSrcApInst(), cdapMessage.getSrcApName(), 0, null, cdapMessage.getDestAEInst(), 
					DefaultEnrollmentStateMachine.DEFAULT_ENROLLMENT, cdapMessage.getDestApInst(), cdapMessage.getDestApName(), cdapMessage.getInvokeID());

			sendCDAPMessage(outgoingCDAPMessage);

			//Read the joining IPC process address
			outgoingCDAPMessage = cdapSessionManager.getReadObjectRequestMessage(portId, null, null,
					RIBObjectNames.CURRENT_SYNONYM_RIB_OBJECT_CLASS, 0, RIBObjectNames.CURRENT_SYNONYM_RIB_OBJECT_NAME, 0, true);
			sendCDAPMessage(outgoingCDAPMessage);

			//set timer (max time to wait before getting M_READ_R)
			timer = new Timer();
			timerTask = getEnrollmentFailedTimerTask(READ_ADDRESS_RESPONSE_TIMEOUT);
			timer.schedule(timerTask, timeout);
			log.debug("Requesting to read the address of the remote IPC Process");

			this.setState(State.READ_ADDRESS);
		}catch(CDAPException ex){
			log.error(ex);
		}
	}

	/**
	 * Called by the EnrollmentTask when it got an M_RELEASE message
	 * @param cdapMessage
	 * @param cdapSessionDescriptor
	 */
	public void release(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor){
		log.debug("Releasing the CDAP connection");
		if (!isValidPortId(cdapSessionDescriptor)){
			return;
		}

		if (this.getState().equals(State.ENROLLED)){
			try{
				ribDaemon.delete(null, DAFMember.DAF_MEMBER_SET_RIB_OBJECT_NAME + RIBObjectNames.SEPARATOR + 
						remotePeer.getApplicationProcessName()+remotePeer.getApplicationProcessInstance(), 0);
			}catch(RIBDaemonException ex){
				log.error(ex);
			}
		}

		this.setState(State.NULL);
		this.remotePeer = new DAFMember();

		if (cdapMessage.getInvokeID() != 0){
			try{
				sendCDAPMessage(cdapSessionManager.getReleaseConnectionResponseMessage(portId, null, 0, null, cdapMessage.getInvokeID()));
			}catch(CDAPException ex){
				log.error(ex);
			}
		}
	}

	/**
	 * Called by the EnrollmentTask when it got an M_RELEASE_R message
	 * @param cdapMessage
	 * @param cdapSessionDescriptor
	 */
	public void releaseResponse(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor){
		if (!isValidPortId(cdapSessionDescriptor)){
			return;
		}

		if (!this.getState().equals(State.NULL)){
			this.setState(State.NULL);
		}
	}

	/**
	 * Called by the EnrollmentTask when the flow supporting the CDAP session with the remote peer
	 * has been deallocated
	 * @param cdapSessionDescriptor
	 */
	public void flowDeallocated(CDAPSessionDescriptor cdapSessionDescriptor){
		log.info("The flow supporting the CDAP session identified by "+cdapSessionDescriptor.getPortId()
				+" has been deallocated.");

		if (!isValidPortId(cdapSessionDescriptor)){
			return;
		}

		//Delete the DAF member entry in the RIB
		if (this.getState().equals(State.ENROLLED)){
			try{
				ribDaemon.delete(null, DAFMember.DAF_MEMBER_SET_RIB_OBJECT_NAME + RIBObjectNames.SEPARATOR + 
						remotePeer.getApplicationProcessName()+remotePeer.getApplicationProcessInstance(), 0);
			}catch(RIBDaemonException ex){
				log.error(ex);
			}

			this.setState(State.NULL);
			this.remotePeer = new DAFMember();
		}
		
		//Cancel any timers
		if (timer != null){
			timer.cancel();
		}
	}

	/**
	 * Called by the RIB Daemon when an M_READ_R message has been delivered
	 */
	public void readResponse(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException {
		if (!isValidPortId(cdapSessionDescriptor)){
			return;
		}

		switch (state){
		case READ_ADDRESS:
			handleReadAddressState(cdapMessage);
			break;
		case INITIALIZING_DATA:
			handleInitializingData(cdapMessage);
			break;
		default:
			enrollmentTask.enrollmentFailed(this.remoteNamingInfo, portId, READ_RESPONSE_IN_BAD_STATE, enrollee, true);
			break;
		}
	}

	private void handleReadAddressState(CDAPMessage cdapMessage){
		CDAPMessage outgoingCDAPMessage = null;
		long address = 0;
		boolean allocated = true;
		boolean expired = true;

		log.debug("Process Read Address state called");

		//Cancel the timer
		timerTask.cancel();

		if (cdapMessage.getResult() != 0){
			enrollmentTask.enrollmentFailed(this.remoteNamingInfo, portId, UNSUCCESSFUL_REPLY, enrollee, true);
			return;
		}

		log.debug("Reading the remote IPC process address");

		//Deserialize the address, if not null
		if (cdapMessage.getObjValue() != null){
			address = cdapMessage.getObjValue().getInt64val();
		}

		if (address == 0 || (address  != 0 && allocated && expired)){
			log.debug("Need to assign a new address to the remote IPC process, and initialize it with the DIF data. " +
			"Waiting for the remote IPC process to request the initialization data");
			//Set timer and wait for READ
			timerTask = getEnrollmentFailedTimerTask(READ_INITIALIZATION_DATA_TIMEOUT);
			timer.schedule(timerTask, timeout);
			this.setState(State.INITIALIZE_NEW_MEMBER);
		}

		if (address != 0 && !allocated){
			enrollmentTask.enrollmentFailed(this.remoteNamingInfo, portId, null, enrollee, true);
			return;
		}

		if (address != 0 && allocated && !expired){
			try{
				outgoingCDAPMessage = cdapSessionManager.getStartObjectRequestMessage(portId, null, null, 
						RIBObjectNames.OPERATIONAL_STATUS_RIB_OBJECT_CLASS, null, 0, 
						RIBObjectNames.OPERATIONAL_STATUS_RIB_OBJECT_NAME, 0, true);
				this.setState(State.WAITING_FOR_STARTUP);
				timerTask = getEnrollmentFailedTimerTask(START_RESPONSE_TIMEOUT);
				timer.schedule(timerTask, timeout);
				this.sendCDAPMessage(outgoingCDAPMessage);
			}catch(Exception ex){
				log.error(ex);
			}
		}
	}

	/**
	 * Called by the EnrollmentTask when it receives an M_READ CDAP mesasge 
	 * on certain object names
	 * @param cdapMessage
	 * @param portId
	 */
	public void read(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) {
		if (!isValidPortId(cdapSessionDescriptor)){
			return;
		}

		switch(state){
		case INITIALIZE_NEW_MEMBER:
			handleInitializeNewMember(cdapMessage);
			break;
		case WAITING_READ_ADDRESS:
			handleWaitingReadAddress(cdapMessage);
			break;
		default:
			enrollmentTask.enrollmentFailed(this.remoteNamingInfo, portId, READ_IN_BAD_STATE, enrollee, true);
			break;
		}
	}

	private void handleInitializeNewMember(CDAPMessage cdapMessage){
		//Cancel timer
		timerTask.cancel();

		if (cdapMessage.getObjName() == null || !cdapMessage.getObjName().equals(EnrollmentTask.ENROLLMENT_RIB_OBJECT_NAME)){
			enrollmentTask.enrollmentFailed(this.remoteNamingInfo, portId, WRONG_OBJECT_NAME, enrollee, true);
			return;
		}

		this.setState(State.INITIALIZE_NEW_MEMBER_SEND_RESPONSE);

		log.debug("Replying with the DIF initialization information");

		//Start a new thread that sends as many M_READ_R as required. Has to be a separate thread 
		//so that it can be stopped when the main worker receives an M_CANCELREAD
		enrollmentInitializer = new EnrollmentInitializer(this, cdapMessage.getInvokeID(), portId, cdapSessionManager);
		enrollmentTask.getIPCProcess().execute(enrollmentInitializer);
	}

	/**
	 * Called by the EnrollmentTask when it receives an M_CANCELREAD CDAP message 
	 * on certain object names
	 * @param cdapMessage
	 * @param portId
	 */
	public void cancelread(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) {
		if (!isValidPortId(cdapSessionDescriptor)){
			return;
		}

		switch(state){
		case INITIALIZE_NEW_MEMBER_SEND_RESPONSE:
			handleInitializeNewMemberCancelread(cdapMessage);
			break;
		default:
			enrollmentTask.enrollmentFailed(this.remoteNamingInfo, portId, CANCEL_READ_IN_BAD_STATE, enrollee, true);
			break;
		}
	}

	private void handleInitializeNewMemberCancelread(CDAPMessage cdapMessage){
		if (cdapMessage.getObjName() == null || !cdapMessage.getObjName().equals(EnrollmentTask.ENROLLMENT_RIB_OBJECT_NAME)){
			enrollmentTask.enrollmentFailed(this.remoteNamingInfo, portId, WRONG_OBJECT_NAME, enrollee, true);
			return;
		}

		//Stop the thread that is sending M_READ_R (if still running).
		if (enrollmentInitializer.isRunning()){
			enrollmentInitializer.cancelread();
		}

		//send the M_CANCELREAD response
		try{
			CDAPMessage outgoingCDAPMessage = CDAPMessage.getCancelReadResponseMessage(null, cdapMessage.getInvokeID(), 0, null);
			sendCDAPMessage(outgoingCDAPMessage);
		}catch(Exception ex){
			log.error(ex);
		}

		enrollmentDataInitializationComplete();
	}

	/**
	 * Called when the Enrollment initializer has finished sending all the initialization data
	 * or the enrolling IPC Process had told it to stop (M_CANCELREAD received)
	 */
	protected void enrollmentDataInitializationComplete(){
		enrollmentInitializer = null;

		try{
			CDAPMessage outgoingCDAPMessage = cdapSessionManager.getStartObjectRequestMessage(portId, null, null, 
					RIBObjectNames.OPERATIONAL_STATUS_RIB_OBJECT_NAME, null, 0, 
					RIBObjectNames.OPERATIONAL_STATUS_RIB_OBJECT_NAME, 0, true);
			this.sendCDAPMessage(outgoingCDAPMessage);

			//start timer
			timerTask = getEnrollmentFailedTimerTask(START_RESPONSE_TIMEOUT);
			timer.schedule(timerTask, timeout);

			this.setState(State.WAITING_FOR_STARTUP);
		}catch(Exception ex){
			log.error(ex);
		}
	}

	/**
	 * Called by the RIB Daemon when it receives an M_START_R message
	 */
	public void startResponse(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor)	throws RIBDaemonException {
		switch(state){
		case WAITING_FOR_STARTUP:
			handleStartup(cdapMessage, cdapSessionDescriptor);
			break;
		default:
			enrollmentTask.enrollmentFailed(this.remoteNamingInfo, portId, START_RESPONSE_IN_BAD_STATE, enrollee, true);
			break;
		}
	}

	private void handleStartup(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor){
		//Cancel timer
		timerTask.cancel();
		timer.cancel();

		if (cdapMessage.getResult() != 0){
			this.setState(State.NULL);
			enrollmentTask.enrollmentFailed(this.remoteNamingInfo, portId, cdapMessage.getResultReason(), enrollee, true);
		}else{
			this.setState(State.ENROLLED);
			enrollmentTask.enrollmentCompleted(remotePeer, enrollee);
			log.info("Remote IPC Process enrolled!");
		}
	}

	/**
	 * Called by the DIFMembersSetObject to initiate the enrollment sequence 
	 * with a remote IPC Process
	 * @param cdapMessage
	 * @param portId
	 */
	public void initiateEnrollment(DAFMember candidate, int portId) throws IPCException{
		remoteNamingInfo = new ApplicationProcessNamingInfo(candidate.getApplicationProcessName(), candidate.getApplicationProcessInstance());
		remotePeer = candidate;
		switch(state){
		case NULL:
			try{
				ApplicationProcessNamingInfo apNamingInfo = (ApplicationProcessNamingInfo) this.getRIBDaemon().getIPCProcess().getApplicationProcessNamingInfo();
				CDAPMessage requestMessage = cdapSessionManager.getOpenConnectionRequestMessage(portId, AuthTypes.AUTH_NONE, null, null, DefaultEnrollmentStateMachine.DEFAULT_ENROLLMENT, 
						candidate.getApplicationProcessInstance(), candidate.getApplicationProcessName(), null, DefaultEnrollmentStateMachine.DEFAULT_ENROLLMENT, 
						apNamingInfo.getApplicationProcessInstance(), apNamingInfo.getApplicationProcessName());
				ribDaemon.sendMessage(requestMessage, portId, null);
				this.portId = portId;

				//Set timer
				timer = new Timer();
				timerTask = getEnrollmentFailedTimerTask(CONNECT_RESPONSE_TIMEOUT);
				timer.schedule(timerTask, timeout);

				//Update state
				this.state = State.WAITING_CONNECTION;
			}catch(Exception ex){
				ex.printStackTrace();
				log.error(ex);
			}
			break;
		default:
			throw new IPCException(IPCException.ENROLLMENT_PROBLEM_CODE, 
					IPCException.ENROLLMENT_PROBLEM + "Enrollment state machine not in NULL state");
		}
	}

	/**
	 * Called by the EnrollmentTask when it got an M_CONNECT_R message
	 * @param cdapMessage
	 * @param cdapSessionDescriptor
	 */
	public void connectResponse(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor){
		switch(state){
		case WAITING_CONNECTION:
			handleConnectResponse(cdapMessage);
			break;
		default:
			this.state = State.NULL;;
			enrollmentTask.enrollmentFailed(remoteNamingInfo, portId, "Message received in wrong order", enrollee, true);
			break;
		}
	}

	private void handleConnectResponse(CDAPMessage cdapMessage){
		timerTask.cancel();

		if (cdapMessage.getResult() != 0){
			this.state = State.NULL;
			enrollmentTask.enrollmentFailed(remoteNamingInfo, portId, cdapMessage.getResultReason(), enrollee, true);
			return;
		}

		//Set timer
		timerTask = getEnrollmentFailedTimerTask(READ_ADDRESS_TIMEOUT);
		timer.schedule(timerTask, timeout);

		//Update state
		state = State.WAITING_READ_ADDRESS;
	}

	private void handleWaitingReadAddress(CDAPMessage cdapMessage){
		//Cancel timer
		timerTask.cancel();

		if (!cdapMessage.getObjName().equals(RIBObjectNames.CURRENT_SYNONYM_RIB_OBJECT_NAME)){
			this.state = State.NULL;
			enrollmentTask.enrollmentFailed(remoteNamingInfo, portId, WRONG_OBJECT_NAME, enrollee, true);
			return;
		}

		try{
			CDAPMessage outgoingCDAPMessage = cdapSessionManager.getReadObjectResponseMessage(portId, null,
					"synonym", 0, RIBObjectNames.CURRENT_SYNONYM_RIB_OBJECT_NAME, null, 0, null, 
					cdapMessage.getInvokeID());

			sendCDAPMessage(outgoingCDAPMessage);

			outgoingCDAPMessage = cdapSessionManager.getReadObjectRequestMessage(portId, null, null, 
					EnrollmentTask.ENROLLMENT_RIB_OBJECT_CLASS, 0, EnrollmentTask.ENROLLMENT_RIB_OBJECT_NAME, 0, true);

			//Set timer
			timerTask = getEnrollmentFailedTimerTask(READ_INITIALIZATION_DATA_RESPONSE_TIMEOUT);
			timer.schedule(timerTask, timeout);

			//Update status
			sendCDAPMessage(outgoingCDAPMessage);
			state = State.INITIALIZING_DATA;
		}catch(CDAPException ex){
			log.error(ex);
			//If there is an exception fix the code, it should never happen.
		}
	}

	private void handleInitializingData(CDAPMessage cdapMessage){
		//Cancel timer
		timerTask.cancel();

		if (cdapMessage.getResult() != 0){
			this.state = State.NULL;
			enrollmentTask.enrollmentFailed(remoteNamingInfo, portId, UNSUCCESSFUL_REPLY, enrollee, true);
			return;
		}

		if (cdapMessage.getObjName().equals(RIBObjectNames.CURRENT_SYNONYM_RIB_OBJECT_NAME)){
			try{
				long synonym = cdapMessage.getObjValue().getInt64val();
				ribDaemon.write(cdapMessage.getObjClass(), cdapMessage.getObjName(), cdapMessage.getObjInst(), new Long(synonym));
			}catch(Exception ex){
				log.error(ex);
			}
		}else if (cdapMessage.getObjName().startsWith(WhatevercastName.WHATEVERCAST_NAME_SET_RIB_OBJECT_NAME)){
			try{
				WhatevercastName name = (WhatevercastName) encoder.decode(
						cdapMessage.getObjValue().getByteval(), WhatevercastName.class);
				ribDaemon.create(cdapMessage.getObjClass(), cdapMessage.getObjName(), cdapMessage.getObjInst(), name);
			}catch(Exception ex){
				log.error(ex);
			}
		}else if (cdapMessage.getObjName().equals(DataTransferConstants.DATA_TRANSFER_CONSTANTS_RIB_OBJECT_NAME)){
			try{
				DataTransferConstants constants = (DataTransferConstants) encoder.decode(
						cdapMessage.getObjValue().getByteval(), DataTransferConstants.class);
				ribDaemon.write(cdapMessage.getObjClass(), cdapMessage.getObjName(), cdapMessage.getObjInst(), constants);
			}catch(Exception ex){
				log.error(ex);
			}
		}else if (cdapMessage.getObjName().startsWith(QoSCube.QOSCUBE_SET_RIB_OBJECT_NAME)){
			try{
				QoSCube cube = (QoSCube) encoder.decode(
						cdapMessage.getObjValue().getByteval(), QoSCube.class);
				ribDaemon.create(cdapMessage.getObjClass(), cdapMessage.getObjName(), cdapMessage.getObjInst(), cube);
			}catch(Exception ex){
				log.error(ex);
			}
		}else if (cdapMessage.getObjName().startsWith(DAFMember.DAF_MEMBER_SET_RIB_OBJECT_NAME)){
			try{
				DAFMember dafMember = (DAFMember) encoder.decode(cdapMessage.getObjValue().getByteval(), DAFMember.class);
				if (remotePeer.getApplicationProcessName().equals(dafMember.getApplicationProcessName()) && 
						(remotePeer.getApplicationProcessInstance() == null || remotePeer.getApplicationProcessInstance().equals(dafMember.getApplicationProcessInstance()))){
					//This is the DAFMember object representing the remote peer I'm enrolling with
					remotePeer.setSynonym(dafMember.getSynonym());
				}else{
					//TODO create DAFMember objects in the RIB for the other DAF Members
				}
			}catch(Exception ex){
				log.error(ex);
			}
		}

		if (cdapMessage.getFlags() != null && cdapMessage.getFlags().equals(Flags.F_RD_INCOMPLETE)){
			//Set timer, more read response messages to come
			timerTask = getEnrollmentFailedTimerTask(READ_INITIALIZATION_DATA_RESPONSE_TIMEOUT);
			timer.schedule(timerTask, timeout);
		}else{
			//Set timer, an START request should be coming
			timerTask = getEnrollmentFailedTimerTask(START_TIMEOUT);
			timer.schedule(timerTask, timeout);

			//Update status
			state = State.WAITING_FOR_STARTUP;
		}
	}

	public void start(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor){
		//Cancel timer
		timerTask.cancel();
		timer.cancel();

		if (!cdapMessage.getObjName().equals(RIBObjectNames.OPERATIONAL_STATUS_RIB_OBJECT_NAME)){
			this.state = State.NULL;
			enrollmentTask.enrollmentFailed(remoteNamingInfo, portId, WRONG_OBJECT_NAME, enrollee, true);
			return;
		}

		try{
			CDAPMessage outgoingCDAPMessage = cdapSessionManager.getStartObjectResponseMessage(portId, null, 0, null, cdapMessage.getInvokeID());
			sendCDAPMessage(outgoingCDAPMessage);
			log.info("IPC Process enrolled!");
			state = State.ENROLLED;
			enrollmentTask.enrollmentCompleted(remotePeer, enrollee);
		}catch(CDAPException ex){
			log.error(ex);
		}
	}

	private boolean isValidPortId(CDAPSessionDescriptor cdapSessionDescriptor){
		if (cdapSessionDescriptor.getPortId() != this.getPortId()){
			log.error("Received a CDAP Message from port id "+cdapSessionDescriptor.getPortId()+". Was expecting messages from port id "+this.getPortId());
			return false;
		}

		return true;
	}

	/**
	 * Send a CDAP message using the RMT
	 * @param cdapMessage
	 * @param portId
	 */
	protected synchronized void sendCDAPMessage(CDAPMessage cdapMessage){
		try{
			ribDaemon.sendMessage(cdapMessage, portId, this);
		}catch(Exception ex){
			ex.printStackTrace();
			log.error("Could not send CDAP message: "+ex.getMessage());
			if (ex.getMessage().equals("Flow closed")){
				cdapSessionManager.removeCDAPSession(portId);
			}
		}
	}

	/**
	 * Returns a timer task that will disconnect the CDAP session when 
	 * the timer task runs.
	 * @return
	 */
	private TimerTask getEnrollmentFailedTimerTask(String reason){
		final String message = reason;
		return new TimerTask(){
			@Override
			public void run() {
				try{
					enrollmentTask.enrollmentFailed(remoteNamingInfo, portId, message, enrollee, true);
					setState(State.NULL);
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}};
	}

	public void cancelReadResponse(CDAPMessage arg0, CDAPSessionDescriptor arg1)
	throws RIBDaemonException {
		// TODO Auto-generated method stub

	}

	public void createResponse(CDAPMessage arg0, CDAPSessionDescriptor arg1)
	throws RIBDaemonException {
		// TODO Auto-generated method stub

	}

	public void deleteResponse(CDAPMessage arg0, CDAPSessionDescriptor arg1)
	throws RIBDaemonException {
		// TODO Auto-generated method stub

	}

	public void stopResponse(CDAPMessage arg0, CDAPSessionDescriptor arg1)
	throws RIBDaemonException {
		// TODO Auto-generated method stub

	}

	public void writeResponse(CDAPMessage arg0, CDAPSessionDescriptor arg1)
	throws RIBDaemonException {
		// TODO Auto-generated method stub

	}
}
