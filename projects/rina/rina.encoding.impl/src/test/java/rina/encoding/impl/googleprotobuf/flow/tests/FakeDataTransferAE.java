package rina.encoding.impl.googleprotobuf.flow.tests;

import java.net.Socket;

import rina.efcp.api.BaseDataTransferAE;
import rina.flowallocator.api.ConnectionId;
import rina.flowallocator.api.Flow;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.IPCException;

public class FakeDataTransferAE extends BaseDataTransferAE{

	@Override
	public void createConnectionAndBindToPortId(Flow arg0, Socket socket, APService applicationCallback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteConnection(ConnectionId arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void freeCEPIds(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pduDelivered(byte[] arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int[] reserveCEPIds(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createLocalConnectionAndBindToPortId(int arg0, int arg1, APService applicationCallback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void subscribeToFlow(int arg0) throws IPCException {
		// TODO Auto-generated method stub
		
	}

}
