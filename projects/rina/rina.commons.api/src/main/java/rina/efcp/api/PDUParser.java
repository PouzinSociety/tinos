package rina.efcp.api;

import rina.flowallocator.api.ConnectionId;

/**
 * Generates/parses EFCP PDUs according to a specific policy
 * @author eduardgrasa
 *
 */
public interface PDUParser {

	/**
	 * Encode the DTP fields that are always the same during the flow lifetime using Little Endian byte order
	 */
	public byte[] computeDTPPCI(long destinationAddress, long sourceAddress, long sourceCEPid, long destinationCEPid, int qosid);
	
	/**
	 * Encode the fields that are always the same in the Flow Control Only DTCP PCI during the flow lifetime, using Little Endian 
	 * byte order
	 * @param destinationAddress
	 * @param sourceAddress
	 * @param sourceCEPid
	 * @param destinationCEPid
	 * @param qosid
	 * @return
	 */
	public byte[] computeFlowControlOnlyDTCPPCI(long destinationAddress, long sourceAddress, long sourceCEPid, long destinationCEPid, int qosid);
	
	/**
	 * Generate a DTP PDU data structure from a pre-computed DTP PCI. Use unsigned types and little-endian byte order
	 * @param pci
	 * @param sequenceNumber
	 * @param flags
	 * @param sdu
	 * @return
	 */
	public DTPPDU generateDTPPDU(byte[] pci, long sequenceNumber, long destinationAddress, 
			ConnectionId connectionId, int flags, byte[] sdu);
	
	public FlowControlOnlyDTCPPDU generateFlowControlOnlyDTCPPDU(byte[] pci, long sequenceNumber, long destinationAddress, 
			ConnectionId connectionId, long rightWindowEdge);
	
	public PDU generateManagementPDU(byte[] sdu);
	
	public PDU generateIdentifySenderPDU(long address, int qosId);
	
	/**
	 * Parse the values of the encoded PDU relevant to the RMT into a canonical data structure.
	 * Uses unsigned types and little-endian byte order
	 * @param encodedPDU
	 * @return
	 */
	public PDU parsePCIForRMT(PDU pdu);
	
	public PDU parsePCIForEFCP(PDU pdu);
}
