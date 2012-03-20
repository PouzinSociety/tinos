package rina.enrollment.impl.statemachines;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.DAFMember;
import rina.cdap.api.BaseCDAPMessageHandler;
import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.encoding.api.Encoder;
import rina.enrollment.api.EnrollmentTask;
import rina.enrollment.impl.statemachines.EnrollmentStateMachine.State;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * The base class that contains the common aspects of both 
 * enrollment state machines: the enroller side and the enrolle side
 * @author eduardgrasa
 *
 */
public abstract class BaseEnrollmentStateMachine extends BaseCDAPMessageHandler{
	
	private static final Log log = LogFactory.getLog(BaseEnrollmentStateMachine.class);
	
	public static final String CONNECT_IN_NOT_NULL = "Received a CONNECT message while not in NULL state";
	public static final String CREATE_IN_BAD_STATE = "Received a CREATE message in a wrong state";
	public static final String START_ENROLLMENT_TIMEOUT = "Timeout waiting for start enrollment request";
	public static final String START_IN_BAD_STATE = "Received a START message in a wrong state";
	public static final String START_RESPONSE_IN_BAD_STATE = "Received a START response in a wrong state";
	public static final String START_RESPONSE_TIMEOUT = "Timeout waiting for start response";
	public static final String STOP_ENROLLMENT_RESPONSE_TIMEOUT = "Timeout waiting for stop enrolment response";
	public static final String STOP_IN_BAD_STATE = "Received a STOP message in a wrong state";
	public static final String STOP_RESPONSE_IN_BAD_STATE = "Received a STOP response in a wrong state";
	public static final String STOP_ENROLLMENT_TIMEOUT = "Timeout waiting for stop enrollment request";
	public static final String UNEXPECTED_ERROR = "Unexpected error. ";
	
	public static final String DEFAULT_ENROLLMENT = "default_enrollment";
	
	public static final String READ_RESPONSE_IN_BAD_STATE = "Received a READ response in a wrong state";
	public static final String READ_IN_BAD_STATE = "Received a READ message in a wrong state";
	public static final String CANCEL_READ_IN_BAD_STATE = "Received a CANCEL READ message in a wrong state";
	public static final String UNSUCCESSFUL_REPLY = "Received an unsuccessful response message";
	public static final String WRONG_OBJECT_NAME = "Received a wrong objectName or objectName is null";
	
	public static final String READ_INITIALIZATION_DATA_TIMEOUT = "Timeout waiting for read enrollment data";
	
	public static final String CONNECT_RESPONSE_TIMEOUT = "Timeout waiting for connect response";
	public static final String READ_ADDRESS_TIMEOUT = "Timeout waiting for read address";
	public static final String READ_INITIALIZATION_DATA_RESPONSE_TIMEOUT = "Timeout waiting for read initialization data";
	public static final String START_TIMEOUT = "Timeout waiting for start";
	
	/**
	 * All the possible states of all the enroller state machines
	 */
	public enum State {NULL, WAIT_CONNECT_R, WAIT_START_ENROLLMENT_R, PUSHING_INFORMATION, 
		PULLING_INFORMATION, AWAIT_START, ENROLLED, WAIT_START_ENROLLMENT, 
		WAIT_STOP_ENROLLMENT_RESPONSE};
		
	/**
	 * the current state
	 */
	protected State state = State.NULL;

	/**
	 * The RMT to post the return messages
	 */
	protected RIBDaemon ribDaemon = null;

	/**
	 * The CDAPSessionManager, to encode/decode cdap messages
	 */
	protected CDAPSessionManager cdapSessionManager = null;

	/**
	 * The encoded to encode/decode the object values in CDAP messages
	 */
	protected Encoder encoder = null;
	
	/**
	 * The timer that will execute the different timer tasks of this class
	 */
	protected Timer timer = null;

	/**
	 * The timer task used by the timer
	 */
	protected TimerTask timerTask = null;

	/**
	 * The portId to use
	 */
	protected int portId = 0;
	
	/**
	 * The enrollment task
	 */
	protected EnrollmentTask enrollmentTask = null;
	
	/**
	 * The maximum time to wait between steps of the enrollment sequence (in ms)
	 */
	protected long timeout = 0;
	
	/**
	 * The information of the remote IPC Process being enrolled
	 */
	protected DAFMember remotePeer = null;
	
	/**
	 * The naming information of the remote IPC process
	 */
	protected ApplicationProcessNamingInfo remoteNamingInfo = null;
	
	public State getState(){
		return this.state;
	}
	
	protected synchronized void setState(State state){
		this.state = state;
	}
	
	public int getPortId(){
		return this.portId;
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
	protected TimerTask getEnrollmentFailedTimerTask(String reason, boolean enrollee){
		final String message = reason;
		final boolean isEnrollee = enrollee;
		return new TimerTask(){
			@Override
			public void run() {
				try{
					enrollmentTask.enrollmentFailed(remoteNamingInfo, portId, message, isEnrollee, true);
					setState(State.NULL);
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}};
	}
	
	protected boolean isValidPortId(CDAPSessionDescriptor cdapSessionDescriptor){
		if (cdapSessionDescriptor.getPortId() != this.getPortId()){
			log.error("Received a CDAP Message from port id "+
					cdapSessionDescriptor.getPortId()+". Was expecting messages from port id "+this.getPortId());
			return false;
		}

		return true;
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
				//TODO see what we have to do here
				/*
				ribDaemon.delete(null, DAFMember.DAF_MEMBER_SET_RIB_OBJECT_NAME + RIBObjectNames.SEPARATOR + 
						remotePeer.getApplicationProcessName()+remotePeer.getApplicationProcessInstance(), 0, null);
						*/
			}catch(Exception ex){
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
			//TODO see what we need to do here
			/*try{
				ribDaemon.delete(null, DAFMember.DAF_MEMBER_SET_RIB_OBJECT_NAME + RIBObjectNames.SEPARATOR + 
						remotePeer.getApplicationProcessName()+remotePeer.getApplicationProcessInstance(), 0);
			}catch(RIBDaemonException ex){
				log.error(ex);
			}*/

			this.setState(State.NULL);
			this.remotePeer = new DAFMember();
		}
		
		//Cancel any timers
		if (timer != null){
			timer.cancel();
		}
	}
}
