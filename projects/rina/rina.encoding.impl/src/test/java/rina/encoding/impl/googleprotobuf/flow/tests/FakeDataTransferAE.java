package rina.encoding.impl.googleprotobuf.flow.tests;

import java.net.Socket;

import rina.efcp.api.BaseDataTransferAE;
import rina.flowallocator.api.ConnectionId;
import rina.flowallocator.api.Flow;

public class FakeDataTransferAE extends BaseDataTransferAE{

	@Override
	public void createConnectionAndBindToPortId(Flow arg0, Socket socket) {
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
	public void postSDU(int arg0, byte[] arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int[] reserveCEPIds(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
