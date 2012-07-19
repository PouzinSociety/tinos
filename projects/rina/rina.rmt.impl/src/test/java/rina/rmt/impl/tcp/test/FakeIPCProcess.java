package rina.rmt.impl.tcp.test;

import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.delimiting.api.DelimiterFactory;
import rina.delimiting.impl.DelimiterFactoryImpl;
import rina.ipcprocess.api.BaseIPCProcess;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCException;

public class FakeIPCProcess extends BaseIPCProcess {
	
	public FakeIPCProcess(){
		this.addIPCProcessComponent(new FakeRIBDaemon());
		DelimiterFactory delimiterFactory = new DelimiterFactoryImpl();
		this.addIPCProcessComponent(delimiterFactory.createDelimiter(DelimiterFactory.DIF));
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execute(Runnable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void register(ApplicationProcessNamingInfo arg0, APService arg1)
			throws IPCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int submitAllocateRequest(FlowService arg0, APService arg1)
			throws IPCException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void submitAllocateResponse(int arg0, boolean arg1, String arg2,
			APService arg3) throws IPCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void submitDeallocate(int arg0) throws IPCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unregister(ApplicationProcessNamingInfo arg0)
			throws IPCException {
		// TODO Auto-generated method stub
		
	}
	
	
}
