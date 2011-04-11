package rina.utils.serialization.googleprotobuf.flow.tests;

import rina.efcp.api.DataTransferAE;
import rina.efcp.api.DataTransferAEInstance;
import rina.efcp.api.DataTransferConstants;
import rina.flowallocator.api.Connection;
import rina.ipcprocess.api.IPCProcess;

public class FakeDataTransferAE implements DataTransferAE{

	private DataTransferConstants dataTransferConstants = new DataTransferConstants();
	
	public void setIPCProcess(IPCProcess arg0) {
		// TODO Auto-generated method stub
		
	}

	public DataTransferAEInstance createDataTransferAEInstance(Connection arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void destroyDataTransferAEInstance(Connection arg0) {
		// TODO Auto-generated method stub
		
	}

	public DataTransferAEInstance getDataTransferAEInstance(Connection arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public DataTransferAEInstance getDataTransferAEInstance(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public DataTransferConstants getDataTransferConstants() {
		return dataTransferConstants;
	}

	public void setDataTransferConstants(DataTransferConstants arg0) {
		// TODO Auto-generated method stub
		
	}

}
