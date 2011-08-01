package rina.enrollment.impl;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.ApplicationProcessNameSynonym;
import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPMessageHandler;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.encoding.api.Encoder;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * Implements the enrollment logics for enrolling with a particular remote IPC process
 * @author eduardgrasa
 *
 */
public class EnrollmentStateMachine implements CDAPMessageHandler{
	
private static final Log log = LogFactory.getLog(EnrollmentStateMachine.class);
	
	private static final long TIME_TO_WAIT_FOR_READ_ADDRESS_RESPONSE = 5*1000;
	private static final long TIME_TO_WAIT_FOR_READ_INITIALIZATION_DATA = 5*1000;
	private static final long TIME_TO_WAIT_FOR_START_RESPONSE = 5*1000;
	
	private enum State {NULL, READ_ADDRESS, INITIALIZE_NEW_MEMBER, INITIALIZE_NEW_MEMBER_SEND_RESPONSE, 
		WAITING_FOR_STARTUP, ENROLLED};
		
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
	
	public EnrollmentStateMachine(RIBDaemon ribDaemon, CDAPSessionManager cdapSessionManager, Encoder encoder, 
			ApplicationProcessNamingInfo remoteNamingInfo){
		this.ribDaemon = ribDaemon;
		this.cdapSessionManager = cdapSessionManager;
		this.encoder = encoder;
		this.remoteNamingInfo = remoteNamingInfo;
		timer = new Timer();
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
					cdapMessage.getSrcAEName(), cdapMessage.getSrcApInst(), cdapMessage.getSrcApName(), cdapMessage.getInvokeID(), 0, null, cdapMessage.getDestAEInst(), 
					cdapMessage.getDestAEName(), cdapMessage.getDestApInst(), cdapMessage.getDestApName(), (int)cdapMessage.getVersion());

			sendCDAPMessage(outgoingCDAPMessage);

			//Read the joining IPC process address
			outgoingCDAPMessage = CDAPMessage.getReadObjectRequestMessage(null, null, 14, 
					"rina.messages.ApplicationProcessNameSynonym", 0, "daf.management.currentSynonym", 0);
			sendCDAPMessage(outgoingCDAPMessage);

			//set timer (max time to wait before getting M_READ_R)
			readAddressResponseTimer = getDisconnectTimerTask();
			timer.schedule(readAddressResponseTimer, TIME_TO_WAIT_FOR_READ_ADDRESS_RESPONSE);
			log.debug("Requesting to read the address of the remote IPC Process");

			this.setState(State.READ_ADDRESS);
		}catch(CDAPException ex){
			log.error(ex);
		}
	}
	
	/**
	 * Called by the EnrollmentTask when it got an M_CONNECT_R message
	 * @param cdapMessage
	 * @param cdapSessionDescriptor
	 */
	public void connectResponse(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor){
		//TODO
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
				ribDaemon.delete(null, RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
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
						"rina.messages.operationalStatus", null, 0, "dif.management.operationalStatus", 0);
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
		default:
			reset();
			break;
		}
	}
	
	private void handleInitializeNewMember(CDAPMessage cdapMessage){
		//Cancel timer
		readInitializationDataTimer.cancel();
		
		if (cdapMessage.getObjName() == null || !cdapMessage.getObjName().equals("daf.management.enrollment")){
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
	 * Called by the EnrollmentTask when it receives an M_CANCELREAD CDAP mesasge 
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
		if (cdapMessage.getObjName() == null || !cdapMessage.getObjName().equals("daf.management.enrollment")){
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
					"rina.messages.operationalStatus", null, 0, "daf.management.operationalStatus", 0);
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
			ribDaemon.create(null, RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
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
	
	/**
	 * Called by the DIFMembersSetObject to initiate the enrollment sequence 
	 * with a remote IPC Process
	 * @param cdapMessage
	 * @param portId
	 */
	public void initiateEnrollment(CDAPMessage cdapMessage, int portId){
		switch(state){
		case NULL:
			break;
		default:
			
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
}
