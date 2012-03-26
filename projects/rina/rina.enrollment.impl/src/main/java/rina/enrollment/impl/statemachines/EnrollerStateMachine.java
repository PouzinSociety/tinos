package rina.enrollment.impl.statemachines;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.WhatevercastName;
import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.ObjectValue;
import rina.efcp.api.DataTransferConstants;
import rina.encoding.api.Encoder;
import rina.enrollment.api.EnrollmentInformationRequest;
import rina.enrollment.api.EnrollmentTask;
import rina.enrollment.api.Neighbor;
import rina.flowallocator.api.DirectoryForwardingTable;
import rina.flowallocator.api.QoSCube;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * The state machine of the party that is a member of the DIF
 * and will help the joining party (enrollee) to join the DIF.
 * @author eduardgrasa
 *
 */
public class EnrollerStateMachine extends BaseEnrollmentStateMachine{
	
	private static final Log log = LogFactory.getLog(EnrollerStateMachine.class);
	
	public EnrollerStateMachine(RIBDaemon ribDaemon, CDAPSessionManager cdapSessionManager, Encoder encoder, 
			ApplicationProcessNamingInfo remoteNamingInfo, EnrollmentTask enrollmentTask, long timeout){
		super(ribDaemon, cdapSessionManager, encoder, remoteNamingInfo, enrollmentTask, timeout);
	}
	
	/**
	 * An M_CONNECT message has been received
	 * @param cdapMessage
	 * @param portId
	 */
	public void connect(CDAPMessage cdapMessage, int portId) {
		switch(this.state){
		case NULL:
			handleNullState(cdapMessage, portId);
			break;
		default:
			this.abortEnrollment(this.remoteNamingInfo, portId, CONNECT_IN_NOT_NULL, false, true);
			break;
		}
	}
	
	/**
	 * Handle the transition from the NULL to the WAIT_START_ENROLLMENT state.
	 * Authenticate the remote peer and issue a connect response
	 * @param cdapMessage
	 * @param portId
	 */
	private void handleNullState(CDAPMessage cdapMessage, int portId){
		CDAPMessage outgoingCDAPMessage = null;
		this.portId = portId;
		log.debug(portId);

		log.debug("Authenticating PC process "+cdapMessage.getSrcApName()+" "+cdapMessage.getSrcApInst());
		remotePeer.setApplicationProcessName(cdapMessage.getSrcApName());
		remotePeer.setApplicationProcessInstance(cdapMessage.getSrcApInst());

		//TODO authenticate sender
		log.debug("Authentication successfull");

		//Send M_CONNECT_R
		try{
			outgoingCDAPMessage = cdapSessionManager.getOpenConnectionResponseMessage(portId, cdapMessage.getAuthMech(), cdapMessage.getAuthValue(), cdapMessage.getSrcAEInst(), 
					BaseEnrollmentStateMachine.DEFAULT_ENROLLMENT, cdapMessage.getSrcApInst(), cdapMessage.getSrcApName(), 0, null, cdapMessage.getDestAEInst(), 
					BaseEnrollmentStateMachine.DEFAULT_ENROLLMENT, this.ribDaemon.getIPCProcess().getApplicationProcessInstance(), 
					this.ribDaemon.getIPCProcess().getApplicationProcessName(), cdapMessage.getInvokeID());

			sendCDAPMessage(outgoingCDAPMessage);

			//set timer (max time to wait before getting M_START)
			timerTask = getEnrollmentFailedTimerTask(START_ENROLLMENT_TIMEOUT, false);
			timer.schedule(timerTask, timeout);
			log.debug("Waiting for start enrollment request message");

			this.setState(State.WAIT_START_ENROLLMENT);
		}catch(CDAPException ex){
			log.error(ex);
		}
	}
	
	/**
	 * Called by the Enrollment object when it receives an M_START message from 
	 * the enrolling member
	 * @param cdapMessage
	 * @param cdapSessionDescriptor
	 */
	public void start(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) {
		if (!isValidPortId(cdapSessionDescriptor)){
			return;
		}
		
		switch(state){
		case WAIT_START_ENROLLMENT:
			handleStartEnrollment(cdapMessage);
			break;
		default:
			this.abortEnrollment(this.remoteNamingInfo, portId, START_IN_BAD_STATE, false, true);
			break;
		}
	}
	
	/**
	 * Have to look at the enrollment information request, from that deduce if the IPC process 
	 * requesting to enroll with me is already a member of the DIF and if its address is valid.
	 * If it is not a member of the DIF, send a new address with the M_START_R, send the 
	 * M_CREATEs to provide the DIF initialization information and state, and send M_STOP_R.
	 * IF it is a valid member, just send M_START_R with no address and M_STOP_R
	 * @param cdapMessage
	 */
	private void handleStartEnrollment(CDAPMessage cdapMessage){
		EnrollmentInformationRequest eiRequest = null;
		boolean requiresInitialization = false;
		CDAPMessage responseMessage = null;
		ObjectValue objectValue = null;

		//Cancel timer
		timerTask.cancel();

		//Find out if the enrolling IPC process requires initialization
		try{
			if (cdapMessage.getObjValue() == null){
				requiresInitialization = true;
			}else{
				eiRequest = (EnrollmentInformationRequest) encoder.
				decode(cdapMessage.getObjValue().getByteval(), EnrollmentInformationRequest.class);
				if (!isValidAddress(eiRequest.getAddress())){
					requiresInitialization = true;
				}
			}
		}catch(Exception ex){
			//TODO what to do?
			ex.printStackTrace();
		}

		
		try{
			//Send M_START_R
			if (requiresInitialization){
				objectValue = new ObjectValue();
				eiRequest.setAddress(getValidAddress());
				objectValue.setByteval(encoder.encode(eiRequest));
			}

			responseMessage =
				cdapSessionManager.getStartObjectResponseMessage(this.portId, null, cdapMessage.getObjClass(), objectValue, 0, 
						cdapMessage.getObjName(), 0, null, cdapMessage.getInvokeID());
			sendCDAPMessage(responseMessage);


			//If initialization is required send the M_CREATEs
			if (requiresInitialization){
				initializeEnrollee();
			}

			//Send the M_STOP request
			objectValue = new ObjectValue();
			objectValue.setBooleanval(true);
			responseMessage = cdapSessionManager.getStopObjectRequestMessage(this.portId, null, null, EnrollmentInformationRequest.ENROLLMENT_INFO_OBJECT_CLASS, 
					objectValue, 0, EnrollmentInformationRequest.ENROLLMENT_INFO_OBJECT_NAME, 0, true);
			sendCDAPMessage(responseMessage);
			timerTask = getEnrollmentFailedTimerTask(STOP_ENROLLMENT_RESPONSE_TIMEOUT, false);
			timer.schedule(timerTask, timeout);
			log.debug("Waiting for stop enrollment response message");

			this.setState(State.WAIT_STOP_ENROLLMENT_RESPONSE);
		}catch(Exception ex){
			log.error(ex);
			this.abortEnrollment(this.remoteNamingInfo, portId, UNEXPECTED_ERROR + ex.getMessage(), false, true);
		}
	}
	
	/**
	 * Decides if a given address is valid or not
	 * @param address
	 * @return
	 */
	private boolean isValidAddress(long address) {
		if (address == 0){
			return false;
		}
		
		return true;
	}
	
	/**
	 * Return a valid address for the IPC process that 
	 * wants to join the DIF
	 * @return
	 */
	private long getValidAddress(){
		//TODO implement this
		return 2;
	}
	
	/**
	 * Send all the information required to start operation to 
	 * the IPC process that is enrolling to me
	 * @throws Exception
	 */
	private void initializeEnrollee() throws Exception{
		//Send whatevercast names
		sendCreateInformation(WhatevercastName.WHATEVERCAST_NAME_SET_RIB_OBJECT_CLASS, 
				WhatevercastName.WHATEVERCAST_NAME_SET_RIB_OBJECT_NAME);
		
		//Send data transfer constants
		sendCreateInformation(DataTransferConstants.DATA_TRANSFER_CONSTANTS_RIB_OBJECT_CLASS, 
				DataTransferConstants.DATA_TRANSFER_CONSTANTS_RIB_OBJECT_NAME);
		
		//Send QoS Cubes
		sendCreateInformation(QoSCube.QOSCUBE_SET_RIB_OBJECT_CLASS, 
				QoSCube.QOSCUBE_SET_RIB_OBJECT_NAME);
		
		//Send DirectoryForwardingTableEntries
		sendCreateInformation(DirectoryForwardingTable.DIRECTORY_FORWARDING_TABLE_ENTRY_SET_RIB_OBJECT_CLASS, 
				DirectoryForwardingTable.DIRECTORY_FORWARDING_ENTRY_SET_RIB_OBJECT_NAME);
		
		//Send neighbors (including myself)
		RIBObject neighborSet = ribDaemon.read(Neighbor.NEIGHBOR_SET_RIB_OBJECT_CLASS, Neighbor.NEIGHBOR_SET_RIB_OBJECT_NAME);
		List<RIBObject> neighbors = neighborSet.getChildren();
		
		Neighbor[] neighborsArray = new Neighbor[neighbors.size() + 1];
		for(int i=1; i<=neighbors.size(); i++){
			neighborsArray[i] = (Neighbor) neighbors.get(i-1).getObjectValue();
		}
		
		neighborsArray[0] = new Neighbor();
		neighborsArray[0].setAddress(ribDaemon.getIPCProcess().getAddress().longValue());
		neighborsArray[0].setApplicationProcessName(ribDaemon.getIPCProcess().getApplicationProcessName());
		neighborsArray[0].setApplicationProcessInstance(ribDaemon.getIPCProcess().getApplicationProcessInstance());
		
		ObjectValue objectValue = new ObjectValue();
		objectValue.setByteval(encoder.encode(neighborsArray));
		CDAPMessage cdapMessage = cdapSessionManager.getCreateObjectRequestMessage(this.portId, null, null, neighborSet.getObjectClass(), 
				0, Neighbor.NEIGHBOR_SET_RIB_OBJECT_NAME, objectValue, 0, false);
		sendCDAPMessage(cdapMessage);
	}
	
	/**
	 * Gets the object value from the RIB and send it as a CDAP Mesage
	 * @param objectClass the class of the object to be send
	 * @param objectName the name of the object to be send
	 * @param suffix the suffix to send after enrollment info
	 * @throws Exception
	 */
	private void sendCreateInformation(String objectClass, String objectName) throws Exception{
		RIBObject ribObject = null;
		CDAPMessage cdapMessage = null;
		ObjectValue objectValue = null;
		
		ribObject = ribDaemon.read(objectClass, objectName);
		objectValue = new ObjectValue();
		objectValue.setByteval(encoder.encode(ribObject.getObjectValue()));
		cdapMessage = cdapSessionManager.getCreateObjectRequestMessage(this.portId, null, null, objectClass, 
				ribObject.getObjectInstance(), objectName, objectValue, 0, false);
		sendCDAPMessage(cdapMessage);
	}

	/**
	 * The response of the stop operation has been received
	 * @param cdapMessage
	 * @param cdapSessionDescriptor
	 */
	@Override
	public void stopResponse(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor)
			throws RIBDaemonException {
		if (!isValidPortId(cdapSessionDescriptor)){
			return;
		}
		
		switch(state){
		case WAIT_STOP_ENROLLMENT_RESPONSE:
			handleStopEnrollmentResponse(cdapMessage);
			break;
		default:
			this.abortEnrollment(this.remoteNamingInfo, portId, STOP_RESPONSE_IN_BAD_STATE, false, true);
			break;
		}
	}
	
	/**
	 * The response of the stop operation has been received, send M_START operation without 
	 * waiting for an answer and consider the process enrolled
	 * @param cdapMessage
	 * @param cdapSessionDescriptor
	 */
	private void handleStopEnrollmentResponse(CDAPMessage cdapMessage){
		//Cancel timer
		timerTask.cancel();

		if (cdapMessage.getResult() != 0){
			this.setState(State.NULL);
			enrollmentTask.enrollmentFailed(this.remoteNamingInfo, portId, cdapMessage.getResultReason(), false, true);
		}else{
			
			try{
				CDAPMessage startMessage = cdapSessionManager.getStartObjectRequestMessage(portId, null, null, 
						RIBObjectNames.OPERATIONAL_STATUS_RIB_OBJECT_CLASS, null, 0, 
						RIBObjectNames.OPERATIONAL_STATUS_RIB_OBJECT_NAME, 0, false);
				sendCDAPMessage(startMessage);
				//TODO, do this?
				/*ribDaemon.create(null, DAFMember.DAF_MEMBER_SET_RIB_OBJECT_NAME + RIBObjectNames.SEPARATOR + 
						remotePeer.getApplicationProcessName()+remotePeer.getApplicationProcessInstance(), 
						0, remotePeer);*/
			}catch(Exception ex){
				log.error(ex);
			}
			
			enrollmentCompleted(false);
		}
	}
}
