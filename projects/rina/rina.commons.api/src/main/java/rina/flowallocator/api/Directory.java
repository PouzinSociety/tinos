package rina.flowallocator.api;

import rina.ipcservice.api.ApplicationProcessNamingInfo;

/**
 * The directory. Maps application process names to IPC process addresses
 * @author eduardgrasa
 *
 */
public interface Directory {
	
	/**
	 * Returns the address of the IPC process where the application process is, or 
	 * null otherwise
	 * @param apNamingInfo
	 * @return
	 */
	public byte[] getAddress(ApplicationProcessNamingInfo apNamingInfo);
	
	/**
	 * Add a new entry to the directory (AP name to IPC process address mapping). If this AP Name was 
	 * mapped to another IPC process address in another entry, drop it. If it was mapped to the same 
	 * IPC process address, extend the lifetime of the entry.
	 * @param apNamingInfo
	 * @param address
	 */
	public void addEntry(ApplicationProcessNamingInfo apNamingInfo, byte[] address);
	
	/**
	 * Remove the entries associated to this AP name
	 * @param apNamingInfo
	 */
	public void removeEntry(ApplicationProcessNamingInfo apNamingInfo);
	
	/**
	 * Remove the entries associated to this address
	 * @param address
	 */
	public void removeEntry(byte[] address);
}
