package rina.cdap.echotarget.enrollment;

import rina.applicationprocess.api.DAFMember;
import rina.applicationprocess.api.WhatevercastName;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.ObjectValue;
import rina.cdap.impl.CDAPSessionManagerImpl;
import rina.cdap.impl.googleprotobuf.GoogleProtocolBufWireMessageProviderFactory;
import rina.delimiting.api.Delimiter;
import rina.delimiting.impl.DIFDelimiter;
import rina.efcp.api.DataTransferConstants;
import rina.encoding.impl.EncoderImpl;
import rina.encoding.impl.googleprotobuf.dafmember.DafMemberEncoder;
import rina.encoding.impl.googleprotobuf.datatransferconstants.DataTransferConstantsEncoder;
import rina.encoding.impl.googleprotobuf.flow.FlowEncoder;
import rina.encoding.impl.googleprotobuf.qoscube.QoSCubeEncoder;
import rina.encoding.impl.googleprotobuf.whatevercast.WhatevercastNameEncoder;
import rina.enrollment.impl.EnrollmentTaskImpl;
import rina.flowallocator.api.QoSCube;
import rina.flowallocator.api.message.Flow;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.impl.IPCProcessImpl;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBObjectNames;
import rina.ribdaemon.impl.RIBDaemonImpl;
import rina.rmt.api.RMT;
import rina.rmt.impl.tcp.TCPRMTImpl;

public class EnrollingIPCProcess {
	
	public static void main(String[] args){
		RIBDaemon ribDaemon = new RIBDaemonImpl();
		IPCProcess joiningIPCProcess = new IPCProcessImpl("Joining","1", ribDaemon);
		joiningIPCProcess.addIPCProcessComponent(ribDaemon);
		Delimiter delimiter = new DIFDelimiter();
		joiningIPCProcess.addIPCProcessComponent(delimiter);
		EncoderImpl encoder = new EncoderImpl();
		encoder.addEncoder(DAFMember.class.toString(), new DafMemberEncoder());
		encoder.addEncoder(DataTransferConstants.class.toString(), new DataTransferConstantsEncoder());
		encoder.addEncoder(Flow.class.toString(), new FlowEncoder());
		encoder.addEncoder(QoSCube.class.toString(), new QoSCubeEncoder());
		encoder.addEncoder(WhatevercastName.class.toString(), new WhatevercastNameEncoder());
		joiningIPCProcess.addIPCProcessComponent(encoder);
		RMT rmt = new TCPRMTImpl();
		joiningIPCProcess.addIPCProcessComponent(rmt);
		CDAPSessionManagerImpl cdapSessionManager = new CDAPSessionManagerImpl();
		cdapSessionManager.setWireMessageProviderFactory(new GoogleProtocolBufWireMessageProviderFactory());
		joiningIPCProcess.addIPCProcessComponent(cdapSessionManager);
		EnrollmentTaskImpl enrollmentTask = new EnrollmentTaskImpl();
		joiningIPCProcess.addIPCProcessComponent(enrollmentTask);
		
		try{
			ObjectValue objectValue = new ObjectValue();
			DAFMember newMember = new DAFMember();
			newMember.setApplicationProcessName("i2CAT-Barcelona");
			newMember.setApplicationProcessInstance("1");
			byte[] encodedObject = encoder.encode(newMember);
			objectValue.setByteval(encodedObject);
			CDAPMessage cdapMessage = cdapSessionManager.getCreateObjectRequestMessage(123456, null, null, "class", 0, RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.ENROLLMENT + RIBObjectNames.SEPARATOR + RIBObjectNames.MEMBERS + RIBObjectNames.SEPARATOR + "1", objectValue, 0, true);

			enrollmentTask.initiateEnrollment(cdapMessage, null);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}

}
