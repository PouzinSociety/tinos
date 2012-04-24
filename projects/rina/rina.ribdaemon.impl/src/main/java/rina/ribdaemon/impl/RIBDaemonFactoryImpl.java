package rina.ribdaemon.impl;

import java.util.HashMap;
import java.util.Map;

import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonFactory;

public class RIBDaemonFactoryImpl implements RIBDaemonFactory{

	private Map<String, RIBDaemon> ribDaemonRespository = null;
	
	public RIBDaemonFactoryImpl(){
		ribDaemonRespository = new HashMap<String, RIBDaemon>();
	}
	
	public RIBDaemon createRIBDaemon(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		RIBDaemon ribDaemon = null;
		try {
			ribDaemon = new RIBDaemonImpl();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ribDaemonRespository.put(ipcProcessNamingInfo.getEncodedString(), ribDaemon);
		return ribDaemon;
	}

	public void destroyRIBDaemon(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		ribDaemonRespository.remove(ipcProcessNamingInfo.getEncodedString());
	}

	public RIBDaemon getRIBDaemon(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		return ribDaemonRespository.get(ipcProcessNamingInfo.getEncodedString());
	}

}
