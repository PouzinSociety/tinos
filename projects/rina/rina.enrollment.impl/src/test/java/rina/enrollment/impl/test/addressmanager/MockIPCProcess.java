package rina.enrollment.impl.test.addressmanager;

import java.util.ArrayList;
import java.util.List;

import rina.efcp.api.DataTransferConstants;
import rina.enrollment.api.Neighbor;
import rina.ipcprocess.api.BaseIPCProcess;

public class MockIPCProcess extends BaseIPCProcess {
	
	List<Neighbor> members = null;
	
	public MockIPCProcess(){
		members = new ArrayList<Neighbor>();
	}

	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	public void execute(Runnable arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void addDAFMember(Neighbor dafMember){
		members.add(dafMember);
	}
	
	@Override
	public Long getAddress(){
		return 1L;
	}
	
	@Override
	public List<Neighbor> getNeighbors(){
		return members;
	}
	
	@Override
	public DataTransferConstants getDataTransferConstants(){
		DataTransferConstants result = new DataTransferConstants();
		result.setAddressLength(2);
		return result;
	}

}
