package rina.enrollment.impl.statemachines;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.DAFMember;
import rina.applicationprocess.api.WhatevercastName;
import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.Flags;
import rina.cdap.api.message.ObjectValue;
import rina.efcp.api.DataTransferConstants;
import rina.flowallocator.api.QoSCube;
import rina.ipcservice.api.IPCException;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * Sends all the M_READ_R messages required to initialize an IPC process that wants to join a DIF
 * @author eduardgrasa
 *
 */
public class EnrollmentInitializer implements Runnable{
	
	private static final Log log = LogFactory.getLog(EnrollmentInitializer.class);
	
	private enum State {ADDRESS, WHATEVERCAST_NAMES, DATA_TRANSFER_CONSTANTS, QOS_CUBES, 
		EFCP_POLICIES, FLOW_ALLOCATOR_POLICIES, DAF_MEMBERS, DONE, ERROR};
	
	private DefaultEnrollmentStateMachine enrollmentStateMachine = null;
	
	private boolean cancelread = false;
		
	private State state = State.ADDRESS;
	
	private int invokeId = 0;
	
	private CDAPSessionManager cdapSessionManager = null;
	
	private int portId = 0;

	public EnrollmentInitializer(DefaultEnrollmentStateMachine enrollmentStateMachine, int invokeId, int portId, CDAPSessionManager cdapSessionManager){
		this.enrollmentStateMachine = enrollmentStateMachine;
		this.invokeId = invokeId;
		this.portId = portId;
		this.cdapSessionManager = cdapSessionManager;
	}
	
	public void cancelread(){
		this.cancelread = true;
	}
	
	public boolean isRunning(){
		return !cancelread;
	}
	
	public void run() {
		while (!cancelread){
			try{
				log.debug("State: "+state);
				switch(state){
				case ADDRESS:
					sendAddress();
					break;
				case WHATEVERCAST_NAMES:
					sendWhatevercastNames();
					break;
				case DATA_TRANSFER_CONSTANTS:
					sendDataTransferConstants();
					break;
				case QOS_CUBES:
					sendQoSCubes();
					break;
				case EFCP_POLICIES:
					break;
				case FLOW_ALLOCATOR_POLICIES:
					break;
				case DAF_MEMBERS:
					sendDAFMembers();
					break;
				case DONE:
					this.cancelread();
					enrollmentStateMachine.enrollmentDataInitializationComplete();
					break;
				default:
					break;
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
	private void sendAddress() throws CDAPException, IOException{
		ObjectValue objectValue = null;
		CDAPMessage cdapMessage = null;
		long address = 0;
		
		//1 Get an address
		try{
			address = enrollmentStateMachine.getEnrollmentTask().getAddressManager().getAvailableAddress();
		}catch(IPCException ex){
			//Signal the error and abort the enrollment
			log.error(ex);
			state = State.ERROR;
			try{
				cdapMessage = cdapSessionManager.getReadObjectResponseMessage(portId, null, 
						RIBObjectNames.CURRENT_SYNONYM_RIB_OBJECT_CLASS, 1, RIBObjectNames.CURRENT_SYNONYM_RIB_OBJECT_NAME, 
						null, ex.getErrorCode(), ex.getMessage(), invokeId);
				enrollmentStateMachine.sendCDAPMessage(cdapMessage);
				enrollmentStateMachine.getEnrollmentTask().enrollmentFailed(
						enrollmentStateMachine.getRemoteNamingInfo(), portId, "No addresses available", 
						enrollmentStateMachine.isEnrollee(), true);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		//2 Send it back
		try{
			objectValue = new ObjectValue();
			objectValue.setInt64val(address);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	    cdapMessage = cdapSessionManager.getReadObjectResponseMessage(portId, Flags.F_RD_INCOMPLETE, 
				RIBObjectNames.CURRENT_SYNONYM_RIB_OBJECT_CLASS, 1, RIBObjectNames.CURRENT_SYNONYM_RIB_OBJECT_NAME, 
				objectValue, 0, null, invokeId);
		
		enrollmentStateMachine.sendCDAPMessage(cdapMessage);
		enrollmentStateMachine.setRemoteAddress(address);
		state = State.WHATEVERCAST_NAMES;
	}
	
	private void sendWhatevercastNames() throws CDAPException, IOException, RIBDaemonException{
		byte[] serializedObject = null;
		ObjectValue objectValue = null;
		
		RIBDaemon ribDaemon = enrollmentStateMachine.getRIBDaemon();
		RIBObject whatevercastNameSet = ribDaemon.read(null, WhatevercastName.WHATEVERCAST_NAME_SET_RIB_OBJECT_NAME, 0);
		List<RIBObject> whatevercastNames = whatevercastNameSet.getChildren();
		
		WhatevercastName[] whatevercastNamesArray = new WhatevercastName[whatevercastNames.size()];
		for(int i=0; i<whatevercastNames.size(); i++){
			whatevercastNamesArray[i] = (WhatevercastName) whatevercastNames.get(i).getObjectValue();
		}
		
		try{
			serializedObject = enrollmentStateMachine.getEncoder().encode(whatevercastNamesArray);
			objectValue = new ObjectValue();
			objectValue.setByteval(serializedObject);
			CDAPMessage cdapMessage = cdapSessionManager.getReadObjectResponseMessage(portId, Flags.F_RD_INCOMPLETE, 
					whatevercastNameSet.getObjectClass(), whatevercastNameSet.getObjectInstance(), 
					whatevercastNameSet.getObjectName(), objectValue, 0, null, invokeId);
			enrollmentStateMachine.sendCDAPMessage(cdapMessage);
		}catch(Exception ex){
			log.error(ex);
		}

		state = State.DATA_TRANSFER_CONSTANTS;
	}
	
	private void sendDataTransferConstants() throws CDAPException, IOException, RIBDaemonException{
		byte[] serializedObject = null;
		ObjectValue objectValue = null;
		
		RIBDaemon ribDaemon = enrollmentStateMachine.getRIBDaemon();
		RIBObject dataTransferConstants = ribDaemon.read(null, DataTransferConstants.DATA_TRANSFER_CONSTANTS_RIB_OBJECT_NAME, 0);
		
		try{
			serializedObject = enrollmentStateMachine.getEncoder().encode(dataTransferConstants.getObjectValue());
			objectValue = new ObjectValue();
			objectValue.setByteval(serializedObject);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		CDAPMessage cdapMessage = cdapSessionManager.getReadObjectResponseMessage(portId, Flags.F_RD_INCOMPLETE, 
				dataTransferConstants.getObjectClass(), dataTransferConstants.getObjectInstance(), dataTransferConstants.getObjectName(), 
				objectValue, 0, null, invokeId);
		
		enrollmentStateMachine.sendCDAPMessage(cdapMessage);
		state = State.QOS_CUBES;
	}

	private void sendQoSCubes() throws CDAPException, IOException, RIBDaemonException{
		byte[] serializedObject = null;
		ObjectValue objectValue = null;
		Flags flags = null;
		
		RIBDaemon ribDaemon = enrollmentStateMachine.getRIBDaemon();
		RIBObject qosCubeSet = ribDaemon.read(QoSCube.QOSCUBE_SET_RIB_OBJECT_CLASS, QoSCube.QOSCUBE_SET_RIB_OBJECT_NAME, 0);
		List<RIBObject> qosCubes = qosCubeSet.getChildren();

		QoSCube[] qosCubesArray = new QoSCube[qosCubes.size()];
		for(int i=0; i<qosCubes.size(); i++){
			qosCubesArray[i] = (QoSCube) qosCubes.get(i).getObjectValue();
		}
		
		try{
			serializedObject = enrollmentStateMachine.getEncoder().encode(qosCubesArray);
			objectValue = new ObjectValue();
			objectValue.setByteval(serializedObject);
			flags = Flags.F_RD_INCOMPLETE;
			
			CDAPMessage cdapMessage = cdapSessionManager.getReadObjectResponseMessage(portId, flags, qosCubeSet.getObjectClass(), 
					qosCubeSet.getObjectInstance(), qosCubeSet.getObjectName(), objectValue, 0, null, invokeId);
			enrollmentStateMachine.sendCDAPMessage(cdapMessage);
		}catch(Exception ex){
			log.error(ex);
		}
		
		state = State.DAF_MEMBERS;
	}
	
	private void sendDAFMembers() throws CDAPException, IOException, RIBDaemonException{
		byte[] serializedObject = null;
		ObjectValue objectValue = null;
		Flags flags = null;
		
		RIBDaemon ribDaemon = enrollmentStateMachine.getRIBDaemon();
		RIBObject dafMemberSet = ribDaemon.read(DAFMember.DAF_MEMBER_SET_RIB_OBJECT_CLASS, DAFMember.DAF_MEMBER_SET_RIB_OBJECT_NAME, 0);
		List<RIBObject> dafMembers = dafMemberSet.getChildren();
		
		DAFMember[] dafMembersArray = new DAFMember[dafMembers.size() + 1];
		for(int i=1; i<=dafMembers.size(); i++){
			dafMembersArray[i] = (DAFMember) dafMembers.get(i-1).getObjectValue();
		}
		
		dafMembersArray[0] = new DAFMember();
		dafMembersArray[0].setSynonym(enrollmentStateMachine.getRIBDaemon().getIPCProcess().getAddress().longValue());
		dafMembersArray[0].setApplicationProcessName(ribDaemon.getIPCProcess().getApplicationProcessName());
		dafMembersArray[0].setApplicationProcessInstance(ribDaemon.getIPCProcess().getApplicationProcessInstance());
		
		try{
			serializedObject = enrollmentStateMachine.getEncoder().encode(dafMembersArray);
			objectValue = new ObjectValue();
			objectValue.setByteval(serializedObject);
			CDAPMessage cdapMessage = cdapSessionManager.getReadObjectResponseMessage(portId, flags, 
					dafMemberSet.getObjectClass(), dafMemberSet.getObjectInstance(), dafMemberSet.getObjectName(), 
					objectValue, 0, null, invokeId);
			enrollmentStateMachine.sendCDAPMessage(cdapMessage);
		}catch(Exception ex){
			log.error(ex);
		}
		
		state = State.DONE;
	}
}
