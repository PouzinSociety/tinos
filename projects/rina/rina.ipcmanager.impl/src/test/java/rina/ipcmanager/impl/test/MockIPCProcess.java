package rina.ipcmanager.impl.test;

import java.util.List;

import junit.framework.Assert;

import rina.ipcprocess.api.BaseIPCProcess;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCException;
import rina.ipcservice.api.IPCService;

public class MockIPCProcess extends BaseIPCProcess implements IPCService{

	public void deliverDeallocateRequestToApplicationProcess(int arg0) {
		// TODO Auto-generated method stub
	}

	public void deliverSDUsToApplicationProcess(List<byte[]> arg0, int arg1) {
		// TODO Auto-generated method stub
	}

	public void destroy() {
		// TODO Auto-generated method stub
	}

	public void register(ApplicationProcessNamingInfo arg0) {
		// TODO Auto-generated method stub
	}

	public void submitAllocateRequest(FlowService flowService, APService apService) throws IPCException {
		Assert.assertEquals(flowService.getDestinationAPNamingInfo().getApplicationProcessName(), "B");
		Assert.assertEquals(flowService.getDestinationAPNamingInfo().getApplicationProcessInstance(), "1");
		Assert.assertEquals(flowService.getSourceAPNamingInfo().getApplicationProcessName(), "A");
		Assert.assertEquals(flowService.getSourceAPNamingInfo().getApplicationProcessInstance(), "1");
		
		System.out.println("Received allocate request from application process A-1 to communicate with application process B-1");
	}

	public void submitAllocateResponse(int arg0, boolean arg1, String arg2)
			throws IPCException {
		// TODO Auto-generated method stub
	}

	public void submitDeallocateRequest(int arg0, APService arg1) {
		// TODO Auto-generated method stub
	}

	public void submitStatus(int arg0) {
		// TODO Auto-generated method stub
	}

	public void submitTransfer(int arg0, byte[] arg1) throws IPCException {
		// TODO Auto-generated method stub
	}

	public void unregister(ApplicationProcessNamingInfo arg0) {
		// TODO Auto-generated method stub
	}
}
