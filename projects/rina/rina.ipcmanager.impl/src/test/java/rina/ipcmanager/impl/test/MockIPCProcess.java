package rina.ipcmanager.impl.test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import rina.ipcmanager.impl.test.APNotifier.Status;
import rina.ipcprocess.api.BaseIPCProcess;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCException;
import rina.ipcservice.api.IPCService;

public class MockIPCProcess extends BaseIPCProcess implements IPCService{
	
	private int portId = 0;
	private FlowService flowService = null;
	private APService apService = null;
	
	/**
	 * The thread pool implementation
	 */
	private ExecutorService executorService = null;
	
	public MockIPCProcess(){
		executorService = Executors.newFixedThreadPool(2);
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

	public void register(ApplicationProcessNamingInfo arg0) {
		// TODO Auto-generated method stub
	}

	public void submitAllocateRequest(FlowService flowService, APService apService) throws IPCException {
		Assert.assertEquals(flowService.getDestinationAPNamingInfo().getApplicationProcessName(), "B");
		Assert.assertEquals(flowService.getDestinationAPNamingInfo().getApplicationProcessInstance(), "1");
		Assert.assertEquals(flowService.getSourceAPNamingInfo().getApplicationProcessName(), "A");
		Assert.assertEquals(flowService.getSourceAPNamingInfo().getApplicationProcessInstance(), "1");
		Assert.assertNotNull(flowService.getPortId());
		this.portId = flowService.getPortId();
		this.flowService = flowService;
		this.apService = apService;
		
		System.out.println("Received allocate request from application process A-1 to communicate with application process B-1");
		System.out.println("Assigned portId: "+portId);
		APNotifier notifier = new APNotifier(apService, flowService, Status.ALLOCATE_RESPONSE_OK);
		executorService.execute(notifier);
	}

	public void submitAllocateResponse(int arg0, boolean arg1, String arg2)
			throws IPCException {
		// TODO Auto-generated method stub
	}

	public void submitDeallocateRequest(int portId, APService apService) {
		Assert.assertEquals(portId, this.portId);
		System.out.println("Received deallocate request for portId "+portId);
		
		APNotifier notifier = new APNotifier(apService, flowService, Status.DEALLOCATE_RESPONSE_OK);
		executorService.execute(notifier);
	}

	public void submitStatus(int arg0) {
		// TODO Auto-generated method stub
	}

	public void submitTransfer(int portId, byte[] sdu) throws IPCException {
		Assert.assertEquals(this.portId, portId);
		System.out.println("Got a request for portId "+portId+ " to send the following SDU: " + sdu);
		
		APNotifier notifier = new APNotifier(apService, flowService, Status.DELIVER_TRANSFER);
		executorService.execute(notifier);
	}

	public void unregister(ApplicationProcessNamingInfo arg0) {
		// TODO Auto-generated method stub
	}
}
