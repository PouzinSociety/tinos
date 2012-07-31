package rina.ipcservice.impl.test;

import java.net.Socket;

import rina.efcp.api.BaseDataTransferAE;
import rina.flowallocator.api.ConnectionId;
import rina.flowallocator.api.Flow;
import rina.ipcservice.api.APService;

public class MockDataTransferAE extends BaseDataTransferAE{

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
	public void createConnectionAndBindToPortId(Flow arg0, Socket arg1, APService ap2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createLocalConnectionAndBindToPortId(int arg0, int arg1, APService arg2) {
		// TODO Auto-generated method stub
		
	}

}
