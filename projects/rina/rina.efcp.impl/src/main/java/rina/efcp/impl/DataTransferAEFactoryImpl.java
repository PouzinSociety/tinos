package rina.efcp.impl;

import java.util.HashMap;
import java.util.Map;

import rina.efcp.api.DataTransferAE;
import rina.efcp.api.DataTransferAEFactory;
import rina.efcp.impl.parsers.PDUParserImpl;
import rina.applicationprocess.api.ApplicationProcessNamingInfo;

public class DataTransferAEFactoryImpl implements DataTransferAEFactory{

	private Map<ApplicationProcessNamingInfo, DataTransferAE> dataTransferAERespository = null;
	
	public DataTransferAEFactoryImpl(){
		dataTransferAERespository = new HashMap<ApplicationProcessNamingInfo, DataTransferAE>();
	}
	
	public DataTransferAE createDataTransferAE(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		DataTransferAEImpl dataTransferAE = new DataTransferAEImpl();
		dataTransferAE.setPDUParser(new PDUParserImpl());
		dataTransferAERespository.put(ipcProcessNamingInfo, dataTransferAE);
		return dataTransferAE;
	}

	public void destroyDataTransferAE(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		dataTransferAERespository.remove(ipcProcessNamingInfo);
	}

	public DataTransferAE getDataTransferAE(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		return dataTransferAERespository.get(ipcProcessNamingInfo);
	}

}
