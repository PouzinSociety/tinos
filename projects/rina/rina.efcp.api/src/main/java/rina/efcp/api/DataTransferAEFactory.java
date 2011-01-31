package rina.efcp.api;

import rina.flowallocator.api.Connection;

/**
 * Creates and manages the lifecycle of DataTrasnferAEInstances
 * @author eduardgrasa
 *
 */
public interface DataTransferAEFactory {

	/**
	 * Create a new instance of a data transfer AE (called by the FA or FAI)
	 * @param connection
	 * @return
	 */
	public DataTransferAEInstance createDataTransferAEInstance(Connection connection);
	
	/**
	 * Destroy the instance of the data transfer AE associated to this connection
	 * @param connection
	 */
	public void destroyDataTransferAEInstance(Connection connection);
	
	/**
	 * Get the instance of the data transfer AE associated to this connection
	 * @param connection
	 */
	public DataTransferAEInstance getDataTransferAEInstance(Connection connection);
}
