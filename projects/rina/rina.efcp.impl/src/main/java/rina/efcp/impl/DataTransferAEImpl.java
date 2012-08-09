package rina.efcp.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.aux.BlockingQueueWithSubscriptor;
import rina.configuration.RINAConfiguration;
import rina.efcp.api.BaseDataTransferAE;
import rina.efcp.api.DataTransferConstants;
import rina.efcp.api.PDU;
import rina.efcp.impl.ribobjects.DataTransferConstantsRIBObject;
import rina.events.api.Event;
import rina.events.api.events.EFCPConnectionCreatedEvent;
import rina.events.api.events.EFCPConnectionDeletedEvent;
import rina.flowallocator.api.ConnectionId;
import rina.flowallocator.api.Flow;
import rina.ipcmanager.api.IPCManager;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.IPCException;
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
	 *  Data Transfer constants associated to the DIF where the IPC process belongs.
	 *  Set when the IPC process joins a DIF.
	 */
	private DataTransferConstants dataTransferConstants = null;
	
	/**
	 * An instance of the RIB Daemon
	 */
	private RIBDaemon ribDaemon = null;
	
	/**
	 * Contains the mapping of portIds to reservedCEPIds
	 */
	private Map<Integer, int[]> reservedCEPIds = null;
	
	/**
	 * The list of reservedCEPids
	 */
	private List<Integer> reservedCEPIdList = null;
	
	/**
	 * The mappings of portId to connection
	 */
	private Map<Integer, DTAEIState> portIdToConnectionMapping = null;
	
	/**
	 * Contains the connection states indexed by CEP id
	 */
	private Map<Long, DTAEIState> connectionStatesByConnectionId = null;
	
	/**
	 * The thread that will read and process SDUs from outgoing flows
	 * (incoming from this IPC Process point of view)
	 */
	private OutgoingFlowQueuesReader outgoingFlowQueuesReader = null;
	
	/**
	 * The thread that will read and process PDUs from incoming EFCP connections 
	 * and deliver them to the right N-portId
	 */
	private IncomingEFCPQueuesReader incomingEFCPQueuesReader = null;
	
	/**
	 * The IPC Manager
	 */
	private IPCManager ipcManager = null;
	
	/**
	 * The incoming connection queues of all the EFCP connections in this IPC Process 
	 */
	private Map<Long, BlockingQueueWithSubscriptor<PDU>> incomingConnectionQueues = null;
	
	/**
	 * The outgoing connection queues of all the EFCP connections in this IPC Process 
	 */
	private Map<Long, BlockingQueueWithSubscriptor<PDU>> outgoingConnectionQueues = null;
	
	public DataTransferAEImpl(){
		this.reservedCEPIds = new ConcurrentHashMap<Integer, int[]>();
		this.reservedCEPIdList = new ArrayList<Integer>();
		this.portIdToConnectionMapping = new ConcurrentHashMap<Integer, DTAEIState>();
		this.connectionStatesByConnectionId = new ConcurrentHashMap<Long, DTAEIState>();
		this.dataTransferConstants = new DataTransferConstants();
		this.incomingConnectionQueues = new ConcurrentHashMap<Long, BlockingQueueWithSubscriptor<PDU>>();
		this.outgoingConnectionQueues = new ConcurrentHashMap<Long, BlockingQueueWithSubscriptor<PDU>>();
	}
	
	@Override
	public void setIPCProcess(IPCProcess ipcProcess){
		super.setIPCProcess(ipcProcess);
		this.ipcManager = ipcProcess.getIPCManager();
		this.ribDaemon = (RIBDaemon) ipcProcess.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		populateRIB(ipcProcess);
		this.dataTransferConstants = ipcProcess.getDataTransferConstants();
		this.outgoingFlowQueuesReader = new OutgoingFlowQueuesReader(ipcProcess.getIPCManager(), 
				portIdToConnectionMapping, this);
		this.incomingEFCPQueuesReader = new IncomingEFCPQueuesReader(ipcManager, connectionStatesByConnectionId, this);
		ipcManager.execute(this.outgoingFlowQueuesReader);
		ipcManager.execute(this.incomingEFCPQueuesReader);
	}
	
	@Override
	public void stop(){
		super.stop();
		this.outgoingFlowQueuesReader.stop();
	}
	
	/**
	 * Subscribe to the outgoing flow queue identified by portId
	 * @param portId the id of the incoming flow queue
	 */
	public void subscribeToFlow(int portId) throws IPCException{
		this.ipcManager.getOutgoingFlowQueue(portId).subscribeToQueue(this.outgoingFlowQueuesReader);
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
	
	public Map<Integer, int[]> getReservedCEPIds(){
		return this.reservedCEPIds;
	}
	
	/**
	 * Reserve a number of CEP ids (connection endpoint ids) that will be used during the lifetime
	 * of a flow (identified by portId).
	 * @param numberOfCEPIds The number of CEP ids to reserve
	 * @param portId the portId of the flow that will use these CEP ids
	 * @return null if no available CEPids or if the portId already has CEP ids allocated
	 */
	public int[] reserveCEPIds(int numberOfCEPIds, int portId){
		synchronized(this.reservedCEPIds){
			if (this.reservedCEPIds.get(new Integer(portId)) != null){
				return null;
			}
			
			int[] result = new int[numberOfCEPIds];
			for(int i=0; i<result.length; i++){
				result[i] = this.getAvailableCEPId();
				if (result[i] == -1){
					return null;
				}else{
					this.reservedCEPIdList.add(new Integer(result[i]));
				}
			}
			
			this.reservedCEPIds.put(new Integer(portId), result);
			return result;
		}
	}
	
	/**
	 * Return an available CEP id
	 * @return an available CEP id or -1 if none is available
	 */
	private Integer getAvailableCEPId(){
		//TODO replace Integer.MAX_VALUE for the maximum value of CEPid
		for(int i=1; i<Integer.MAX_VALUE; i++){
			if (this.reservedCEPIdList.contains(new Integer(i))){
				continue;
			}else{
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * Free the CEP ids (connection endpoint ids) associated to a flow identified by portId
	 * Delete the binding between the port id and the connection
	 * @param portId
	 */
	public void freeCEPIds(int portId){
		synchronized(this.reservedCEPIds){
			int[] cepIds = this.reservedCEPIds.remove(new Integer(portId));
			if (cepIds != null){
				for(int i=0; i<cepIds.length; i++){
					this.reservedCEPIdList.remove(new Integer(cepIds[i]));
				}
			}
		}
		
		synchronized(this.portIdToConnectionMapping){
			this.portIdToConnectionMapping.remove(new Integer(portId));
		}
	}
	
	/**
	 * Initialize the state of a new connection, and bind it to the portId (all the SDUs delivered 
	 * to the portId by an application will be sent through this connection)
	 * @param flow the flow object, describing the service supported by this connection
	 */
	public synchronized void createConnectionAndBindToPortId(Flow flow){
		DTAEIState state = new DTAEIState(flow, this.dataTransferConstants);
		
		int portId = 0;
		if (flow.isSource()){
			portId = (int) flow.getSourcePortId(); 
		}else{
			portId = (int) flow.getDestinationPortId();
		}
		
		Long connectionEndpointId = null;
		ConnectionId connectionId = flow.getConnectionIds().get(flow.getCurrentConnectionIdIndex());
		if (flow.isSource()){
			connectionEndpointId = new Long(connectionId.getSourceCEPId());
		}else{
			connectionEndpointId = new Long(connectionId.getDestinationCEPId());
		}
		this.connectionStatesByConnectionId.put(connectionEndpointId, state);
		this.incomingConnectionQueues.put(connectionEndpointId, 
				new BlockingQueueWithSubscriptor<PDU>(connectionEndpointId.intValue(), RINAConfiguration.getInstance().getLocalConfiguration().getLengthOfFlowQueues()));
		this.outgoingConnectionQueues.put(connectionEndpointId, 
				new BlockingQueueWithSubscriptor<PDU>(connectionEndpointId.intValue(), RINAConfiguration.getInstance().getLocalConfiguration().getLengthOfFlowQueues()));
		this.portIdToConnectionMapping.put(new Integer(portId), state);
		try{
			this.getIncomingConnectionQueue(connectionEndpointId.longValue()).subscribeToQueue(this.incomingEFCPQueuesReader);
		}catch(Exception ex){
			log.error("Problems subscribing to incoming EFCP Connection queue identified by connectionEndpoint Id "+connectionEndpointId, ex);
		}
		Event event = new EFCPConnectionCreatedEvent(connectionEndpointId.longValue());
		this.ribDaemon.deliverEvent(event);
	}
	
	/**
	 * Initialize the state of a new local connection and bind it to the portId
	 * @param portId
	 * @param remotePortId
	 * @param applicationCallback the callback to the application, used to deliver the data
	 */
	public void createLocalConnectionAndBindToPortId(int portId, int remotePortId, APService applicationCallback){
		DTAEIState state = new DTAEIState(portId, remotePortId);
		state.setApplicationCallback(applicationCallback);
		this.portIdToConnectionMapping.put(new Integer(portId), state);
	}
	
	/**
	 * Destroy the instance of the data transfer AE associated to this connection endpoint Id
	 * @param connection endpoint id
	 */
	public synchronized void deleteConnection(long connectionEndpointId){
		Long id = new Long(connectionEndpointId);
		this.connectionStatesByConnectionId.remove(id);
		this.outgoingConnectionQueues.remove(id);
		this.incomingConnectionQueues.remove(id);
		Event event = new EFCPConnectionDeletedEvent(connectionEndpointId);
		this.ribDaemon.deliverEvent(event);
	}

	/**
	 * Get the incoming queue that supports the connection identified by connectionEndpointId
	 * @param connectionId
	 * @return
	 * @throws IPCException if there is no incoming queue associated to connectionEndpointId
	 */
	public BlockingQueueWithSubscriptor<PDU> getIncomingConnectionQueue(long connectionEndpointId) throws IPCException{
		BlockingQueueWithSubscriptor<PDU> result = this.incomingConnectionQueues.get(new Long(connectionEndpointId));
		if (result == null){
			throw new IPCException(IPCException.ERROR_CODE, "Could not find the incoming EFCP connection queue " +
					"associated to connection endpoint id "+connectionEndpointId);
		}
		
		return result;
	}
	
	/**
	 * Get the outgoing queue that supports the connection identified by connectionEndpointId
	 * @param connectionId
	 * @return
	 * @throws IPCException if there is no outgoing queue associated to connectionEndpointId
	 */
	public BlockingQueueWithSubscriptor<PDU> getOutgoingConnectionQueue(long connectionEndpointId) throws IPCException{
		BlockingQueueWithSubscriptor<PDU> result = this.outgoingConnectionQueues.get(new Long(connectionEndpointId));
		if (result == null){
			throw new IPCException(IPCException.ERROR_CODE, "Could not find the outgoing EFCP connection queue " +
					"associated to connection endpoint id "+connectionEndpointId);
		}
		
		return result;
	}
}
