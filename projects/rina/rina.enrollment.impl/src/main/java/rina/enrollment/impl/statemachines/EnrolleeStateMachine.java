package rina.enrollment.impl.statemachines;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.AuthTypes;
import rina.cdap.api.message.ObjectValue;
import rina.efcp.api.DataTransferConstants;
import rina.encoding.api.Encoder;
import rina.enrollment.api.EnrollmentInformationRequest;
import rina.enrollment.api.EnrollmentTask;
import rina.enrollment.api.Neighbor;
import rina.flowallocator.api.QoSCube;
import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.IPCException;
import rina.ipcservice.api.IPCService;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * The state machine of the party that wants to 
 * become a new member of the DIF.
 * @author eduardgrasa
 *
 */
public class EnrolleeStateMachine extends BaseEnrollmentStateMachine{

	private static final Log log = LogFactory.getLog(EnrolleeStateMachine.class);
	
	/**
	 * True if early start is allowed by the enrolling IPC Process
	 */
	private boolean allowedToStart = false;
	
	private CDAPMessage stopEnrollmentRequestMessage = null;
	
	public EnrolleeStateMachine(RIBDaemon ribDaemon, CDAPSessionManager cdapSessionManager, Encoder encoder, 
			ApplicationProcessNamingInfo remoteNamingInfo, EnrollmentTask enrollmentTask, long timeout){
		super(ribDaemon, cdapSessionManager, encoder, remoteNamingInfo, enrollmentTask, timeout);
	}
	
	
	/**
	 * Called by the DIFMembersSetObject to initiate the enrollment sequence 
	 * with a remote IPC Process
	 * @param cdapMessage
	 * @param portId
	 */
	public synchronized void initiateEnrollment(Neighbor candidate, int portId) throws IPCException{
		remoteNamingInfo = new ApplicationProcessNamingInfo(candidate.getApplicationProcessName(), candidate.getApplicationProcessInstance());
		remotePeer = candidate;
		switch(state){
		case NULL:
			try{
				ApplicationProcessNamingInfo apNamingInfo = (ApplicationProcessNamingInfo) ribDaemon.getIPCProcess().getApplicationProcessNamingInfo();
				CDAPMessage requestMessage = cdapSessionManager.getOpenConnectionRequestMessage(portId, AuthTypes.AUTH_NONE, null, null, IPCService.MANAGEMENT_AE, 
						candidate.getApplicationProcessInstance(), candidate.getApplicationProcessName(), null, IPCService.MANAGEMENT_AE, 
						apNamingInfo.getApplicationProcessInstance(), apNamingInfo.getApplicationProcessName());
				ribDaemon.sendMessage(requestMessage, portId, null);
				this.portId = portId;

				//Set timer
				timerTask = getEnrollmentFailedTimerTask(CONNECT_RESPONSE_TIMEOUT, true);
				timer.schedule(timerTask, timeout);

				//Update state
				this.state = State.WAIT_CONNECT_RESPONSE;
			}catch(Exception ex){
				ex.printStackTrace();
				this.abortEnrollment(remoteNamingInfo, portId, ex.getMessage(), true, false);
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
	public synchronized void connectResponse(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor){
		switch(state){
		case WAIT_CONNECT_RESPONSE:
			handleConnectResponse(cdapMessage);
			break;
		default:
			this.abortEnrollment(remoteNamingInfo, portId, "Message received in wrong order", true, true);
			break;
		}
	}
	
	private void handleConnectResponse(CDAPMessage cdapMessage){
		timerTask.cancel();

		if (cdapMessage.getResult() != 0){
			this.state = State.NULL;
			enrollmentTask.enrollmentFailed(remoteNamingInfo, portId, cdapMessage.getResultReason(), true, true);
			return;
		}
		
		remotePeer.setApplicationProcessInstance(cdapMessage.getSrcApInst());
		remoteNamingInfo.setApplicationProcessInstance(cdapMessage.getSrcApInst());

		//Send M_START with EnrollmentInformation object
		try{
			EnrollmentInformationRequest eiRequest = new EnrollmentInformationRequest();
			Long address = ribDaemon.getIPCProcess().getAddress();
			if (address != null){
				eiRequest.setAddress(ribDaemon.getIPCProcess().getAddress());
			}
			ObjectValue objectValue = new ObjectValue();
			objectValue.setByteval(encoder.encode(eiRequest));
			CDAPMessage startMessage = cdapSessionManager.getStartObjectRequestMessage(portId, null, null, 
					EnrollmentInformationRequest.ENROLLMENT_INFO_OBJECT_CLASS, objectValue, 0, 
					EnrollmentInformationRequest.ENROLLMENT_INFO_OBJECT_NAME, 0, true);
			sendCDAPMessage(startMessage);

			//Set timer
			timerTask = getEnrollmentFailedTimerTask(START_RESPONSE_TIMEOUT, true);
			timer.schedule(timerTask, timeout);

			//Update state
			state = State.WAIT_START_ENROLLMENT_RESPONSE;
		}catch(Exception ex){
			ex.printStackTrace();
			//TODO what to do?
		}
	}
	
	/**
	 * Received the Start Response
	 */
	@Override
	public void startResponse(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor)
			throws RIBDaemonException {
		if (!isValidPortId(cdapSessionDescriptor)){
			return;
		}
		
		synchronized(this){
			switch(state){
			case WAIT_START_ENROLLMENT_RESPONSE:
				handleStartEnrollmentResponse(cdapMessage);
				break;
			default:
				this.abortEnrollment(this.remoteNamingInfo, portId, START_RESPONSE_IN_BAD_STATE, true, true);
				break;
			}
		}
	}
	
	/**
	 * Received Start enrollment response.
	 * @param cdapMessage
	 */
	private void handleStartEnrollmentResponse(CDAPMessage cdapMessage){
		//Cancel timer
		timerTask.cancel();
		
		if (cdapMessage.getResult() != 0){
			this.setState(State.NULL);
			enrollmentTask.enrollmentFailed(this.remoteNamingInfo, portId, cdapMessage.getResultReason(), true, true);
			return;
		}
		
		//Update address
		if (cdapMessage.getObjValue() != null){
			try{
				long address = ((EnrollmentInformationRequest) encoder.decode(
						cdapMessage.getObjValue().getByteval(), EnrollmentInformationRequest.class)).getAddress();
				ribDaemon.write(RIBObjectNames.ADDRESS_RIB_OBJECT_CLASS, RIBObjectNames.ADDRESS_RIB_OBJECT_NAME, 
						new Long(address), null);
			}catch(Exception ex){
				ex.printStackTrace();
				this.abortEnrollment(this.remoteNamingInfo, portId, UNEXPECTED_ERROR + ex.getMessage(), true, true);
			}
		}
		
		//Set timer
		timerTask = getEnrollmentFailedTimerTask(STOP_ENROLLMENT_TIMEOUT, true);
		timer.schedule(timerTask, timeout);
		
		//Update state
		state = State.WAIT_STOP_ENROLLMENT_RESPONSE;
	}
	
	/**
	 * A stop request was received
	 * @param cdapMessage
	 * @param cdapSessionDescriptor
	 */
	public void stop(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor){
		if (!isValidPortId(cdapSessionDescriptor)){
			return;
		}
		
		synchronized(this){
			switch(state){
			case WAIT_STOP_ENROLLMENT_RESPONSE:
				handleStopEnrollment(cdapMessage);
				break;
			default:
				this.abortEnrollment(this.remoteNamingInfo, portId, STOP_IN_BAD_STATE, true, true);
				break;
			}
		}
	}
	
	/**
	 * Stop enrollment request received. Check if I have enough information, if not 
	 * ask for more with M_READs.
	 * Have to check if I can start operating (if not wait 
	 * until M_START operationStatus). If I can start and have enough information, 
	 * create or update all the objects received during the enrollment phase.
	 * @param cdapMessage
	 */
	private void handleStopEnrollment(CDAPMessage cdapMessage){
		//Cancel timer
		timerTask.cancel();
		
		//Check if I'm allowed to start early
		if (cdapMessage.getObjValue() == null){
			this.abortEnrollment(this.remoteNamingInfo, portId, STOP_WITH_NO_OBJECT_VALUE, true, true);
			return;
		}
		allowedToStart = cdapMessage.getObjValue().isBooleanval();
		stopEnrollmentRequestMessage = cdapMessage;
		
		//Request more information or start
		try{
			requestMoreInformationOrStart();
		}catch(Exception ex){
			log.error(ex);
			this.abortEnrollment(this.remoteNamingInfo, portId, UNEXPECTED_ERROR + ex.getMessage(), true, true);
		}
	}
	
	/**
	 * See if more information is required for enrollment, or if we can 
	 * start or if we have to wait for the start message
	 * @throws Exception
	 */
	private void requestMoreInformationOrStart() throws Exception{
		CDAPMessage stopResponseMessage = null;
		
		//Check if more information is required
		CDAPMessage readMessage = nextObjectRequired();
		
		if (readMessage != null){
			//Request information
			sendCDAPMessage(readMessage);
			
			//Set timer
			timerTask = getEnrollmentFailedTimerTask(READ_RESPONSE_TIMEOUT, true);
			timer.schedule(timerTask, timeout);
			
			//Update state
			state = State.WAIT_READ_RESPONSE;
			return;
		}
		
		//No more information is required, if I'm allowed to start early, 
		//commit the enrollment information, set operational status to true
		//and send M_STOP_R. If not, just send M_STOP_R
		if (allowedToStart){
			try{
				commitEnrollment();
				stopResponseMessage = cdapSessionManager.getStopObjectResponseMessage(portId, null, 0, 
						null, stopEnrollmentRequestMessage.getInvokeID());
				sendCDAPMessage(stopResponseMessage);
				
				enrollmentCompleted(true);
			}catch(RIBDaemonException ex){
				log.error(ex);
				stopResponseMessage = cdapSessionManager.getStopObjectResponseMessage(portId, null, 3, 
						PROBLEMS_COMITING_ENROLLMENT_INFO, stopEnrollmentRequestMessage.getInvokeID());
				sendCDAPMessage(stopResponseMessage);
				this.abortEnrollment(this.remoteNamingInfo, portId, PROBLEMS_COMITING_ENROLLMENT_INFO, true, true);
			}
			
			return;
		}
		
		stopResponseMessage = cdapSessionManager.getStopObjectResponseMessage(portId, null, 0, 
					null, stopEnrollmentRequestMessage.getInvokeID());
		sendCDAPMessage(stopResponseMessage);
		timerTask = getEnrollmentFailedTimerTask(START_TIMEOUT, true);
		timer.schedule(timerTask, timeout);
		this.setState(State.WAIT_START);
	}
	
	/**
	 * Checks if more information is required for enrollment
	 * (At least there must be DataTransferConstants, a QoS cube and a DAF Member). If there is, 
	 * it returns a CDAP READ message requesting the next object to be read. If not, it returns null
	 * @return A CDAP READ message requesting the next object to be read. If not, it returns null
	 */
	private CDAPMessage nextObjectRequired() throws Exception{
		CDAPMessage cdapMessage = null;
		
		if (ribDaemon.getIPCProcess().getDataTransferConstants() == null){
			cdapMessage = cdapSessionManager.getReadObjectRequestMessage(portId, null, null, 
					DataTransferConstants.DATA_TRANSFER_CONSTANTS_RIB_OBJECT_CLASS, 0, 
					DataTransferConstants.DATA_TRANSFER_CONSTANTS_RIB_OBJECT_NAME, 0, true);
		}else if (ribDaemon.getIPCProcess().getQoSCubes().size() == 0){
			cdapMessage = cdapSessionManager.getReadObjectRequestMessage(portId, null, null, 
					QoSCube.QOSCUBE_SET_RIB_OBJECT_CLASS, 0, QoSCube.QOSCUBE_SET_RIB_OBJECT_NAME, 
					0, true);
		}else if (ribDaemon.getIPCProcess().getNeighbors().size() == 0){
			cdapMessage = cdapSessionManager.getReadObjectRequestMessage(portId, null, null, 
					Neighbor.NEIGHBOR_SET_RIB_OBJECT_CLASS, 0, Neighbor.NEIGHBOR_SET_RIB_OBJECT_NAME, 
					0, true);
		}
		
		return cdapMessage;
	}
	
	/**
	 * Create the objects in the RIB
	 * @throws RIBDaemonException
	 */
	private void commitEnrollment() throws RIBDaemonException{
		ribDaemon.start(RIBObjectNames.OPERATIONAL_STATUS_RIB_OBJECT_CLASS, 
				RIBObjectNames.OPERATIONAL_STATUS_RIB_OBJECT_NAME, null);
	}
	
	/**
	 * Received a Read Response message
	 */
	@Override
	public void readResponse(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor)
			throws RIBDaemonException {
		if (!isValidPortId(cdapSessionDescriptor)){
			return;
		}
		
		synchronized(this){
			switch(state){
			case WAIT_READ_RESPONSE:
				handleReadResponse(cdapMessage);
				break;
			default:
				this.abortEnrollment(this.remoteNamingInfo, portId, READ_RESPONSE_IN_BAD_STATE, true, true);
				break;
			}
		}
	}
	
	/**
	 * See if the response is valid and contains an object. See if more objects 
	 * are required. If not, 
	 * @param cdapMessage
	 */
	private void handleReadResponse(CDAPMessage cdapMessage){
		//Cancel timer
		timerTask.cancel();
		
		//Check if I'm allowed to start early
		if (cdapMessage.getResult() != 0 || cdapMessage.getObjValue() == null){
			this.abortEnrollment(this.remoteNamingInfo, portId, UNSUCCESSFULL_READ_RESPONSE, true, true);
			return;
		}
		
		//Update the enrollment information with the new value
		if (cdapMessage.getResult() != 0 || cdapMessage.getObjValue() == null){
			this.abortEnrollment(this.remoteNamingInfo, portId, UNSUCCESSFULL_READ_RESPONSE, true, true);
			return;
		}
		
		if (cdapMessage.getObjName().equals(DataTransferConstants.DATA_TRANSFER_CONSTANTS_RIB_OBJECT_NAME)){
			try{
				DataTransferConstants constants = (DataTransferConstants) encoder.decode(
						cdapMessage.getObjValue().getByteval(), DataTransferConstants.class);
				ribDaemon.create(cdapMessage.getObjClass(), cdapMessage.getObjInst(), 
						cdapMessage.getObjName(), constants, null);
			}catch(Exception ex){
				log.error(ex);
			}
		}else if (cdapMessage.getObjName().equals(QoSCube.QOSCUBE_SET_RIB_OBJECT_NAME)){
			try{
				QoSCube[] cubesArray = (QoSCube[]) encoder.decode(
						cdapMessage.getObjValue().getByteval(), QoSCube[].class);
				ribDaemon.create(cdapMessage.getObjClass(), cdapMessage.getObjInst(), 
						cdapMessage.getObjName(), cubesArray, null);
			}catch(Exception ex){
				log.error(ex);
			}
		}else if (cdapMessage.getObjName().equals(Neighbor.NEIGHBOR_SET_RIB_OBJECT_NAME)){
			try{
				Neighbor[] neighborsArray = (Neighbor[]) encoder.decode(
						cdapMessage.getObjValue().getByteval(), Neighbor[].class);
				ribDaemon.create(cdapMessage.getObjClass(), cdapMessage.getObjInst(), 
						cdapMessage.getObjName(), neighborsArray, null);
			}catch(Exception ex){
				log.error(ex);
			}
		}else{
			log.warn("The object to be created is not required for enrollment: "+cdapMessage.toString());
		}
		
		//Request more information or proceed with the enrollment program
		try{
			requestMoreInformationOrStart();
		}catch(Exception ex){
			log.error(ex);
			this.abortEnrollment(this.remoteNamingInfo, portId, UNEXPECTED_ERROR + ex.getMessage(), true, true);
		}
	}
	
	/**
	 * A stop request was received
	 * @param cdapMessage
	 * @param cdapSessionDescriptor
	 */
	public void start(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor){
		if (!isValidPortId(cdapSessionDescriptor)){
			return;
		}

		synchronized(this){
			switch(state){
			case WAIT_START:
				handleStartOperation(cdapMessage);
				break;
			case ENROLLED:
				//Do nothing, just ignore
				break;
			default:
				this.abortEnrollment(this.remoteNamingInfo, portId, START_IN_BAD_STATE, true, true);
				break;
			}
		}
	}
	
	/**
	 * Commit the enrollment information and
	 * start.
	 */
	private void handleStartOperation(CDAPMessage cdapMessage){
		//Cancel timer
		timerTask.cancel();
		
		if (cdapMessage.getResult() != 0){
			this.abortEnrollment(this.remoteNamingInfo, portId, UNSUCCESSFULL_START, true, true);
			return;
		}
		
		try{
			commitEnrollment();
			enrollmentCompleted(true);
		}catch(Exception ex){
			log.error(ex);
			this.abortEnrollment(this.remoteNamingInfo, portId, PROBLEMS_COMITING_ENROLLMENT_INFO, true, true);
		}
	}
}
