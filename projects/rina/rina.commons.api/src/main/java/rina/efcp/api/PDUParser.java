package rina.efcp.api;

import rina.flowallocator.api.ConnectionId;

/**
 * Generates/parses EFCP PDUs according to a specific policy
 * @author eduardgrasa
 *
 */
public interface PDUParser {

	/**
	 * Encode the fields that are always the same using Little Endian byte order
	 */
	public byte[] computePCI(long destinationAddress, long sourceAddress, long sourceCEPid, long destinationCEPid, int qosid);
	
	/**
	 * Generate a PDU data structure from a pre-computed PCI. Use unsigned types and little-endian byte order
	 * @param pci
	 * @param sequenceNumber
	 * @param pduType
	 * @param flags
	 * @param sdu
	 * @return
	 */
	public PDU generatePDU(byte[] pci, long sequenceNumber, long destinationAddress, 
			ConnectionId connectionId, int pduType, int flags, byte[] sdu);
	
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
