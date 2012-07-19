package rina.efcp.api;

import java.net.Socket;

import rina.flowallocator.api.ConnectionId;
import rina.flowallocator.api.Flow;
import rina.ipcprocess.api.IPCProcessComponent;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.IPCException;

/**
 * Creates and manages the lifecycle of DataTrasnferAEInstances
 * @author eduardgrasa
 *
 */
public interface DataTransferAE extends IPCProcessComponent {
	
	/**
	 * Reserve a number of CEP ids (connection endpoint ids) that will be used during the lifetime
	 * of a flow (identified by portId).
	 * @param numberOfCEPIds The number of CEP ids to reserve
	 * @param portId the portId of the flow that will use these CEP ids
	 * @return
	 */
	public int[] reserveCEPIds(int numberOfCEPIds, int portId);
	
	/**
	 * Free the CEP ids (connection endpoint ids) associated to a flow identified by portId
	 * @param portId
	 */
	public void freeCEPIds(int portId);

	/**
	 * Initialize the state of a new local connection and bind it to the portId
	 * @param portId
	 * @param remotePortId
	 * @param applicationCallback the callback to the application, used to deliver the data
	 */
	public void createLocalConnectionAndBindToPortId(int portId, int remotePortId, APService applicationCallback);
	
	/**
	 * Initialize the state of a new connection, and bind it to the portId (all the SDUs delivered 
	 * to the portId by an application will be sent through this connection)
	 * @param flow the flow object, describing the service supported by this connection
	 * @param socket The socket used to send the data
	 * @param local true if this is a connection supporting a local flow, false otherways
	 * @param applicationCallback the callback to the application, used to deliver the data
	 */
	public void createConnectionAndBindToPortId(Flow flow, Socket socket, APService applicationCallback);
	
	/**
	 * Destroy the instance of the data transfer AE associated to this connection endpoint Id
	 * @param connection endpoint id
	 */
	public void deleteConnection(ConnectionId connectionId);
	
	/**
	 * Sends a delimited 0 length SDU
	 * @param portId
	 * @throws IPCException
	 */
	public void post0LengthSDU(int portId) throws IPCException;
	
	/**
	 * A PDU has been delivered through an N-1 port
	 * @param pdu
	 */
	public void pduDelivered(byte[] pdu);
}
