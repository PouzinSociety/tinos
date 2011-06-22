package rina.encoding.impl.googleprotobuf.flow.tests;

import java.util.List;

import rina.ipcprocess.api.BaseIPCProcess;

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

}
