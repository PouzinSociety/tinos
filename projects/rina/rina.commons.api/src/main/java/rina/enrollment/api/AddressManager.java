package rina.enrollment.api;

import rina.ipcservice.api.IPCException;

/**
 * Manages the allocation, deallocaiton and usage of addresses within the DIF
 * @author eduardgrasa
 *
 */
public interface AddressManager {
	
	/**
	 * Returns an available address for a remote process that is joining the DIF.
	 * @return the available address
	 * @throw IPCException if there are no available addresses
	 */
	public long getAvailableAddress() throws IPCException;
	
	/**
	 * True if an address is in use, false otherwise
	 * @param address
	 * @return
	 */
	public boolean addressInUse(long address);

}
