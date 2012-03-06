package rina.ipcservice.impl.test;

import java.util.List;

import rina.cdap.api.CDAPMessageHandler;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.UpdateStrategy;

public class MockRIBDaemon extends BaseRIBDaemon{

	public void addRIBObject(RIBObject arg0) throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void cdapMessageDelivered(byte[] arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	public void create(String arg0, String arg1, long arg2, Object arg3)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void delete(String arg0, String arg1, long arg2)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void flowDeallocated(int arg0) {
		// TODO Auto-generated method stub
		
	}

	public List<RIBObject> getRIBObjects() {
		// TODO Auto-generated method stub
		return null;
	}

	public void processOperation(CDAPMessage arg0, CDAPSessionDescriptor arg1)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public RIBObject read(String arg0, String arg1, long arg2)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeRIBObject(RIBObject arg0, String arg1)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void sendMessage(CDAPMessage arg0, int arg1, CDAPMessageHandler arg2)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void sendMessages(CDAPMessage[] arg0, UpdateStrategy arg1) {
		// TODO Auto-generated method stub
		
	}

	public void start(String arg0, String arg1, long arg2, Object arg3)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void stop(String arg0, String arg1, long arg2, Object arg3)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void write(String arg0, String arg1, long arg2, Object arg3)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

}
