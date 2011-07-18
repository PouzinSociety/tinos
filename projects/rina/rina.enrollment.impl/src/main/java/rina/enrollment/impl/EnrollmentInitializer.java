package rina.enrollment.impl;

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
	
	private EnrollmentStateMachine enrollmentStateMachine = null;
	
	private boolean cancelread = false;
		
	private State state = State.ADDRESS;
	
	private int invokeId = 0;
	
	private int portId = 0;

	public EnrollmentInitializer(EnrollmentStateMachine enrollmentStateMachine, int invokeId, int portId){
		this.enrollmentStateMachine = enrollmentStateMachine;
		this.invokeId = invokeId;
		this.portId = portId;
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
					enrollmentStateMachine.processCDAPMessage(null, portId);
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
				"rina.messages.ApplicationProcessNameSynonym", 1, "daf.management.currentSynonym", objectValue, 0, null);
		
		enrollmentStateMachine.sendCDAPMessage(cdapMessage);
		enrollmentStateMachine.setRemoteAddress(apNameSynonym);
		state = State.WHATEVERCAST_NAMES;
	}
	
	private void sendWhatevercastNames() throws CDAPException, IOException, RIBDaemonException{
		byte[] serializedObject = null;
		ObjectValue objectValue = null;
		
		RIBDaemon ribDaemon = enrollmentStateMachine.getRIBDaemon();
		List<WhatevercastName> whatevercastNames = (List<WhatevercastName>) ribDaemon.read(null, RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + RIBObjectNames.SEPARATOR + 
				RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.WHATEVERCAST_NAMES, 0);

		for(int i=0; i<whatevercastNames.size(); i++){
			try{
				serializedObject = enrollmentStateMachine.getEncoder().encode(whatevercastNames.get(i));
				objectValue = new ObjectValue();
				objectValue.setByteval(serializedObject);
				CDAPMessage cdapMessage = CDAPMessage.getReadObjectResponseMessage(Flags.F_RD_INCOMPLETE, invokeId, 
						"rina.messages.WhatevercastName", 2 + i, "daf.management.whatevercast", objectValue, 0, null);
				enrollmentStateMachine.sendCDAPMessage(cdapMessage);
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}

		state = State.DATA_TRANSFER_CONSTANTS;
	}
	
	private void sendDataTransferConstants() throws CDAPException, IOException{
		byte[] serializedObject = null;
		ObjectValue objectValue = null;
		
		DataTransferConstants dataTransferConstants = new DataTransferConstants();
		dataTransferConstants.setAddressLength(2);
		dataTransferConstants.setCepIdLength(2);
		dataTransferConstants.setDIFConcatenation(true);
		dataTransferConstants.setDIFFragmentation(false);
		dataTransferConstants.setDIFIntegrity(false);
		dataTransferConstants.setLengthLength(2);
		dataTransferConstants.setMaxPDUSize(1950);
		dataTransferConstants.setPortIdLength(2);
		dataTransferConstants.setQosIdLength(1);
		dataTransferConstants.setSequenceNumberLength(2);
		
		try{
			serializedObject = enrollmentStateMachine.getEncoder().encode(dataTransferConstants);
			objectValue = new ObjectValue();
			objectValue.setByteval(serializedObject);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		CDAPMessage cdapMessage = CDAPMessage.getReadObjectResponseMessage(Flags.F_RD_INCOMPLETE, invokeId, 
				"rina.messages.DataTransferConstants", 4, "dif.ipc.datatransfer.constants", objectValue, 0, null);
		
		enrollmentStateMachine.sendCDAPMessage(cdapMessage);
		state = State.QOS_CUBES;
	}

	private void sendQoSCubes() throws CDAPException, IOException{
		byte[] serializedObject = null;
		ObjectValue objectValue = null;
		Flags flags = null;

		QoSCube qosCube = new QoSCube();
		qosCube.setAverageBandwidth(0);
		qosCube.setAverageSDUBandwidth(0);
		qosCube.setDelay(0);
		qosCube.setJitter(0);
		qosCube.setMaxAllowableGapSdu(-1);
		qosCube.setOrder(false);
		qosCube.setPartialDelivery(true);
		qosCube.setPeakBandwidthDuration(0);
		qosCube.setPeakSDUBandwidthDuration(0);
		qosCube.setQosId(new byte[]{0x01});
		qosCube.setUndetectedBitErrorRate(Double.valueOf("1E-09"));
		flags = Flags.F_RD_INCOMPLETE;

		try{
			serializedObject = enrollmentStateMachine.getEncoder().encode(qosCube);
			objectValue = new ObjectValue();
			objectValue.setByteval(serializedObject);
			CDAPMessage cdapMessage = CDAPMessage.getReadObjectResponseMessage(flags, invokeId, 
					"rina.messages.qosCube", 5, "dif.management.flowallocator.qoscube", objectValue, 0, null);
			enrollmentStateMachine.sendCDAPMessage(cdapMessage);
		}catch(Exception ex){
			ex.printStackTrace();
		}

		qosCube = new QoSCube();
		qosCube.setAverageBandwidth(0);
		qosCube.setAverageSDUBandwidth(0);
		qosCube.setDelay(0);
		qosCube.setJitter(0);
		qosCube.setMaxAllowableGapSdu(0);
		qosCube.setOrder(true);
		qosCube.setPartialDelivery(false);
		qosCube.setPeakBandwidthDuration(0);
		qosCube.setPeakSDUBandwidthDuration(0);
		qosCube.setQosId(new byte[]{0x02});
		qosCube.setUndetectedBitErrorRate(Double.valueOf("1E-09"));

		try{
			serializedObject = enrollmentStateMachine.getEncoder().encode(qosCube);
			objectValue = new ObjectValue();
			objectValue.setByteval(serializedObject);
			CDAPMessage cdapMessage = CDAPMessage.getReadObjectResponseMessage(null, invokeId, 
					"rina.messages.qosCube", 5, "dif.management.flowallocator.qoscube", objectValue, 0, null);
			enrollmentStateMachine.sendCDAPMessage(cdapMessage);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		state = State.DONE;
	}
}
