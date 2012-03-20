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
	
	/**
	 * Stores the initialization information received by the joining IPC Process
	 */
	private EnrollmentInitializationInformation enrollmentInformation = null;
	
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
		
		if (this.enrollmentInformation == null){
			enrollmentInformation = new EnrollmentInitializationInformation();
		}

		if (cdapMessage.getObjName().equals(RIBObjectNames.CURRENT_SYNONYM_RIB_OBJECT_NAME)){
			try{
				long synonym = cdapMessage.getObjValue().getInt64val();
				enrollmentInformation.setSynonym(new Long(synonym));
			}catch(Exception ex){
				log.error(ex);
			}
		}else if (cdapMessage.getObjName().equals(WhatevercastName.WHATEVERCAST_NAME_SET_RIB_OBJECT_NAME)){
			try{
				WhatevercastName[] namesArray = (WhatevercastName[]) encoder.decode(
						cdapMessage.getObjValue().getByteval(), WhatevercastName[].class);
				for(int i=0; i<namesArray.length; i++){
					enrollmentInformation.addWhatevercastName(namesArray[i]);
				}
			}catch(Exception ex){
				log.error(ex);
			}
		}else if (cdapMessage.getObjName().startsWith(WhatevercastName.WHATEVERCAST_NAME_SET_RIB_OBJECT_NAME)){
			try{
				WhatevercastName name = (WhatevercastName) encoder.decode(
						cdapMessage.getObjValue().getByteval(), WhatevercastName.class);
				enrollmentInformation.addWhatevercastName(name);
			}catch(Exception ex){
				log.error(ex);
			}
		}else if (cdapMessage.getObjName().equals(DataTransferConstants.DATA_TRANSFER_CONSTANTS_RIB_OBJECT_NAME)){
			try{
				DataTransferConstants constants = (DataTransferConstants) encoder.decode(
						cdapMessage.getObjValue().getByteval(), DataTransferConstants.class);
				enrollmentInformation.setDataTransferConstants(constants);
			}catch(Exception ex){
				log.error(ex);
			}
		}else if (cdapMessage.getObjName().equals(QoSCube.QOSCUBE_SET_RIB_OBJECT_NAME)){
			try{
				QoSCube[] cubesArray = (QoSCube[]) encoder.decode(
						cdapMessage.getObjValue().getByteval(), QoSCube[].class);
				for(int i=0; i<cubesArray.length; i++){
					enrollmentInformation.addQoSCube(cubesArray[i]);
				}
			}catch(Exception ex){
				log.error(ex);
			}
		}else if (cdapMessage.getObjName().startsWith(QoSCube.QOSCUBE_SET_RIB_OBJECT_NAME)){
			try{
				QoSCube cube = (QoSCube) encoder.decode(
						cdapMessage.getObjValue().getByteval(), QoSCube.class);
				enrollmentInformation.addQoSCube(cube);
			}catch(Exception ex){
				log.error(ex);
			}
		}else if(cdapMessage.getObjName().equals(DAFMember.DAF_MEMBER_SET_RIB_OBJECT_NAME)){
			try{
				DAFMember[] dafMembersArray = (DAFMember[]) encoder.decode(
						cdapMessage.getObjValue().getByteval(), DAFMember[].class);
				for(int i=0; i<dafMembersArray.length; i++){
					if (isRemotePeer(dafMembersArray[i])){
						remotePeer.setSynonym(dafMembersArray[i].getSynonym());
					}else{
						enrollmentInformation.addDAFMember(dafMembersArray[i]);
					}
				}
			}catch(Exception ex){
				log.error(ex);
			}
		}else if (cdapMessage.getObjName().startsWith(DAFMember.DAF_MEMBER_SET_RIB_OBJECT_NAME)){
			try{
				DAFMember dafMember = (DAFMember) encoder.decode(cdapMessage.getObjValue().getByteval(), DAFMember.class);
				if (isRemotePeer(dafMember)){
					remotePeer.setSynonym(dafMember.getSynonym());
				}else{
					enrollmentInformation.addDAFMember(dafMember);
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
	
	private boolean isRemotePeer(DAFMember dafMember){
		if (remotePeer.getApplicationProcessName().equals(dafMember.getApplicationProcessName()) && 
				(remotePeer.getApplicationProcessInstance() == null || 
						remotePeer.getApplicationProcessInstance().equals(dafMember.getApplicationProcessInstance()))){
			return true;
		}
		
		return false;
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
		
		//Verify Enrollment Information
		try{
			verifyEnrollment();
		}catch(Exception ex){
			ex.printStackTrace();
			//TODO send M_START_R indicating error
		}

		//Commit enrollment
		try{
			commitEnrollment();
		}catch(Exception ex){
			ex.printStackTrace();
			//TODO send M_START_R indicating error
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
	
	/**
	 * Verify
	 */
	private void verifyEnrollment(){
		//TODO implement this
	}
	
	/**
	 * Create the objects in the RIB
	 * @throws RIBDaemonException
	 */
	private void commitEnrollment() throws RIBDaemonException{
		//Synonym (address)
		ribDaemon.write(RIBObjectNames.CURRENT_SYNONYM_RIB_OBJECT_CLASS, RIBObjectNames.CURRENT_SYNONYM_RIB_OBJECT_NAME, 
				0, enrollmentInformation.getSynonym());
		
		//Data transfer constants
		ribDaemon.write(DataTransferConstants.DATA_TRANSFER_CONSTANTS_RIB_OBJECT_CLASS, 
				DataTransferConstants.DATA_TRANSFER_CONSTANTS_RIB_OBJECT_NAME, 0, enrollmentInformation.getDataTransferConstants());
		
		//Whatevercast names
		WhatevercastName whatevercastName = null;
		for(int i=0; i<enrollmentInformation.getWhatevercastNames().size(); i++){
			whatevercastName = enrollmentInformation.getWhatevercastNames().get(i);
			ribDaemon.create(WhatevercastName.WHATEVERCAST_NAME_RIB_OBJECT_CLASS, 
					WhatevercastName.WHATEVERCAST_NAME_SET_RIB_OBJECT_NAME + RIBObjectNames.SEPARATOR + whatevercastName.getName(), 
					0, enrollmentInformation.getWhatevercastNames().get(i));
		}
		
		//QoS Cubes
		QoSCube qosCube = null;
		for(int i=0; i<enrollmentInformation.getQosCubes().size(); i++){
			qosCube = enrollmentInformation.getQosCubes().get(i);
			ribDaemon.create(QoSCube.QOSCUBE_RIB_OBJECT_CLASS, QoSCube.QOSCUBE_SET_RIB_OBJECT_NAME + 
					RIBObjectNames.SEPARATOR + qosCube.getQosId(), 0, qosCube);
		}

		//Nearest neighbour we're enrolled to
		ribDaemon.create(null, DAFMember.DAF_MEMBER_SET_RIB_OBJECT_NAME + RIBObjectNames.SEPARATOR + 
				remotePeer.getApplicationProcessName()+remotePeer.getApplicationProcessInstance(), 
				0, remotePeer);
	}

	

	
}
