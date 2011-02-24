package rina.efcp.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import rina.efcp.api.DataTransferAE;
import rina.efcp.api.DataTransferAEInstance;
import rina.flowallocator.api.Connection;
import rina.ipcprocess.api.IPCProcess;

/**
 * Simple implementation of DataTransferAEFactory
 * @author eduardgrasa
 *
 */
public class DataTransferAEImpl implements DataTransferAE{
	
	/**
	 * A pointer to the IPC process
	 */
	private IPCProcess ipcProcess = null;
	
	/**
	 * Stores all the instantiated data transfer application entities
	 */
	private Map<Connection, DataTransferAEInstance> dataTransferAEInstances = null;
	
	public DataTransferAEImpl(){
		dataTransferAEInstances = new HashMap<Connection, DataTransferAEInstance>();
	}

	public void setIPCProcess(IPCProcess ipcProcess) {
		this.ipcProcess = ipcProcess;
	}

	public Map<Connection, DataTransferAEInstance> getDataTransferAEInstances() {
		return dataTransferAEInstances;
	}

	public void setDataTransferAEInstances(
			Map<Connection, DataTransferAEInstance> dataTransferAEInstances) {
		this.dataTransferAEInstances = dataTransferAEInstances;
	}

	public DataTransferAEInstance createDataTransferAEInstance(Connection connection) {
		if (dataTransferAEInstances.containsKey(connection)){
			throw new RuntimeException("This connection already has a running data transfer AE instance attached");
		}
		DataTransferAEInstanceImpl dataTransferAEInstance = new DataTransferAEInstanceImpl(connection);
		dataTransferAEInstance.setIPCProcess(ipcProcess);
		dataTransferAEInstances.put(connection, dataTransferAEInstance);
		
		return dataTransferAEInstance;
	}

	public void destroyDataTransferAEInstance(Connection connection) {
		DataTransferAEInstance dataTransferAEInstance = dataTransferAEInstances.remove(connection);
		if (dataTransferAEInstance == null){
			throw new RuntimeException("This connection doesn't have an associated running data transfer AE instance");
		}
		//TODO clear any stuff the data transfer AE instance may have instantiated, like threads or so
	}

	public DataTransferAEInstance getDataTransferAEInstance(Connection connection) {
		return dataTransferAEInstances.get(connection);
		
	}

	/**
	 * Return the dataTransferAEInstance currently associated to a portId
	 * @param portId
	 */
	public DataTransferAEInstance getDataTransferAEInstance(int portId) {
		Iterator<Connection> iterator = dataTransferAEInstances.keySet().iterator();
		Connection connection = null;
		
		while(iterator.hasNext()){
			connection = iterator.next();
			if (connection.getSourcePortId().getValue() == portId){
				return dataTransferAEInstances.get(connection);
			}
		}
		
		return null;
	}

}
