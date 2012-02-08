package rina.efcp.impl.test;

import java.util.List;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.ipcprocess.api.BaseIPCProcess;
import rina.ribdaemon.api.RIBDaemonException;

public class FakeIPCProcess extends BaseIPCProcess{

	public void deliverSDUsToApplicationProcess(List<byte[]> sdus, int portId) {
		System.out.println("Delivering sdus to port " + portId);
		
		for(int i=0; i<sdus.size(); i++){
			System.out.println(new String(sdus.get(i)));
		}
	}

	public void deliverDeallocateRequestToApplicationProcess(int arg0) {
		// TODO Auto-generated method stub
		
	}

	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	public void cancelRead(CDAPMessage arg0, CDAPSessionDescriptor arg1)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void cancelRead(String arg0, String arg1, long arg2, Object arg3)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void create(CDAPMessage arg0, CDAPSessionDescriptor arg1)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void create(String arg0, String arg1, long arg2, Object arg3)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void delete(CDAPMessage arg0, CDAPSessionDescriptor arg1)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void delete(String arg0, String arg1, long arg2, Object arg3)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void read(CDAPMessage arg0, CDAPSessionDescriptor arg1)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public Object read(String arg0, String arg1, long arg2)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		return null;
	}

	public void start(CDAPMessage arg0, CDAPSessionDescriptor arg1)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void start(String arg0, String arg1, long arg2, Object arg3)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void stop(CDAPMessage arg0, CDAPSessionDescriptor arg1)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void stop(String arg0, String arg1, long arg2, Object arg3)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void write(CDAPMessage arg0, CDAPSessionDescriptor arg1)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public void write(String arg0, String arg1, long arg2, Object arg3)
			throws RIBDaemonException {
		// TODO Auto-generated method stub
		
	}

	public Long getAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	public void execute(Runnable arg0) {
		// TODO Auto-generated method stub
		
	}

	public String getApplicationProcessInstance() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getApplicationProcessName() {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
