package rina.rmt.api;

import rina.ipcprocess.api.IPCProcessComponent;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.QoSParameters;

/**
 * Specifies the interface of the Relaying and Multiplexing task. Mediates the access to one or more (N-1) DIFs 
 * or physical media
 * @author eduardgrasa
 */
public interface RMT extends IPCProcessComponent{
	
	/**
	 * When the RMT receives an EFCP PDU via a send primitive, it inspects the destination 
	 * address field and the connection-id field of the PDU. Using the FIB, it determines 
	 * which queue, the PDU should be placed on
	 * @param pdu
	 */
	public void sendEFCPPDU(byte[] pdu);
	
	/**
	 * Send a CDAP message to the IPC process address identified by the 'address' parameter. 
	 * This operation is invoked by the management tasks of the IPC process, usually to 
	 * send CDAP messages to the nearest neighbors. The RMT will lookup the 'address' 
	 * parameter in the forwarding table, and send the capMessage using the management flow 
	 * that was established when this IPC process joined the DIF.
	 * @param portId
	 * @param cdapMessage
	 */
	public void sendCDAPMessage(int portId, byte[] cdapMessage);
	
	/**
	 * Cause the RMT to allocate a new flow through an N-1 DIF or the underlying
	 * physical media
	 * @param apNamingInfo the destination application process naming information 
	 * @param qosparams the quality of service requested by the flow
	 * @return int the portId allocated to the flow
	 * @throws Exception if there was an issue allocating the flow
	 */
	public int allocateFlow(ApplicationProcessNamingInfo apNamingInfo, QoSParameters qosparams) throws Exception;
}