package rina.ribdaemon.impl;

import rina.cdap.api.message.CDAPMessage;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.MessageSubscriber;
import rina.ribdaemon.api.MessageSubscription;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.UpdateStrategy;

public class RIBDaemonImpl implements RIBDaemon{

	public void setIPCProcess(IPCProcess arg0) {
		// TODO Auto-generated method stub
		
	}

	public void cdapMessageDelivered(byte[] arg0) {
		// TODO Auto-generated method stub
		
	}

	public Object read(String arg0, long arg1, String arg2, Object arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	public void remove(String arg0, long arg1, String arg2)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void sendMessages(CDAPMessage[] arg0, UpdateStrategy arg1) {
		// TODO Auto-generated method stub
		
	}

	public void subscribeToMessages(MessageSubscription arg0,
			MessageSubscriber arg1) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void unsubscribeFromMessages(MessageSubscription arg0,
			MessageSubscriber arg1) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void write(String arg0, long arg1, String arg2, Object arg3)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

}
