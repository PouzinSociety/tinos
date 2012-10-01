package rina.protection.impl;

import java.util.HashMap;
import java.util.Map;

import rina.protection.api.SDUProtecionModuleRepositoryFactory;
import rina.protection.api.SDUProtectionModuleRepository;
import rina.applicationprocess.api.ApplicationProcessNamingInfo;

public class SDUProtectionModuleRepositoryFactoryImpl implements SDUProtecionModuleRepositoryFactory{

	private Map<String, SDUProtectionModuleRepository> sduProtectionModuleRepositoryRespositories = null;
	
	public SDUProtectionModuleRepositoryFactoryImpl(){
		sduProtectionModuleRepositoryRespositories = new HashMap<String, SDUProtectionModuleRepository>();
	}
	
	public SDUProtectionModuleRepository createSDUProtecionModuleRepository(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		SDUProtectionModuleRepositoryImpl sduProtectionModuleRepository = null;
		try {
			sduProtectionModuleRepository = new SDUProtectionModuleRepositoryImpl();
		} catch (Exception e) {
			e.printStackTrace();
		}
		synchronized(sduProtectionModuleRepositoryRespositories){
			sduProtectionModuleRepositoryRespositories.put(ipcProcessNamingInfo.getEncodedString(), sduProtectionModuleRepository);
		}
		return sduProtectionModuleRepository;
	}

	public void destroySDUProtecionModuleRepository(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		synchronized(sduProtectionModuleRepositoryRespositories){
			sduProtectionModuleRepositoryRespositories.remove(ipcProcessNamingInfo.getEncodedString());
		}
	}

	public SDUProtectionModuleRepository getSDUProtecionModuleRepository(
			ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		synchronized(sduProtectionModuleRepositoryRespositories){
			return sduProtectionModuleRepositoryRespositories.get(ipcProcessNamingInfo.getEncodedString());
		}
	}

}
