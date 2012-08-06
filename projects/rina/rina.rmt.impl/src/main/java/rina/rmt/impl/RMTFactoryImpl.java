package rina.rmt.impl;

import java.util.HashMap;
import java.util.Map;

import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.rmt.api.RMT;
import rina.rmt.api.RMTFactory;

public class RMTFactoryImpl implements RMTFactory{

	private Map<String, RMT> rmtRespository = null;
	
	public RMTFactoryImpl(){
		rmtRespository = new HashMap<String, RMT>();
	}
	
	public RMT createRMT(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		RMT rmt = null;
		try {
			rmt = new RMTImpl();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rmtRespository.put(ipcProcessNamingInfo.getEncodedString(), rmt);
		return rmt;
	}

	public void destroyRMT(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		RMT rmt = rmtRespository.remove(ipcProcessNamingInfo.getEncodedString());
		rmt.stop();
	}

	public RMT getRMT(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		return rmtRespository.get(ipcProcessNamingInfo.getEncodedString());
	}

}
