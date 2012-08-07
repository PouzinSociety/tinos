package rina.resourceallocator.api;

import rina.efcp.api.PDU;

/**
 * Classes implementing this interface contain information of what N-1 flow 
 * has to be used to send an N-PDU, based on the values of the fields of the PDU's PCI.
 * @author eduardgrasa
 *
 */
public interface PDUForwardingTable {
	
	/**
	 * Returns the N-1 portId through which the N PDU has to be sent
	 * @param pdu
	 * @return
	 */
	public int getNMinusOnePortId(PDU pdu);
	
	/**
	 * Add an entry to the forwarding table
	 * @param destinationAddress
	 * @param qosId
	 * @param destinationCEPId
	 * @param portId the portId associated to the destination_address-qosId-destination_CEP_id 
	 */
	public void addEntry(long destinationAddress, int qosId, long destinationCEPId, int portId);
	
	/**
	 * Remove an entry from the forwarding table
	 * @param destinationAddress
	 * @param qosId
	 * @param destinationCEPId
	 */
	public void removeEntry(long destinationAddress, int qosId, long destinationCEPId);
}
