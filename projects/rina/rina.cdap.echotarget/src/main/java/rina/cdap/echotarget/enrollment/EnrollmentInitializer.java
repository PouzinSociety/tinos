package rina.cdap.echotarget.enrollment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rina.applicationprocess.api.WhatevercastName;
import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.Flags;
import rina.cdap.api.message.ObjectValue;
import rina.efcp.api.DataTransferConstants;
import rina.flowallocator.api.QoSCube;

/**
 * Sends all the M_READ_R messages required to initialize an IPC process that wants to join a DIF
 * @author eduardgrasa
 *
 */
public class EnrollmentInitializer implements Runnable{
	
	private enum State {ADDRESS, WHATEVERCAST_NAMES, DATA_TRANSFER_CONSTANTS, QOS_CUBES, 
		EFCP_POLICIES, FLOW_ALLOCATOR_POLICIES, DONE};
	
	private CDAPEnrollmentWorker cdapEnrollmentWorker = null;
	
	private boolean cancelread = false;
		
	private State state = State.ADDRESS;
	
	private int invokeId = 0;
	
	private int counter = 0;
	
	private CDAPSessionManager cdapSessionManager = null;
	
	private int portId = 0;

	public EnrollmentInitializer(CDAPEnrollmentWorker cdapEnrollmentWorker, int invokeId, CDAPSessionManager cdapSessionManager, int portId){
		this.cdapEnrollmentWorker = cdapEnrollmentWorker;
		this.invokeId = invokeId;
		this.cdapSessionManager = cdapSessionManager;
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
					cdapEnrollmentWorker.processCDAPMessage(null);
					break;
				default:
					break;
				}
			}catch(CDAPException ex){
				ex.printStackTrace();
			}catch(IOException ex){
				ex.printStackTrace();
			}
		}
	}
	
	private void sendAddress() throws CDAPException, IOException{
		ObjectValue objectValue = null;

		try{
			objectValue = new ObjectValue();
			objectValue.setInt64val(2);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		CDAPMessage cdapMessage = cdapSessionManager.getReadObjectResponseMessage(portId, Flags.F_RD_INCOMPLETE,
				"rina.messages.ApplicationProcessNameSynonym", 1, "/daf/management/currentSynonym", objectValue, 0, null,  invokeId);
		
		cdapEnrollmentWorker.sendCDAPMessage(cdapMessage);
		state = State.WHATEVERCAST_NAMES;
	}
	
	private void sendWhatevercastNames() throws CDAPException, IOException{
		byte[] serializedObject = null;
		ObjectValue objectValue = null;
		List<byte[]> members = null;
		
		WhatevercastName whatevercastName = new WhatevercastName();
		if (counter == 0){
			whatevercastName.setName("RINA-Demo.all.DIF");
			whatevercastName.setRule("all members");
			members = new ArrayList<byte[]>();
			members.add(new byte[]{0x01});
			members.add(new byte[]{0x02});
			whatevercastName.setSetMembers(members);
		}else if (counter == 1){
			whatevercastName.setName("RINA-Demo.one.DIF");
			whatevercastName.setRule("nearest member");
			members = new ArrayList<byte[]>();
			members.add(new byte[]{0x01});
			whatevercastName.setSetMembers(members);
		}
		
		try{
			serializedObject = cdapEnrollmentWorker.getEncoder().encode(whatevercastName);
			objectValue = new ObjectValue();
			objectValue.setByteval(serializedObject);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		CDAPMessage cdapMessage = cdapSessionManager.getReadObjectResponseMessage(portId, Flags.F_RD_INCOMPLETE,  
				"rina.messages.WhatevercastName", 2 + counter, "/daf/management/whatevercast", objectValue, 0, null, invokeId);
		
		cdapEnrollmentWorker.sendCDAPMessage(cdapMessage);
		
		if (counter == 0){
			counter++;
		}else if (counter == 1){
			counter = 0;
			state = State.DATA_TRANSFER_CONSTANTS;
		}
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
			serializedObject = cdapEnrollmentWorker.getEncoder().encode(dataTransferConstants);
			objectValue = new ObjectValue();
			objectValue.setByteval(serializedObject);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		CDAPMessage cdapMessage = cdapSessionManager.getReadObjectResponseMessage(portId, Flags.F_RD_INCOMPLETE,
				"rina.messages.DataTransferConstants", 4, "/dif/ipc/datatransfer/constants", objectValue, 0, null, invokeId);
		
		cdapEnrollmentWorker.sendCDAPMessage(cdapMessage);
		state = State.QOS_CUBES;
	}
	
	private void sendQoSCubes() throws CDAPException, IOException{
		byte[] serializedObject = null;
		ObjectValue objectValue = null;
		Flags flags = null;
		
		QoSCube qosCube = new QoSCube();
		if (counter == 0){
			qosCube.setAverageBandwidth(0);
			qosCube.setAverageSDUBandwidth(0);
			qosCube.setDelay(0);
			qosCube.setJitter(0);
			qosCube.setMaxAllowableGapSdu(-1);
			qosCube.setName("unreliable");
			qosCube.setOrder(false);
			qosCube.setPartialDelivery(true);
			qosCube.setPeakBandwidthDuration(0);
			qosCube.setPeakSDUBandwidthDuration(0);
			qosCube.setQosId(1);
			qosCube.setUndetectedBitErrorRate(Double.valueOf("1E-09"));
			flags = Flags.F_RD_INCOMPLETE;
		}else if (counter == 1){
			qosCube.setAverageBandwidth(0);
			qosCube.setAverageSDUBandwidth(0);
			qosCube.setDelay(0);
			qosCube.setJitter(0);
			qosCube.setMaxAllowableGapSdu(0);
			qosCube.setName("reliable");
			qosCube.setOrder(true);
			qosCube.setPartialDelivery(false);
			qosCube.setPeakBandwidthDuration(0);
			qosCube.setPeakSDUBandwidthDuration(0);
			qosCube.setQosId(2);
			qosCube.setUndetectedBitErrorRate(Double.valueOf("1E-09"));
		}
		
		try{
			serializedObject = cdapEnrollmentWorker.getEncoder().encode(qosCube);
			objectValue = new ObjectValue();
			objectValue.setByteval(serializedObject);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		CDAPMessage cdapMessage = cdapSessionManager.getReadObjectResponseMessage(portId, flags,
				"rina.messages.qosCube", 5 + counter, "/dif/management/flowallocator/qoscube", objectValue, 0, null,  invokeId);
		cdapEnrollmentWorker.sendCDAPMessage(cdapMessage);
		
		if (counter == 0){
			counter++;
		}else if (counter == 1){
			counter = 0;
			state = State.DONE;
		}
	}

}
