package rina.protection.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rina.ipcservice.api.IPCException;
import rina.protection.api.SDUProtectionModule;
import rina.protection.api.SDUProtectionModuleRepository;
import rina.protection.impl.modules.NullSDUProtectionModule;

/**
 * The repository of SDU Protection modules, sorted by type
 * @author eduardgrasa
 *
 */
public class SDUProtectionModuleRepositoryImpl implements SDUProtectionModuleRepository{

	private Map<String, SDUProtectionModule> sduProtectionModuleRepository = null;
	
	public SDUProtectionModuleRepositoryImpl(){
		sduProtectionModuleRepository = new ConcurrentHashMap<String, SDUProtectionModule>();
		sduProtectionModuleRepository.put(SDUProtectionModuleRepository.NULL, new NullSDUProtectionModule());
	}
	
	/**
	 * Return an instance of the SDU protection module whose type 
	 * matches the one provided in the operation's argument
	 * @param type the type of SDU Protection module to be returned
	 * @return The instance of the SDU Protection module
	 * @throws IPCException if no instance of an SDU Protection module of a given type exists
	 */
	public SDUProtectionModule getSDUProtectionModule(String type) throws IPCException {
		SDUProtectionModule sduProtectionModule = this.sduProtectionModuleRepository.get(type);
		if (sduProtectionModule == null){
			throw new IPCException(IPCException.ERROR_CODE, "Could not find an SDU Protection Module of type "+type);
		}
		
		return sduProtectionModule;
	}

}
