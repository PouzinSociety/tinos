package rina.protection.api;

import rina.ipcservice.api.IPCException;

/**
 * Maintains the implementation of SDU Protection modules
 * @author eduardgrasa
 *
 */
public interface SDUProtectionModuleRepository {

	public static final String NULL = "NULL";
	
	/**
	 * Return an instance of the SDU protection module whose type 
	 * matches the one provided in the operation's argument
	 * @param type the type of SDU Protection module to be returned
	 * @return The instance of the SDU Protection module
	 * @throws IPCException if no instance of an SDU Protection module of a given type exists
	 */
	public SDUProtectionModule getSDUProtectionModule(String type) throws IPCException;
}
