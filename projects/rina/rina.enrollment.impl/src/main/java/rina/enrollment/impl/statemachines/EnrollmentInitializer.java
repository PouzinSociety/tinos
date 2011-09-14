package rina.enrollment.impl.statemachines;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.ApplicationProcessNameSynonym;
import rina.applicationprocess.api.WhatevercastName;
import rina.cdap.api.CDAPException;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.Flags;
import rina.cdap.api.message.ObjectValue;
import rina.efcp.api.DataTransferConstants;
import rina.flowallocator.api.QoSCube;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * Sends all the M_READ_R messages required to initialize an IPC process that wants to join a DIF
 * @author eduardgrasa
 *
 */
public class EnrollmentInitializer implements Runnable{
	
	private static final Log log = LogFactory.getLog(EnrollmentInitializer.class);
	
	private enum State {ADDRESS, WHATEVERCAST_NAMES, DATA_TRANSFER_CONSTANTS, QOS_CUBES, 
		EFCP_POLICIES, FLOW_ALLOCATOR_POLICIES, DONE};
	
	private DefaultEnrollmentStateMachine enrollmentStateMachine = null;
	
	private boolean cancelread = false;
		
	private State state = State.ADDRESS;
	
	private int invokeId = 0;

	public EnrollmentInitializer(DefaultEnrollmentStateMachine enrollmentStateMachine, int invokeId, int portId){
		this.enrollmentStateMachine = enrollmentStateMachine;
		this.invokeId = invokeId;
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
		byte[] serializedAddress = null;
		ObjectValue objectValue = null;
		
		ApplicationProcessNameSynonym apNameSynonym = new ApplicationProcessNameSynonym();
		apNameSynonym.setApplicationProcessName(enrollmentStateMachine.getRemoteNamingInfo().getApplicationProcessName());
		apNameSynonym.setApplicationProcessInstance(enrollmentStateMachine.getRemoteNamingInfo().getApplicationProcessInstance());
		apNameSynonym.setSynonym(new byte[]{0x02});
		try{
			serializedAddress = enrollmentStateMachine.getEncoder().encode(apNameSynonym);
			objectValue = new ObjectValue();
			objectValue.setByteval(serializedAddress);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		CDAPMessage cdapMessage = CDAPMessage.getReadObjectResponseMessage(Flags.F_RD_INCOMPLETE, invokeId, 
				"rina.messages.ApplicationProcessNameSynonym", 1, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.CURRENT_SYNONYM, objectValue, 0, null);
		
		enrollmentStateMachine.sendCDAPMessage(cdapMessage);
		enrollmentStateMachine.setRemoteAddress(apNameSynonym);
		state = State.WHATEVERCAST_NAMES;
	}
	
	private void sendWhatevercastNames() throws CDAPException, IOException, RIBDaemonException{
		byte[] serializedObject = null;
		ObjectValue objectValue = null;
		
		RIBDaemon ribDaemon = enrollmentStateMachine.getRIBDaemon();
		List<WhatevercastName> whatevercastNames = (List<WhatevercastName>) ribDaemon.read(null, RIBObjectNames.SEPARATOR +  RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + 
				RIBObjectNames.MANAGEMENT + RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.WHATEVERCAST_NAMES, 0);

		for(int i=0; i<whatevercastNames.size(); i++){
			try{
				serializedObject = enrollmentStateMachine.getEncoder().encode(whatevercastNames.get(i));
				objectValue = new ObjectValue();
				objectValue.setByteval(serializedObject);
				CDAPMessage cdapMessage = CDAPMessage.getReadObjectResponseMessage(Flags.F_RD_INCOMPLETE, invokeId, 
						"rina.messages.WhatevercastName", 2 + i, RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + RIBObjectNames.SEPARATOR + 
						RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.WHATEVERCAST_NAMES + RIBObjectNames.SEPARATOR + (i+1), objectValue, 0, null);
				enrollmentStateMachine.sendCDAPMessage(cdapMessage);
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}

		state = State.DATA_TRANSFER_CONSTANTS;
	}
	
	private void sendDataTransferConstants() throws CDAPException, IOException, RIBDaemonException{
		byte[] serializedObject = null;
		ObjectValue objectValue = null;
		
		RIBDaemon ribDaemon = enrollmentStateMachine.getRIBDaemon();
		DataTransferConstants dataTransferConstants = (DataTransferConstants) ribDaemon.read(null,RIBObjectNames.SEPARATOR + RIBObjectNames.DIF + RIBObjectNames.SEPARATOR + RIBObjectNames.IPC + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.DATA_TRANSFER+ RIBObjectNames.SEPARATOR + RIBObjectNames.CONSTANTS, 0);
		
		try{
			serializedObject = enrollmentStateMachine.getEncoder().encode(dataTransferConstants);
			objectValue = new ObjectValue();
			objectValue.setByteval(serializedObject);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		CDAPMessage cdapMessage = CDAPMessage.getReadObjectResponseMessage(Flags.F_RD_INCOMPLETE, invokeId, 
				"rina.messages.DataTransferConstants", 4, RIBObjectNames.SEPARATOR + RIBObjectNames.DIF + RIBObjectNames.SEPARATOR + RIBObjectNames.IPC + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.DATA_TRANSFER+ RIBObjectNames.SEPARATOR + RIBObjectNames.CONSTANTS, objectValue, 0, null);
		
		enrollmentStateMachine.sendCDAPMessage(cdapMessage);
		state = State.QOS_CUBES;
	}

	private void sendQoSCubes() throws CDAPException, IOException, RIBDaemonException{
		byte[] serializedObject = null;
		ObjectValue objectValue = null;
		Flags flags = null;
		
		RIBDaemon ribDaemon = enrollmentStateMachine.getRIBDaemon();
		List<QoSCube> qosCubes = (List<QoSCube>) ribDaemon.read(null, RIBObjectNames.SEPARATOR + RIBObjectNames.DIF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.FLOW_ALLOCATOR + RIBObjectNames.SEPARATOR + RIBObjectNames.QOS_CUBES, 0);

		for(int i=0; i<qosCubes.size(); i++){
			try{
				serializedObject = enrollmentStateMachine.getEncoder().encode(qosCubes.get(i));
				objectValue = new ObjectValue();
				objectValue.setByteval(serializedObject);
				CDAPMessage cdapMessage = CDAPMessage.getReadObjectResponseMessage(Flags.F_RD_INCOMPLETE, invokeId, 
						"rina.messages.WhatevercastName", 2 + i, RIBObjectNames.SEPARATOR + RIBObjectNames.DIF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
						RIBObjectNames.SEPARATOR + RIBObjectNames.FLOW_ALLOCATOR + RIBObjectNames.SEPARATOR + RIBObjectNames.QOS_CUBES + RIBObjectNames.SEPARATOR + (i+1), objectValue, 0, null);
				enrollmentStateMachine.sendCDAPMessage(cdapMessage);
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		
		state = State.DONE;
	}
}
