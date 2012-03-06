package rina.ipcservice.impl.test;

import rina.efcp.api.DataTransferAE;
import rina.efcp.api.DataTransferAEFactory;
import rina.ipcservice.api.ApplicationProcessNamingInfo;

public class MockDataTransferAEFactory implements DataTransferAEFactory{

	private DataTransferAE mockDataTransferAE = null;
	
	public DataTransferAE createDataTransferAE(ApplicationProcessNamingInfo arg0) {
		mockDataTransferAE = new MockDataTransferAE();
		return mockDataTransferAE;
	}

	public void destroyDataTransferAE(ApplicationProcessNamingInfo arg0) {
		mockDataTransferAE = null;
	}

	public DataTransferAE getDataTransferAE(ApplicationProcessNamingInfo arg0) {
		return mockDataTransferAE;
	}

}
