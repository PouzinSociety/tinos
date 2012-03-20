package rina.enrollment.impl.statemachines;

import java.util.Timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.DAFMember;
import rina.applicationprocess.api.WhatevercastName;
import rina.cdap.api.BaseCDAPMessageHandler;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.AuthTypes;
import rina.cdap.api.message.ObjectValue;
import rina.efcp.api.DataTransferConstants;
import rina.encoding.api.Encoder;
import rina.enrollment.api.EnrollmentInformationRequest;
import rina.enrollment.api.EnrollmentTask;
import rina.enrollment.impl.statemachines.EnrollmentStateMachine.State;
import rina.flowallocator.api.DirectoryForwardingTableEntry;
import rina.flowallocator.api.QoSCube;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.IPCException;
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
	 * Stores the initialization information received by the joining IPC Process
	 */
	private EnrollmentInitializationInformation enrollmentInformation = null;
	
	public EnrolleeStateMachine(RIBDaemon ribDaemon, CDAPSessionManager cdapSessionManager, Encoder encoder, 
			ApplicationProcessNamingInfo remoteNamingInfo, EnrollmentTask enrollmentTask, long timeout){
		this.ribDaemon = ribDaemon;
		this.cdapSessionManager = cdapSessionManager;
		this.encoder = encoder;
		this.remoteNamingInfo = remoteNamingInfo;
		this.enrollmentTask = enrollmentTask;
		this.remotePeer = new DAFMember();
		this.timeout = timeout;
		this.enrollmentInformation = new EnrollmentInitializationInformation();
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
				ApplicationProcessNamingInfo apNamingInfo = (ApplicationProcessNamingInfo) ribDaemon.getIPCProcess().getApplicationProcessNamingInfo();
				CDAPMessage requestMessage = cdapSessionManager.getOpenConnectionRequestMessage(portId, AuthTypes.AUTH_NONE, null, null, DefaultEnrollmentStateMachine.DEFAULT_ENROLLMENT, 
						candidate.getApplicationProcessInstance(), candidate.getApplicationProcessName(), null, DefaultEnrollmentStateMachine.DEFAULT_ENROLLMENT, 
						apNamingInfo.getApplicationProcessInstance(), apNamingInfo.getApplicationProcessName());
				ribDaemon.sendMessage(requestMessage, portId, null);
				this.portId = portId;

				//Set timer
				timer = new Timer();
				timerTask = getEnrollmentFailedTimerTask(CONNECT_RESPONSE_TIMEOUT, true);
				timer.schedule(timerTask, timeout);

				//Update state
				this.state = State.WAIT_CONNECT_R;
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
		case WAIT_CONNECT_R:
			handleConnectResponse(cdapMessage);
			break;
		default:
			this.state = State.NULL;;
			enrollmentTask.enrollmentFailed(remoteNamingInfo, portId, "Message received in wrong order", true, true);
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

		//Send M_START with EnrollmentInformation object
		try{
			EnrollmentInformationRequest eiRequest = new EnrollmentInformationRequest();
			eiRequest.setAddress(ribDaemon.getIPCProcess().getAddress());
			ObjectValue objectValue = new ObjectValue();
			objectValue.setByteval(encoder.encode(eiRequest));
			CDAPMessage startMessage = cdapSessionManager.getStartObjectRequestMessage(portId, null, null, 
					EnrollmentTask.ENROLLMENT_RIB_OBJECT_CLASS, objectValue, 0, 
					EnrollmentTask.ENROLLMENT_RIB_OBJECT_NAME, 0, true);
			sendCDAPMessage(startMessage);

			//Set timer
			timerTask = getEnrollmentFailedTimerTask(START_RESPONSE_TIMEOUT, true);
			timer.schedule(timerTask, timeout);

			//Update state
			state = State.WAIT_START_ENROLLMENT_R;
		}catch(Exception ex){
			log.error(ex);
			//TODO what to do?
		}
	}
	
	/**
	 * Received the Start Response
	 */
	public void startResponse(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor)
			throws RIBDaemonException {
		if (!isValidPortId(cdapSessionDescriptor)){
			return;
		}
		
		switch(state){
		case WAIT_START_ENROLLMENT_R:
			handleStartEnrollmentResponse(cdapMessage);
			break;
		default:
			enrollmentTask.enrollmentFailed(this.remoteNamingInfo, portId, START_RESPONSE_IN_BAD_STATE, true, true);
			break;
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
			this.enrollmentInformation.setSynonym(new Long(cdapMessage.getObjValue().getInt64val()));
		}
		
		//Set timer
		timerTask = getEnrollmentFailedTimerTask(STOP_ENROLLMENT_TIMEOUT, true);
		timer.schedule(timerTask, timeout);
		
		//Update state
		state = State.WAIT_STOP_ENROLLMENT_RESPONSE;
	}
	
	/**
	 * A create request was received
	 * @param cdapMessage
	 * @param cdapSessionDescriptor
	 */
	public void create(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor){
		if (!isValidPortId(cdapSessionDescriptor)){
			return;
		}
		
		switch(state){
		case WAIT_STOP_ENROLLMENT_RESPONSE:
			handleCreate(cdapMessage);
			break;
		default:
			enrollmentTask.enrollmentFailed(this.remoteNamingInfo, portId, CREATE_IN_BAD_STATE, true, true);
			break;
		}
	}
	
	/**
	 * Handle the create request message (store the information in the enrollment object, will 
	 * process it once I'm authorized to start operating)
	 * @param cdapMessage
	 */
	private void handleCreate(CDAPMessage cdapMessage){
		if (cdapMessage.getObjName().equals(EnrollmentInformationRequest.ENROLLMENT_INFO_OBJECT_NAME + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.WHATEVERCAST_NAMES)){
			try{
				WhatevercastName[] namesArray = (WhatevercastName[]) encoder.decode(
						cdapMessage.getObjValue().getByteval(), WhatevercastName[].class);
				for(int i=0; i<namesArray.length; i++){
					enrollmentInformation.addWhatevercastName(namesArray[i]);
				}
			}catch(Exception ex){
				log.error(ex);
			}
		}else if (cdapMessage.getObjName().equals(EnrollmentInformationRequest.ENROLLMENT_INFO_OBJECT_NAME + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.DATA_TRANSFER + RIBObjectNames.SEPARATOR + 
				RIBObjectNames.CONSTANTS)){
			try{
				DataTransferConstants constants = (DataTransferConstants) encoder.decode(
						cdapMessage.getObjValue().getByteval(), DataTransferConstants.class);
				enrollmentInformation.setDataTransferConstants(constants);
			}catch(Exception ex){
				log.error(ex);
			}
		}else if (cdapMessage.getObjName().equals(EnrollmentInformationRequest.ENROLLMENT_INFO_OBJECT_NAME + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.QOS_CUBES)){
			try{
				QoSCube[] cubesArray = (QoSCube[]) encoder.decode(
						cdapMessage.getObjValue().getByteval(), QoSCube[].class);
				for(int i=0; i<cubesArray.length; i++){
					enrollmentInformation.addQoSCube(cubesArray[i]);
				}
			}catch(Exception ex){
				log.error(ex);
			}
		}else if (cdapMessage.getObjName().equals(EnrollmentInformationRequest.ENROLLMENT_INFO_OBJECT_NAME + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.MEMBERS)){
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
		}else if (cdapMessage.getObjName().equals(EnrollmentInformationRequest.ENROLLMENT_INFO_OBJECT_NAME + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.DIRECTORY_FORWARDING_TABLE_ENTRIES)){
			try{
				DirectoryForwardingTableEntry[] entriesArray = (DirectoryForwardingTableEntry[]) encoder.decode(
						cdapMessage.getObjValue().getByteval(), DirectoryForwardingTableEntry[].class);
				for(int i=0; i<entriesArray.length; i++){
					enrollmentInformation.addDirectoryEntry(entriesArray[i]);
				}
			}catch(Exception ex){
				log.error(ex);
			}
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
	
	/**
	 * A stop request was received
	 * @param cdapMessage
	 * @param cdapSessionDescriptor
	 */
	public void stop(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor){
		if (!isValidPortId(cdapSessionDescriptor)){
			return;
		}
		
		switch(state){
		case WAIT_STOP_ENROLLMENT_RESPONSE:
			handleStopEnrollment(cdapMessage);
			break;
		default:
			enrollmentTask.enrollmentFailed(this.remoteNamingInfo, portId, STOP_IN_BAD_STATE, true, true);
			break;
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
		//TODO
	}	
}
