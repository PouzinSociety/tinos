package rina.enrollment.impl.statemachines;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.ApplicationProcessNameSynonym;
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
import rina.enrollment.impl.EnrollmentTaskImpl;
import rina.flowallocator.api.QoSCube;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
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
	
	private static final long TIME_TO_WAIT_FOR_READ_ADDRESS_RESPONSE = 5*1000;
	private static final long TIME_TO_WAIT_FOR_READ_INITIALIZATION_DATA = 5*1000;
	private static final long TIME_TO_WAIT_FOR_START_RESPONSE = 5*1000;
		
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
	 * The thread pool implementation
	 */
	private ExecutorService executorService = null;
	
	private Timer timer = null;
	
	/**
	 * The timer to wait for read address response
	 */
	private TimerTask readAddressResponseTimer = null;
	
	/**
	 * The timer to wait for a read initialization data request
	 */
	private TimerTask readInitializationDataTimer = null;
	
	/**
	 * The timer to wait for a start response
	 */
	private TimerTask startResponseTimer = null;
	
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
	private ApplicationProcessNameSynonym remoteAddress = null;
	
	/**
	 * The enrollment task
	 */
	private EnrollmentTaskImpl enrollmentTask = null;
	
	public DefaultEnrollmentStateMachine(RIBDaemon ribDaemon, CDAPSessionManager cdapSessionManager, Encoder encoder, 
			ApplicationProcessNamingInfo remoteNamingInfo, EnrollmentTaskImpl enrollmentTask){
		this.ribDaemon = ribDaemon;
		this.cdapSessionManager = cdapSessionManager;
		this.encoder = encoder;
		this.remoteNamingInfo = remoteNamingInfo;
		this.enrollmentTask = enrollmentTask;
		this.executorService = Executors.newFixedThreadPool(2);
	}
	
	protected synchronized void setState(State state){
		this.state = state;
	}
	
	public State getState(){
		return this.state;
	}
	
	protected void setRemoteAddress(ApplicationProcessNameSynonym address){
		this.remoteAddress = address;
	}
	
	public ApplicationProcessNameSynonym getRemoteAddress(){
		return this.remoteAddress;
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
			reset();
			break;
		}
	}
	
	private void handleNullState(CDAPMessage cdapMessage, int portId){
		CDAPMessage outgoingCDAPMessage = null;
		this.portId = portId;
		log.debug(portId);
		
		log.debug("Trying to enroll IPC process "+cdapMessage.getSrcApName()+" "+cdapMessage.getSrcApInst());

		//TODO authenticate sender
		log.debug("Authentication successfull");

		//Send M_CONNECT_R
		try{
			outgoingCDAPMessage = CDAPMessage.getOpenConnectionResponseMessage(cdapMessage.getAuthMech(), cdapMessage.getAuthValue(), cdapMessage.getSrcAEInst(), 
					DefaultEnrollmentStateMachine.DEFAULT_ENROLLMENT, cdapMessage.getSrcApInst(), cdapMessage.getSrcApName(), cdapMessage.getInvokeID(), 0, null, cdapMessage.getDestAEInst(), 
					DefaultEnrollmentStateMachine.DEFAULT_ENROLLMENT, cdapMessage.getDestApInst(), cdapMessage.getDestApName());

			sendCDAPMessage(outgoingCDAPMessage);

			//Read the joining IPC process address
			outgoingCDAPMessage = CDAPMessage.getReadObjectRequestMessage(null, null, 14, 
					"rina.messages.ApplicationProcessNameSynonym", 0, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.CURRENT_SYNONYM, 0);
			sendCDAPMessage(outgoingCDAPMessage);

			//set timer (max time to wait before getting M_READ_R)
			readAddressResponseTimer = getDisconnectTimerTask();
			timer = new Timer();
			timer.schedule(readAddressResponseTimer, TIME_TO_WAIT_FOR_READ_ADDRESS_RESPONSE);
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
				ribDaemon.delete(null, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
						RIBObjectNames.SEPARATOR + RIBObjectNames.ENROLLMENT + RIBObjectNames.SEPARATOR + RIBObjectNames.MEMBERS
						+ RIBObjectNames.SEPARATOR + this.remoteAddress.getApplicationProcessName()+this.remoteAddress.getApplicationProcessInstance(), 
						0, null);
			}catch(RIBDaemonException ex){
				log.error(ex);
			}
		}
		
		this.setState(State.NULL);
		this.remoteAddress = null;
		
		if (cdapMessage.getInvokeID() != 0){
			try{
				sendCDAPMessage(CDAPMessage.getReleaseConnectionResponseMessage(null, cdapMessage.getInvokeID(), 0, null));
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
			reset();
			break;
		}
	}
	
	private void handleReadAddressState(CDAPMessage cdapMessage){
		CDAPMessage outgoingCDAPMessage = null;
		byte[] serializedAddress = null;
		ApplicationProcessNameSynonym address = null;
		boolean allocated = true;
		boolean expired = true;
		
		log.debug("Process Read Address state called");
		
		//Cancel the timer
		readAddressResponseTimer.cancel();
		
		if (cdapMessage.getResult() != 0){
			reset();
			return;
		}
		
		log.debug("Reading the remote IPC process address");
		
		//Deserialize the address, if not null
		if (cdapMessage.getObjValue() != null){
			serializedAddress = cdapMessage.getObjValue().getByteval();
			if (serializedAddress != null){
				try {
					address = (ApplicationProcessNameSynonym) encoder.decode(serializedAddress, ApplicationProcessNameSynonym.class.getName());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		if (address == null || (address  != null && allocated && expired)){
			log.debug("Need to assign a new address to the remote IPC process, and initialize it with the DIF data. " +
					"Waiting for the remote IPC process to request the initialization data");
			//Set timer and wait for READ
			readInitializationDataTimer = getDisconnectTimerTask();
			timer.schedule(readInitializationDataTimer, TIME_TO_WAIT_FOR_READ_INITIALIZATION_DATA);
			this.setState(State.INITIALIZE_NEW_MEMBER);
		}
		
		if (address != null && !allocated){
			reset();
			return;
		}
		
		if (address != null && allocated && !expired){
			try{
				outgoingCDAPMessage = CDAPMessage.getStartObjectRequestMessage(null, null, 25, 
						"rina.messages.operationalStatus", null, 0, "/dif/management/operationalStatus", 0);
				this.setState(State.WAITING_FOR_STARTUP);
				startResponseTimer = getDisconnectTimerTask();
				timer.schedule(startResponseTimer, TIME_TO_WAIT_FOR_START_RESPONSE);
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
			reset();
			break;
		}
	}
	
	private void handleInitializeNewMember(CDAPMessage cdapMessage){
		//Cancel timer
		readInitializationDataTimer.cancel();
		
		if (cdapMessage.getObjName() == null || !cdapMessage.getObjName().equals(RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + 
				RIBObjectNames.MANAGEMENT + RIBObjectNames.SEPARATOR + RIBObjectNames.ENROLLMENT)){
			reset();
			return;
		}
		
		this.setState(State.INITIALIZE_NEW_MEMBER_SEND_RESPONSE);
		
		log.debug("Replying with the DIF initialization information");
		
		//Start a new thread that sends as many M_READ_R as required. Has to be a separate thread 
		//so that it can be stopped when the main worker receives an M_CANCELREAD
		enrollmentInitializer = new EnrollmentInitializer(this, cdapMessage.getInvokeID(), portId);
		executorService.execute(enrollmentInitializer);
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
			reset();
			break;
		}
	}
	
	private void handleInitializeNewMemberCancelread(CDAPMessage cdapMessage){
		if (cdapMessage.getObjName() == null || !cdapMessage.getObjName().equals(RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + 
				RIBObjectNames.MANAGEMENT + RIBObjectNames.SEPARATOR + RIBObjectNames.ENROLLMENT)){
			reset();
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
			CDAPMessage outgoingCDAPMessage = CDAPMessage.getStartObjectRequestMessage(null, null, 25, 
					"rina.messages.operationalStatus", null, 0, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.OPERATIONAL_STATUS, 0);
			this.sendCDAPMessage(outgoingCDAPMessage);
			
			//start timer
			startResponseTimer = getDisconnectTimerTask();
			timer.schedule(startResponseTimer, TIME_TO_WAIT_FOR_START_RESPONSE);
			
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
			handleStartup(cdapMessage);
			break;
		default:
			reset();
			break;
		}
	}

	private void handleStartup(CDAPMessage cdapMessage){
		//Cancel timer
		startResponseTimer.cancel();
		timer.cancel();

		try{
			ribDaemon.create(null, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.ENROLLMENT + RIBObjectNames.SEPARATOR + RIBObjectNames.MEMBERS
					+ RIBObjectNames.SEPARATOR + this.remoteAddress.getApplicationProcessName()+this.remoteAddress.getApplicationProcessInstance(), 
					0, this.remoteAddress);
		}catch(RIBDaemonException ex){
			log.error(ex);
		}

		this.setState(State.ENROLLED);
		log.info("Remote IPC Process enrolled!");
	}

	/**
	 * Go back to the NULL state and send an M_RELEASE message
	 */
	
	
	/**
	 * Called by the DIFMembersSetObject to initiate the enrollment sequence 
	 * with a remote IPC Process
	 * @param cdapMessage
	 * @param portId
	 */
	public void initiateEnrollment(ApplicationProcessNameSynonym candidate, int portId){
		remoteNamingInfo = new ApplicationProcessNamingInfo(candidate.getApplicationProcessName(), candidate.getApplicationProcessInstance(), null, null);
		remoteAddress = candidate;
		switch(state){
		case NULL:
			try{
				ApplicationProcessNamingInfo apNamingInfo = (ApplicationProcessNamingInfo) this.getRIBDaemon().read(null, 
						RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
						RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.APNAME, 0);
				CDAPMessage requestMessage = CDAPMessage.getOpenConnectionRequestMessage(AuthTypes.AUTH_NONE, null, null, DefaultEnrollmentStateMachine.DEFAULT_ENROLLMENT, 
						candidate.getApplicationProcessInstance(), candidate.getApplicationProcessName(), 1, null, DefaultEnrollmentStateMachine.DEFAULT_ENROLLMENT, 
						apNamingInfo.getApplicationProcessInstance(), apNamingInfo.getApplicationProcessName());
				ribDaemon.sendMessage(requestMessage, portId, null);
				this.portId = portId;
				//todo set Timer
				this.state = State.WAITING_CONNECTION;
			}catch(Exception ex){
				ex.printStackTrace();
				log.error(ex);
			}
			break;
		default:
			enrollmentTask.enrollmentCompleted(candidate, 1, "Enrollment state machine was not in NULL state");
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
			this.state = State.NULL;
			enrollmentTask.enrollmentCompleted(remoteAddress, 1, "Message received in wrong order");
			break;
		}
	}
	
	private void handleConnectResponse(CDAPMessage cdapMessage){
		//TODO cancel timer
		
		if (cdapMessage.getResult() != 0){
			this.state = State.NULL;
			enrollmentTask.enrollmentCompleted(remoteAddress, cdapMessage.getResult(), cdapMessage.getResultReason());
		}
		
		//TODO set timer
		
		state = State.WAITING_READ_ADDRESS;
	}
	
	private void handleWaitingReadAddress(CDAPMessage cdapMessage){
		//TODO cancel timer
		
		if (!cdapMessage.getObjName().equals(RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.CURRENT_SYNONYM)){
			reset();
			enrollmentTask.enrollmentCompleted(remoteAddress, 1, "Wrong objectName");
		}
		
		try{
			CDAPMessage outgoingCDAPMessage = CDAPMessage.getReadObjectResponseMessage(null, cdapMessage.getInvokeID(), 
					"rina.messages.ApplicationProcessNameSynonym", 0, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.CURRENT_SYNONYM, null, 0, null);

			sendCDAPMessage(outgoingCDAPMessage);

			outgoingCDAPMessage = CDAPMessage.getReadObjectRequestMessage(null, null, 49, 
					"rina.messages.DIFEnrollmentInformation", 0, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.ENROLLMENT, 0);

			//TODO set timer

			sendCDAPMessage(outgoingCDAPMessage);
			state = State.INITIALIZING_DATA;
		}catch(CDAPException ex){
			log.error(ex);
			//If there is an exception fix the code, it should never happen.
		}
	}
	
	private void handleInitializingData(CDAPMessage cdapMessage){
		//TODO cancel timer
		
		if (cdapMessage.getResult() != 0){
			reset();
			enrollmentTask.enrollmentCompleted(remoteAddress, cdapMessage.getResult(), cdapMessage.getResultReason());
		}
		
		if (cdapMessage.getObjName().equals(RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.CURRENT_SYNONYM)){
			try{
				ApplicationProcessNameSynonym synonym = (ApplicationProcessNameSynonym) encoder.decode(
						cdapMessage.getObjValue().getByteval(), ApplicationProcessNameSynonym.class.toString());
				ribDaemon.write(cdapMessage.getObjClass(), cdapMessage.getObjName(), cdapMessage.getObjInst(), synonym);
			}catch(Exception ex){
				log.error(ex);
			}
		}else if (cdapMessage.getObjName().startsWith(RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.WHATEVERCAST_NAMES)){
			try{
				WhatevercastName name = (WhatevercastName) encoder.decode(
						cdapMessage.getObjValue().getByteval(), WhatevercastName.class.toString());
				ribDaemon.create(cdapMessage.getObjClass(), cdapMessage.getObjName(), cdapMessage.getObjInst(), name);
			}catch(Exception ex){
				log.error(ex);
			}
		}else if (cdapMessage.getObjName().equals(RIBObjectNames.SEPARATOR + RIBObjectNames.DIF + RIBObjectNames.SEPARATOR + RIBObjectNames.IPC + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.DATA_TRANSFER+ RIBObjectNames.SEPARATOR + RIBObjectNames.CONSTANTS)){
			try{
				DataTransferConstants constants = (DataTransferConstants) encoder.decode(
						cdapMessage.getObjValue().getByteval(), DataTransferConstants.class.toString());
				ribDaemon.write(cdapMessage.getObjClass(), cdapMessage.getObjName(), cdapMessage.getObjInst(), constants);
			}catch(Exception ex){
				log.error(ex);
			}
		}else if (cdapMessage.getObjName().startsWith(RIBObjectNames.SEPARATOR + RIBObjectNames.DIF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.FLOW_ALLOCATOR + RIBObjectNames.SEPARATOR + RIBObjectNames.QOS_CUBES)){
			try{
				QoSCube cube = (QoSCube) encoder.decode(
						cdapMessage.getObjValue().getByteval(), QoSCube.class.toString());
				ribDaemon.create(cdapMessage.getObjClass(), cdapMessage.getObjName(), cdapMessage.getObjInst(), cube);
			}catch(Exception ex){
				log.error(ex);
			}
		}
		//TODO add QoS cubes info
		
		if (cdapMessage.getFlags() != null && cdapMessage.getFlags().equals(Flags.F_RD_INCOMPLETE)){
			//TODO set timer, more read response messages to come
		}else{
			//TODO set timer, an START request should be coming
			state = State.WAITING_FOR_STARTUP;
		}
	}
	
	public void start(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor){
		//TODO cancel timer
		
		if (!cdapMessage.getObjName().equals(RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.OPERATIONAL_STATUS)){
			reset();
			enrollmentTask.enrollmentCompleted(remoteAddress, 1, "Wrong objectname");
		}
		
		try{
			CDAPMessage outgoingCDAPMessage = CDAPMessage.getStartObjectResponseMessage(null, cdapMessage.getInvokeID(), 0, null);
			sendCDAPMessage(outgoingCDAPMessage);
			log.info("IPC Process enrolled!");
			state = State.ENROLLED;
			enrollmentTask.enrollmentCompleted(remoteAddress, 0, null);
		}catch(CDAPException ex){
			log.error(ex);
		}
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
	
	private void reset(){
		log.error("Received a failed response message or a message in the wrong orther");
		try{
			sendCDAPMessage(CDAPMessage.getReleaseConnectionRequestMessage(null, 0));
		}catch(CDAPException ex){
			log.error(ex);
		}
		this.setState(State.NULL);
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
	private TimerTask getDisconnectTimerTask(){
		return new TimerTask(){
			@Override
			public void run() {
				try{
					CDAPMessage cdapMessage = CDAPMessage.getReleaseConnectionRequestMessage(null, 0);
					sendCDAPMessage(cdapMessage);
					setState(State.NULL);
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}};
	}
}
