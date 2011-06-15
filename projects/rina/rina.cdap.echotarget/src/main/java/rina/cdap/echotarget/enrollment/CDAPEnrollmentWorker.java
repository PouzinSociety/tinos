package rina.cdap.echotarget.enrollment;

import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.ApplicationProcessNameSynonym;
import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.Opcode;
import rina.cdap.echotarget.CDAPWorker;
import rina.delimiting.api.Delimiter;
import rina.encoding.api.Encoder;

public class CDAPEnrollmentWorker extends CDAPWorker {

	private static final Log log = LogFactory.getLog(CDAPEnrollmentWorker.class);
	
	private static final long TIME_TO_WAIT_FOR_READ_ADDRESS_RESPONSE = 5*1000;
	private static final long TIME_TO_WAIT_FOR_READ_INITIALIZATION_DATA = 5*1000;
	private static final long TIME_TO_WAIT_FOR_START_RESPONSE = 5*1000;
	
	private enum State {NULL, READ_ADDRESS, INITIALIZE_NEW_MEMBER, INITIALIZE_NEW_MEMBER_SEND_RESPONSE, 
		WAITING_FOR_STARTUP, ENROLLED};
		
	private State state = State.NULL;
	
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

	public CDAPEnrollmentWorker(Socket socket, CDAPSessionManager cdapSessionManager, Delimiter delimiter, Encoder encoder) {
		super(socket, cdapSessionManager, delimiter, encoder);
		this.executorService = Executors.newFixedThreadPool(2);
		timer = new Timer();
	}
	
	protected synchronized void setState(State state){
		this.state = state;
	}
	
	protected State getState(){
		return this.state;
	}
	
	public Encoder getEncoder(){
		return this.encoder;
	}

	@Override
	protected void processCDAPMessage(byte[] serializedCDAPMessage) {
		CDAPMessage incomingCDAPMessage = null;
		CDAPMessage outgoingCDAPMessage = null;

		try {
			if (serializedCDAPMessage != null){
				incomingCDAPMessage = cdapMessageReceived(serializedCDAPMessage);
			}
			
			switch(state){
			case NULL:
				outgoingCDAPMessage = processNullState(incomingCDAPMessage);
				break;
			case READ_ADDRESS:
				outgoingCDAPMessage = processReadAddressState(incomingCDAPMessage);
				break;
			case INITIALIZE_NEW_MEMBER:
				outgoingCDAPMessage = processInitializeNewMemberState(incomingCDAPMessage);
				break;
			case INITIALIZE_NEW_MEMBER_SEND_RESPONSE:
				outgoingCDAPMessage = processInitializeNewMemberAndSendResponseState(incomingCDAPMessage);
				break;
			case WAITING_FOR_STARTUP:
				outgoingCDAPMessage = processWaitingForStartupState(incomingCDAPMessage);
				break;
			case ENROLLED:
				outgoingCDAPMessage = processEnrolledState(incomingCDAPMessage);
				break;
			default:
				break;
			}
			
			if (outgoingCDAPMessage != null){
				sendCDAPMessage(outgoingCDAPMessage);
			}
		}catch(CDAPException ex){
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}catch(IOException ex){
			ex.printStackTrace();
			end = true;
		}
	}
	
	private CDAPMessage processNullState(CDAPMessage cdapMessage) throws CDAPException, IOException{
		if (!cdapMessage.getOpCode().equals(Opcode.M_CONNECT)){
			end = true;
			return CDAPMessage.getReleaseConnectionRequestMessage(null, 0);
		}
		
		//TODO authenticate sender
		
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
			
		this.setState(State.READ_ADDRESS);
		return outgoingCDAPMessage;
	}
	
	private CDAPMessage processReadAddressState(CDAPMessage cdapMessage) throws CDAPException{
		CDAPMessage outgoingCDAPMessage = null;
		byte[] serializedAddress = null;
		ApplicationProcessNameSynonym address = null;
		boolean allocated = true;
		boolean expired = true;
		
		//Cancel the timer
		readAddressResponseTimer.cancel();
		timer.purge();
		
		if (cdapMessage.getOpCode().equals(Opcode.M_RELEASE)){
			this.setState(State.NULL);
			end = true;
			return null;
		}
		
		if (!cdapMessage.getOpCode().equals(Opcode.M_READ_R) || cdapMessage.getResult() != 0){
			outgoingCDAPMessage = CDAPMessage.getReleaseConnectionRequestMessage(null, 0);
			this.setState(State.NULL);
			end = true;
			return outgoingCDAPMessage;
		}
		
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
			//Set timer and wait for READ
			readInitializationDataTimer = getDisconnectTimerTask();
			timer.schedule(readInitializationDataTimer, TIME_TO_WAIT_FOR_READ_INITIALIZATION_DATA);
			this.setState(State.INITIALIZE_NEW_MEMBER);
			return null;
		}
		
		if (address != null && !allocated){
			outgoingCDAPMessage = CDAPMessage.getReleaseConnectionRequestMessage(null, 0);
			this.setState(State.NULL);
			end = true;
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
		timer.purge();
		
		if (!cdapMessage.getOpCode().equals(Opcode.M_READ) || cdapMessage.getObjName() == null || 
				!cdapMessage.getObjName().equals("daf.management.enrollment")){
			end = true;
			return CDAPMessage.getReleaseConnectionRequestMessage(null, 0);
		}
		
		this.setState(State.INITIALIZE_NEW_MEMBER_SEND_RESPONSE);
		
		//Start a new thread that sends as many M_READ_R as required. Has to be a separate thread 
		//so that it can be stopped when the main worker receives an M_CANCELREAD
		enrollmentInitializer = new EnrollmentInitializer(this, cdapMessage.getInvokeID());
		executorService.execute(enrollmentInitializer);
		
		return null;
	}
	
	private CDAPMessage processInitializeNewMemberAndSendResponseState(CDAPMessage cdapMessage) throws CDAPException, IOException{
		CDAPMessage outgoingCDAPMessage = null;
		
		if (cdapMessage != null){
			if (!cdapMessage.getOpCode().equals(Opcode.M_CANCELREAD) || cdapMessage.getObjName() == null || 
					!cdapMessage.getObjName().equals("daf.management.enrollment")){
				end = true;
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
				"rina.messages.operationalStatus", null, 0, "dif.management.operationalStatus", 0);
		this.setState(State.WAITING_FOR_STARTUP);
			
		return outgoingCDAPMessage;
	}
	
	private CDAPMessage processWaitingForStartupState(CDAPMessage cdapMessage) throws CDAPException{
		//Cancel timer
		startResponseTimer.cancel();
		timer.purge();
		
		if (!cdapMessage.getOpCode().equals(Opcode.M_START_R) || cdapMessage.getObjName() == null || 
				!cdapMessage.getObjName().equals("dif.management.operationalStatus")){
			end = true;
			return CDAPMessage.getReleaseConnectionRequestMessage(null, 0);
		}
		
		this.setState(State.ENROLLED);
		return null;
	}
	
	private CDAPMessage processEnrolledState(CDAPMessage cdapMessage) throws CDAPException{
		if (cdapMessage.getOpCode().equals(Opcode.M_RELEASE) && cdapMessage.getInvokeID() != 0){
			return CDAPMessage.getReleaseConnectionResponseMessage(null, cdapMessage.getInvokeID(), 0, null);
		}
		
		return null;
	}
	
	private CDAPMessage cdapMessageReceived(byte[] serializedCDAPMessage) throws CDAPException{
		log.info("Processing serialized CDAP message. This is the serialized message: ");
		log.info(printBytes(serializedCDAPMessage));
		CDAPMessage incomingCDAPMessage = cdapSessionManager.messageReceived(serializedCDAPMessage, socket.getLocalPort());
		log.info("Received CDAP message: "+incomingCDAPMessage.toString());
		
		return incomingCDAPMessage;
	}
	
	protected synchronized void sendCDAPMessage(CDAPMessage cdapMessage) throws CDAPException, IOException{
		byte[] serializedCDAPMessageToBeSend = null;
		byte[] delimitedSdu = null;
		
		serializedCDAPMessageToBeSend = cdapSessionManager.encodeNextMessageToBeSent(cdapMessage, socket.getLocalPort());
		delimitedSdu = delimiter.getDelimitedSdu(serializedCDAPMessageToBeSend);
		socket.getOutputStream().write(delimitedSdu);
		cdapSessionManager.messageSent(cdapMessage, socket.getLocalPort());
		log.info("Sent CDAP Message: "+ cdapMessage.toString());
		log.info("Sent SDU:" + printBytes(delimitedSdu));
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
					end = true;;
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}};
	}
}
