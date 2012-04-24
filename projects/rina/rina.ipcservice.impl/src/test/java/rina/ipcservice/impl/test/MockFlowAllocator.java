package rina.ipcservice.impl.test;

import java.net.Socket;

import rina.cdap.api.message.CDAPMessage;
import rina.flowallocator.api.BaseFlowAllocator;
import rina.flowallocator.api.DirectoryForwardingTable;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCException;

public class MockFlowAllocator extends BaseFlowAllocator{

	public void createFlowRequestMessageReceived(CDAPMessage arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	public DirectoryForwardingTable getDirectoryForwardingTable() {
		// TODO Auto-generated method stub
		return null;
	}

	public void newConnectionAccepted(Socket arg0) {
		// TODO Auto-generated method stub
		
	}

	public void receivedDeallocateLocalFlowRequest(int arg0)
			throws IPCException {
		// TODO Auto-generated method stub
		
	}

	public void receivedLocalFlowRequest(FlowService arg0, String arg1)
			throws IPCException {
		// TODO Auto-generated method stub
		
	}

	public void receivedLocalFlowResponse(int arg0, int arg1, boolean arg2,
			String arg3) throws IPCException {
		// TODO Auto-generated method stub
		
	}

	public void removeFlowAllocatorInstance(int arg0) {
		// TODO Auto-generated method stub
		
	}

	public int submitAllocateRequest(FlowService arg0) throws IPCException {
		// TODO Auto-generated method stub
		return 0;
	}

	public void submitAllocateResponse(int arg0, boolean arg1, String arg2)
			throws IPCException {
		// TODO Auto-generated method stub
		
	}

	public void submitDeallocate(int arg0) throws IPCException {
		// TODO Auto-generated method stub
		
	}

	public void submitTransfer(int arg0, byte[] arg1) throws IPCException {
		// TODO Auto-generated method stub
		
	}

}
