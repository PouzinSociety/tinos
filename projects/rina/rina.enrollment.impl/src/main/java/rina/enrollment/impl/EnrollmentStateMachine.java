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
import rina.cdap.api.message.CDAPMessage.Opcode;
import rina.encoding.api.Encoder;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ribdaemon.api.RIBDaemon;

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
	
	public EnrollmentStateMachine(RIBDaemon ribDaemon, CDAPSessionManager cdapSessionManager, Encoder encoder, ApplicationProcessNamingInfo remoteNamingInfo){
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
	 * Called by the RIB Daemon when a response message for me has arrived.
	 * @param cdapMessage
	 * @param cdapSessionDescriptor
	 */
	public void processMessage(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) {
		if (cdapSessionDescriptor.getPortId() != this.getPortId()){
			log.error("Received a CDAP Message from port id "+cdapSessionDescriptor.getPortId()+". Was expecting messages from port id "+this.getPortId());
			return;
		}
		
		processCDAPMessage(cdapMessage, cdapSessionDescriptor.getPortId());
	}
	
	/**
	 * Process the CDAP message
	 * @param cdapMessage
	 */
	public void processCDAPMessage(CDAPMessage cdapMessage, int portId){
		CDAPMessage outgoingCDAPMessage = null;
		this.portId = portId;
		
		log.debug("Current state: "+state);

		try{
			switch(state){
			case NULL:
				outgoingCDAPMessage = processNullState(cdapMessage);
				break;
			case READ_ADDRESS:
				outgoingCDAPMessage = processReadAddressState(cdapMessage);
				break;
			case INITIALIZE_NEW_MEMBER:
				outgoingCDAPMessage = processInitializeNewMemberState(cdapMessage);
				break;
			case INITIALIZE_NEW_MEMBER_SEND_RESPONSE:
				outgoingCDAPMessage = processInitializeNewMemberAndSendResponseState(cdapMessage);
				break;
			case WAITING_FOR_STARTUP:
				outgoingCDAPMessage = processWaitingForStartupState(cdapMessage);
				break;
			case ENROLLED:
				outgoingCDAPMessage = processEnrolledState(cdapMessage);
				break;
			default:
				break;
			}
		}catch(CDAPException ex){
			ex.printStackTrace();
			log.error(ex.getMessage());
		}

		if (outgoingCDAPMessage != null){
			sendCDAPMessage(outgoingCDAPMessage);
		}
	}
	
	private CDAPMessage processNullState(CDAPMessage cdapMessage) throws CDAPException{
		if (!cdapMessage.getOpCode().equals(Opcode.M_CONNECT)){
			return CDAPMessage.getReleaseConnectionRequestMessage(null, 0);
		}
		
		log.debug("Trying to enroll IPC process "+cdapMessage.getSrcApName()+" "+cdapMessage.getSrcApInst());
		
		//TODO authenticate sender
		
		log.debug("Authentication successfull");
		
		//Send M_CONNECT_R
		CDAPMessage outgoingCDAPMessage = CDAPMessage.getOpenConnectionResponseMessage(cdapMessage.getAuthMech(), cdapMessage.getAuthValue(), cdapMessage.getSrcAEInst(), 
				cdapMessage.getSrcAEName(), cdapMessage.getSrcApInst(), cdapMessage.getSrcApName(), cdapMessage.getInvokeID(), 0, null, cdapMessage.getDestAEInst(), 
				cdapMessage.getDestAEName(), cdapMessage.getDestApInst(), cdapMessage.getDestApName(), (int)cdapMessage.getVersion());
		
		sendCDAPMessage(outgoingCDAPMessage);
		
		//Read the joining IPC process address
		outgoingCDAPMessage = CDAPMessage.getReadObjectRequestMessage(null, null, 14, 
				"rina.messages.ApplicationProcessNameSynonym", 0, "daf.management.currentSynonym", 0);
		
		//set timer (max time to wait before getting M_READ_R)
		readAddressResponseTimer = getDisconnectTimerTask();
		timer.schedule(readAddressResponseTimer, TIME_TO_WAIT_FOR_READ_ADDRESS_RESPONSE);
		
		log.debug("Requesting to read the address of the remote IPC Process");
			
		this.setState(State.READ_ADDRESS);
		return outgoingCDAPMessage;
	}
	
	private CDAPMessage processReadAddressState(CDAPMessage cdapMessage) throws CDAPException{
		CDAPMessage outgoingCDAPMessage = null;
		byte[] serializedAddress = null;
		ApplicationProcessNameSynonym address = null;
		boolean allocated = true;
		boolean expired = true;
		
		log.debug("Process Read Address state called");
		
		//Cancel the timer
		readAddressResponseTimer.cancel();
		
		if (cdapMessage.getOpCode().equals(Opcode.M_RELEASE)){
			this.setState(State.NULL);
			return null;
		}
		
		if (!cdapMessage.getOpCode().equals(Opcode.M_READ_R) || cdapMessage.getResult() != 0){
			outgoingCDAPMessage = CDAPMessage.getReleaseConnectionRequestMessage(null, 0);
			this.setState(State.NULL);
			return outgoingCDAPMessage;
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
			return null;
		}
		
		if (address != null && !allocated){
			outgoingCDAPMessage = CDAPMessage.getReleaseConnectionRequestMessage(null, 0);
			this.setState(State.NULL);
			return outgoingCDAPMessage;
		}
		
		if (address != null && allocated && !expired){
			outgoingCDAPMessage = CDAPMessage.getStartObjectRequestMessage(null, null, 25, 
					"rina.messages.operationalStatus", null, 0, "dif.management.operationalStatus", 0);
			this.setState(State.WAITING_FOR_STARTUP);
			startResponseTimer = getDisconnectTimerTask();
			timer.schedule(startResponseTimer, TIME_TO_WAIT_FOR_START_RESPONSE);
			return outgoingCDAPMessage;
		}
		
		return null;
	}
	
	private CDAPMessage processInitializeNewMemberState(CDAPMessage cdapMessage) throws CDAPException{
		//Cancel timer
		readInitializationDataTimer.cancel();
		
		if (!cdapMessage.getOpCode().equals(Opcode.M_READ) || cdapMessage.getObjName() == null || 
				!cdapMessage.getObjName().equals("daf.management.enrollment")){
			return CDAPMessage.getReleaseConnectionRequestMessage(null, 0);
		}
		
		this.setState(State.INITIALIZE_NEW_MEMBER_SEND_RESPONSE);
		
		log.debug("Replying with the DIF initialization information");
		
		//Start a new thread that sends as many M_READ_R as required. Has to be a separate thread 
		//so that it can be stopped when the main worker receives an M_CANCELREAD
		enrollmentInitializer = new EnrollmentInitializer(this, cdapMessage.getInvokeID(), portId);
		executorService.execute(enrollmentInitializer);
		
		return null;
	}
	
	private CDAPMessage processInitializeNewMemberAndSendResponseState(CDAPMessage cdapMessage) throws CDAPException{
		CDAPMessage outgoingCDAPMessage = null;
		
		if (cdapMessage != null){
			if (!cdapMessage.getOpCode().equals(Opcode.M_CANCELREAD) || cdapMessage.getObjName() == null || 
					!cdapMessage.getObjName().equals("daf.management.enrollment")){
				return CDAPMessage.getReleaseConnectionRequestMessage(null, 0);
			}

			//Stop the thread that is sending M_READ_R (if still running).
			if (enrollmentInitializer.isRunning()){
				enrollmentInitializer.cancelread();
			}

			//send the M_CANCELREAD response
			outgoingCDAPMessage = CDAPMessage.getCancelReadResponseMessage(null, cdapMessage.getInvokeID(), 0, null);
			sendCDAPMessage(outgoingCDAPMessage);
		}
		
		enrollmentInitializer = null;
		
		//start timer
		startResponseTimer = getDisconnectTimerTask();
		timer.schedule(startResponseTimer, TIME_TO_WAIT_FOR_START_RESPONSE);
		
		outgoingCDAPMessage = CDAPMessage.getStartObjectRequestMessage(null, null, 25, 
				"rina.messages.operationalStatus", null, 0, "daf.management.operationalStatus", 0);
		this.setState(State.WAITING_FOR_STARTUP);
			
		return outgoingCDAPMessage;
	}
	
	private CDAPMessage processWaitingForStartupState(CDAPMessage cdapMessage) throws CDAPException{
		//Cancel timer
		startResponseTimer.cancel();
		timer.cancel();
		
		if (!cdapMessage.getOpCode().equals(Opcode.M_START_R)){
			return CDAPMessage.getReleaseConnectionRequestMessage(null, 0);
		}
		
		this.setState(State.ENROLLED);
		log.info("Remote IPC Process enrolled!");
		return null;
	}
	
	private CDAPMessage processEnrolledState(CDAPMessage cdapMessage) throws CDAPException{
		if (cdapMessage.getOpCode().equals(Opcode.M_RELEASE)){
			log.info("Remote IPC process disconnected");
			this.setState(State.NULL);
			if (cdapMessage.getInvokeID() != 0){
				return CDAPMessage.getReleaseConnectionResponseMessage(null, cdapMessage.getInvokeID(), 0, null);
			}
		}

		return null;
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
