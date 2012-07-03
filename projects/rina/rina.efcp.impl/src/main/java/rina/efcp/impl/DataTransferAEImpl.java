package rina.efcp.impl;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.delimiting.api.BaseDelimiter;
import rina.delimiting.api.Delimiter;
import rina.efcp.api.BaseDataTransferAE;
import rina.efcp.api.DataTransferConstants;
import rina.efcp.impl.ribobjects.DataTransferConstantsRIBObject;
import rina.flowallocator.api.ConnectionId;
import rina.flowallocator.api.Flow;
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
	 * The IPC Process delimiter
	 */
	private Delimiter delimiter = null;
	
	/**
	 * The address of this IPC process
	 */
	private long myAddres = -1;
	
	public DataTransferAEImpl(){
		this.reservedCEPIds = new ConcurrentHashMap<Integer, int[]>();
		this.reservedCEPIdList = new ArrayList<Integer>();
		this.portIdToConnectionMapping = new ConcurrentHashMap<Integer, DTAEIState>();
		this.connectionStatesByConnectionId = new ConcurrentHashMap<Long, DTAEIState>();
		this.dataTransferConstants = new DataTransferConstants();
	}
	
	@Override
	public void setIPCProcess(IPCProcess ipcProcess){
		super.setIPCProcess(ipcProcess);
		this.ribDaemon = (RIBDaemon) ipcProcess.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		this.delimiter = (Delimiter) ipcProcess.getIPCProcessComponent(BaseDelimiter.getComponentName());
		populateRIB(ipcProcess);
		this.dataTransferConstants = ipcProcess.getDataTransferConstants();
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
	 * @param source true if this is the 'source' part of the connection, false if this 
	 * is the 'destination' part of the connection
	 * @param socket The socket used to send the data
	 * @param applicationCallback the callback to the application, used to deliver the data
	 */
	public synchronized void createConnectionAndBindToPortId(Flow flow, Socket socket, APService applicationCallback){
		DTAEIState state = new DTAEIState(flow, this.dataTransferConstants);
		state.setSocket(socket);
		state.setApplicationCallback(applicationCallback);
		
		int portId = 0;
		if (flow.isSource()){
			portId = (int) flow.getSourcePortId(); 
		}else{
			portId = (int) flow.getDestinationPortId();
		}
		
		ConnectionId connectionId = flow.getConnectionIds().get(flow.getCurrentConnectionIdIndex());
		if (flow.isSource()){
			this.connectionStatesByConnectionId.put(new Long(connectionId.getSourceCEPId()), state);
		}else{
			this.connectionStatesByConnectionId.put(new Long(connectionId.getDestinationCEPId()), state);
		}
		this.portIdToConnectionMapping.put(new Integer(portId), state);
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
	public synchronized void deleteConnection(ConnectionId connectionId){
		this.connectionStatesByConnectionId.remove(connectionId);
	}
	
	private long getMyAddress(){
		//This code is here since it must be called after enrollment
		if (this.myAddres == -1){
			this.myAddres = this.getIPCProcess().getAddress().longValue();
		}
		
		return this.myAddres;
	}
	
	/**
	 * Post an SDU to the portId (will be sent through the connection identified by portId)
	 * @param portID
	 * @param sdu
	 */
	public void postSDU(int portId, byte[] sdu) throws IPCException{
		DTAEIState state = this.portIdToConnectionMapping.get(new Integer(portId));
		if  (state == null){
			throw new IPCException(IPCException.PROBLEMS_SENDING_SDU_CODE, 
					IPCException.PROBLEMS_SENDING_SDU + ". No active connection is associated to this portId.");
		}
		
		//This connection is supporting a local flow
		if (state.isLocal()){
			DTAEIState state2 = this.portIdToConnectionMapping.get(new Integer(state.getRemotePortId()));
			if (state2 == null){
				throw new IPCException(IPCException.PROBLEMS_SENDING_SDU_CODE, 
						IPCException.PROBLEMS_SENDING_SDU + ". No active connection is associated to this portId.");
			}
			state2.getApplicationCallback().deliverTransfer(state.getRemotePortId(), sdu);
			return;
		}
		
		//Convert the SDU into a PDU and post it to an RMT queue (right now posting it to the socket)
		byte[] pdu = PDUParser.generatePDU(state.getPreComputedPCI(), 
				state.getNextSequenceToSend(), 0x81, 0x00, sdu);
		
		try{
			state.getSocket().getOutputStream().write(this.delimiter.getDelimitedSdu(pdu));
		}catch(IOException ex){
			log.error(ex);
			throw new IPCException(IPCException.PROBLEMS_SENDING_SDU_CODE, 
					IPCException.PROBLEMS_SENDING_SDU + ex.getMessage());
		}
		
		//Update DTAEI state
		synchronized(state){
			state.incrementNextSequenceToSend();
		}
	}
	
	/**
	 * A PDU has been delivered through an N-1 port
	 * @param pdu
	 */
	public void pduDelivered(byte[] pdu){
		//Parse PCI, see if the PDU is for us
		long destinationAddress = PDUParser.parseDestinationAddress(pdu);
		if (destinationAddress != this.getMyAddress()){
			//TODO the PDU must be relayed, but we don't support relaying yet
			log.error("Received a PDU not addressed to this IPC Process, " +
					"but to this destination address: "+destinationAddress);
			return;
		}
		
		//Decode the PDU and look for associated state
		PDU decodedPDU = PDUParser.parsePDU(pdu);
		DTAEIState state = this.connectionStatesByConnectionId.get(new Long(decodedPDU.getConnectionId().getDestinationCEPId()));
		if (state == null){
			log.error("Received a PDU with an unrecognized Connection ID: "+decodedPDU.getConnectionId());
			return;
		}
		
		//Deliver the PDU to the portId
		state.getApplicationCallback().deliverTransfer((int)state.getPortId(), decodedPDU.getUserData().get(0));
		
		//Update DTAEI state
		synchronized(state){
			state.incrementLastSequenceDelivered();
		}
	}
}
