package rina.encoding.impl.googleprotobuf.flow.tests;

import java.util.List;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.Opcode;
import rina.ipcprocess.api.BaseIPCProcess;
import rina.ribdaemon.api.RIBDaemonException;

public class FakeIPCProcess extends BaseIPCProcess{
	
	public FakeIPCProcess(){
		this.addIPCProcessComponent(new FakeDataTransferAE());
	}
	
	public void deliverDeallocateRequestToApplicationProcess(int arg0) {
		// TODO Auto-generated method stub
		
	}

	public void deliverSDUsToApplicationProcess(List<byte[]> arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	public void processOperation(CDAPMessage arg0, CDAPSessionDescriptor arg1)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public Object processOperation(Opcode arg0, String arg1, String arg2,
			long arg3, Object arg4) throws RIBDaemonException {
		// TODO Auto-generated method stub
		return null;
	}

}
