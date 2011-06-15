package rina.encoding.impl.googleprotobuf.flow.tests;

import rina.efcp.api.BaseDataTransferAE;
import rina.efcp.api.DataTransferAEInstance;
import rina.efcp.api.DataTransferConstants;
import rina.flowallocator.api.Connection;

public class FakeDataTransferAE extends BaseDataTransferAE{

	private DataTransferConstants dataTransferConstants = new DataTransferConstants();

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
