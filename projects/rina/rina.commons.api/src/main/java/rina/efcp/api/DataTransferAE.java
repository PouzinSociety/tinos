package rina.efcp.api;

import rina.flowallocator.api.Connection;
import rina.ipcprocess.api.IPCProcessComponent;

/**
 * Creates and manages the lifecycle of DataTrasnferAEInstances
 * @author eduardgrasa
 *
 */
public interface DataTransferAE extends IPCProcessComponent {

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
	
	/**
	 * Get the instance of the data transfer AE associated to this port Id
	 * @param portId
	 * @return
	 */
	public DataTransferAEInstance getDataTransferAEInstance(int portId);
	
	/**
	 * Data Transfer constants associated to the DIF where the IPC process belongs
	 * @return
	 */
	public DataTransferConstants getDataTransferConstants();
	
	/**
	 * Data Transfer constants associated to the DIF where the IPC process belongs
	 * @param dataTransferConstants
	 */
	public void setDataTransferConstants(DataTransferConstants dataTransferConstants);
}
