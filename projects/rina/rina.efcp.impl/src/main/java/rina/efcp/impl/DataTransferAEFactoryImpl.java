package rina.efcp.impl;

import java.util.HashMap;
import java.util.Map;

import rina.efcp.api.DataTransferAEFactory;
import rina.efcp.api.DataTransferAEInstance;
import rina.efcp.api.SDUCollector;
import rina.flowallocator.api.Connection;
import rina.rmt.api.RMT;

/**
 * Simple implementation of DataTransferAEFactory
 * @author eduardgrasa
 *
 */
public class DataTransferAEFactoryImpl implements DataTransferAEFactory{
	
	/**
	 * A pointer to the relaying and multiplexing task
	 */
	private RMT rmt = null;
	
	/**
	 * A pointer to the SDU collector of the IPC process. The SDU collector is called 
	 * when one or more SDUs are ready to be delivered to a certain port Id.
	 * It is the responsibility of the SDU collector to call the application 
	 * and pass it the complete SDUs
	 */
	private SDUCollector sduCollector = null;
	
	/**
	 * Stores all the instantiated data transfer application entities
	 */
	private Map<Connection, DataTransferAEInstance> dataTransferAEInstances = null;
	
	public DataTransferAEFactoryImpl(){
		dataTransferAEInstances = new HashMap<Connection, DataTransferAEInstance>();
	}
	
	public RMT getRmt() {
		return rmt;
	}

	public void setRmt(RMT rmt) {
		this.rmt = rmt;
	}
	
	public SDUCollector getSduCollector() {
		return sduCollector;
	}

	public void setSduCollector(SDUCollector sduCollector) {
		this.sduCollector = sduCollector;
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
		dataTransferAEInstance.setRmt(rmt);
		dataTransferAEInstance.setSduCollector(sduCollector);
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

}
