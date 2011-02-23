package rina.efcp.impl.test;

import java.util.List;

import rina.efcp.api.DataTransferAE;
import rina.flowallocator.api.FlowAllocator;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ribdaemon.api.RIBDaemon;
import rina.rmt.api.RMT;

public class FakeIPCProcess implements IPCProcess{
	
	private RMT rmt = null;

	public void deliverSDUsToApplicationProcess(List<byte[]> sdus, int portId) {
		System.out.println("Delivering sdus to port " + portId);
		
		for(int i=0; i<sdus.size(); i++){
			System.out.println(new String(sdus.get(i)));
		}
	}

	public DataTransferAE getDataTransferAE() {
		// TODO Auto-generated method stub
		return null;
	}

	public FlowAllocator getFlowAllocator() {
		// TODO Auto-generated method stub
		return null;
	}

	public ApplicationProcessNamingInfo getIPCProcessNamingInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public RIBDaemon getRibDaemon() {
		// TODO Auto-generated method stub
		return null;
	}

	public RMT getRmt() {
		return rmt;
	}

	public void setDataTransferAE(DataTransferAE arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setFlowAllocator(FlowAllocator arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setIPCProcessNamingInfo(ApplicationProcessNamingInfo arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setRibDaemon(RIBDaemon arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setRmt(RMT rmt) {
		this.rmt = rmt;
	}

	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
