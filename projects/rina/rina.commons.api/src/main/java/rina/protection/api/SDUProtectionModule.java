package rina.protection.api;

import rina.ipcservice.api.IPCException;

/**
 * An instantiation of an SDU protection module. Can protect 
 * an SDU by prepending some bytes, and unprotect the SDU by 
 * removing them. SDU Protection comprises integrity, confidentiality 
 * and compression.
 * 
 * @author eduardgrasa
 *
 */
public interface SDUProtectionModule {
	
	public static final String NULL = "NULL";
	
	/**
	 * Return the type of the SDU Protection Module
	 * @return
	 */
	public String getType();

	/**
	 * Protects an SDU before being sent through an N-1 flow
	 * @param sdu
	 * @return the protected SDU
	 * @throws IPCException if there is an issue protecting the SDU
	 */
	public byte[] protectSDU(byte[] sdu) throws IPCException;
	
	/**
	 * Unprotects an SDU after receiving it from an N-1 flow
	 * @param sdu
	 * @return the unprotected SDU
	 * @throws IPCException if there is an issue unprotecting the SDU
	 */
	public byte[] unprotectSDU(byte[] sdu) throws IPCException;
}
