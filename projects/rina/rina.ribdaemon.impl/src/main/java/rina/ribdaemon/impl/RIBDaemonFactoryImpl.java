package rina.ribdaemon.impl;

import java.util.HashMap;
import java.util.Map;

import rina.ipcservice.api.ApplicationProcessNamingInfo;
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
		ribDaemonRespository.put(ipcProcessNamingInfo.getProcessKey(), ribDaemon);
		return ribDaemon;
	}

	public void destroyRIBDaemon(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		ribDaemonRespository.remove(ipcProcessNamingInfo.getProcessKey());
	}

	public RIBDaemon getRIBDaemon(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		return ribDaemonRespository.get(ipcProcessNamingInfo.getProcessKey());
	}

}
