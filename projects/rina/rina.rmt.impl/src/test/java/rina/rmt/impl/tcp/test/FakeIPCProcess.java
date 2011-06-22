package rina.rmt.impl.tcp.test;

import java.util.List;

import rina.delimiting.api.DelimiterFactory;
import rina.delimiting.impl.DelimiterFactoryImpl;
import rina.ipcprocess.api.BaseIPCProcess;

public class FakeIPCProcess extends BaseIPCProcess {
	
	public FakeIPCProcess(){
		this.addIPCProcessComponent(new FakeRIBDaemon());
		DelimiterFactory delimiterFactory = new DelimiterFactoryImpl();
		this.addIPCProcessComponent(delimiterFactory.createDelimiter(DelimiterFactory.DIF));
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
