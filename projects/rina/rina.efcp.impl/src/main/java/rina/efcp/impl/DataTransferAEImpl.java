package rina.efcp.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.efcp.api.BaseDataTransferAE;
import rina.efcp.api.DataTransferAEInstance;
import rina.efcp.api.DataTransferConstants;
import rina.efcp.impl.ribobjects.DataTransferConstantsRIBObject;
import rina.flowallocator.api.Connection;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;

/**
 * Simple implementation of DataTransferAEFactory
 * @author eduardgrasa
 *
 */
public class DataTransferAEImpl extends BaseDataTransferAE{
	
	private static final Log log = LogFactory.getLog(DataTransferAEImpl.class);
	
	/**
	 * Stores all the instantiated data transfer application entities
	 */
	private Map<Connection, DataTransferAEInstance> dataTransferAEInstances = null;
	
	/**
	 *  Data Transfer constants associated to the DIF where the IPC process belongs.
	 *  Set when the IPC process joins a DIF.
	 */
	private DataTransferConstants dataTransferConstants = null;
	
	private RIBDaemon ribDaemon = null;
	
	public DataTransferAEImpl(){
		dataTransferAEInstances = new HashMap<Connection, DataTransferAEInstance>();
		dataTransferConstants = new DataTransferConstants();
	}
	
	@Override
	public void setIPCProcess(IPCProcess ipcProcess){
		super.setIPCProcess(ipcProcess);
		this.ribDaemon = (RIBDaemon) getIPCProcess().getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		populateRIB(ipcProcess);
	}
	
	private void populateRIB(IPCProcess ipcProcess){
		try{
			RIBObject ribObject = new DataTransferConstantsRIBObject(ipcProcess, 
					dataTransferConstants);
			ribDaemon.addRIBObject(ribObject);
		}catch(RIBDaemonException ex){
			ex.printStackTrace();
			log.error("Could not subscribe to RIB Daemon:" +ex.getMessage());
		}
	}
	
	public DataTransferConstants getDataTransferConstants(){
		return dataTransferConstants;
	}
	
	public void setDataTransferConstants(DataTransferConstants dataTransferConstants){
		this.dataTransferConstants = dataTransferConstants;
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
		DataTransferAEInstanceImpl dataTransferAEInstance = new DataTransferAEInstanceImpl(connection, dataTransferConstants);
		dataTransferAEInstance.setIPCProcess(getIPCProcess());
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
			if (connection.getSourcePortId() == portId){
				return dataTransferAEInstances.get(connection);
			}
		}
		
		return null;
	}
}
