package rina.efcp.impl;

import rina.flowallocator.api.ConnectionId;

import com.google.common.primitives.Longs;

/**
 * Generates/parses EFCP PDUs according to a specific policy
 * @author eduardgrasa
 *
 */
public class PDUParser {
	
	/**
	 * Encode the fields that are always the same using Little Endian byte order
	 */
	public static byte[] computePCI(long destinationAddress, long sourceAddress, ConnectionId connectionId){
		byte[] aux = null;
		byte[] preComputedPCI = new byte[15];
		//Encode destination address
		aux = Longs.toByteArray(destinationAddress);
		preComputedPCI[0] = aux[7];
		preComputedPCI[1] = aux[6];
		//Encode source address;
		aux = Longs.toByteArray(sourceAddress);
		preComputedPCI[2] = aux[7];
		preComputedPCI[3] = aux[6];
		//Encode destination CEP-id
		aux = Longs.toByteArray(connectionId.getDestinationCEPId());
		preComputedPCI[4] = aux[7];
		preComputedPCI[5] = aux[6];
		//Encode source CEP-id
		aux = Longs.toByteArray(connectionId.getSourceCEPId());
		preComputedPCI[6] = aux[7];
		preComputedPCI[7] = aux[6];
		//Encode QoS-id
		preComputedPCI[8] = (byte) (connectionId.getQosId() & 0xFF);
		//Encode pdu-type
		preComputedPCI[9] = (byte) 0x81;
		//Encode flags
		preComputedPCI[10] = 0x00;
		//Encode initial sequence number
		preComputedPCI[11] = 0;
		preComputedPCI[12] = 0;
		preComputedPCI[13] = 0;
		preComputedPCI[14] = 0;
		
		return preComputedPCI;
	}

	/**
	 * Generate a PDU from a pre-computed PCI. Use unsigned types and little-endian byte order
	 * @param pci
	 * @param sequenceNumber
	 * @param pduType
	 * @param flags
	 * @param sdu
	 * @return
	 */
	public static byte[] generatePDU(byte[] pci, long sequenceNumber, int pduType, int flags, byte[] sdu){
		byte[] pdu = new byte[pci.length + sdu.length];
		
		for(int i=0; i<9; i++){
			pdu[i] = pci[i];
		}
		
		//Encode pduType
		pdu[9] = (byte) (pduType & 0xFF);
		
		//Encode flags
		pdu[10] = (byte) (flags & 0xFF);
		
		//Encode sequence number
		byte[] aux = Longs.toByteArray(sequenceNumber);
		pdu[11] = aux[7];
		pdu[12] = aux[6];
		pdu[13] = aux[5];
		pdu[14] = aux[4];
		
		//Add user SDU
		System.arraycopy(sdu, 0, pdu, 15, sdu.length);
		
		return pdu;
	}
	
	/**
	 * Parse the values of the encoded PDU into a canonical data structure.
	 * Uses unsigned types and little-endian byte order
	 * @param encodedPDU
	 * @return
	 */
	public static PDU parsePDU(byte[] encodedPDU){
		PDU pdu = new PDU();
		pdu.setRawPDU(encodedPDU);
		pdu.setDestinationAddress((encodedPDU[1] & 0xFFL) << 8 | (encodedPDU[0] & 0xFFL));
		pdu.setSourceAddress((encodedPDU[3] & 0xFFL) << 8 | (encodedPDU[2] & 0xFFL));
		ConnectionId connectionId = new ConnectionId();
		connectionId.setDestinationCEPId((encodedPDU[5] & 0xFFL) << 8 | (encodedPDU[4] & 0xFFL));
		connectionId.setSourceCEPId((encodedPDU[7] & 0xFFL) << 8 | (encodedPDU[6] & 0xFFL));
		connectionId.setQosId(encodedPDU[8] & 0xFF); //has to be 'anded' with 0xFF to remove the sign bit
		pdu.setConnectionId(connectionId);
		pdu.setPduType(encodedPDU[9] & 0xFF);
		pdu.setFlags(encodedPDU[10] & 0xFF);
		pdu.setSequenceNumber((encodedPDU[14] & 0xFFL) << 24 | (encodedPDU[13] & 0xFFL) << 16 | 
				(encodedPDU[12] & 0xFFL) << 8 | (encodedPDU[11] & 0xFFL));
		
		byte[] sdu = new byte[encodedPDU.length - 15];
		System.arraycopy(encodedPDU, 15, sdu, 0, sdu.length);
		pdu.getUserData().add(sdu);
		
		return pdu;
	}
	
	/**
	 * Returns the decoded destination address of a PDU's PCI
	 * @param encodedPDU
	 * @return the decoded destination address
	 */
	public static long parseDestinationAddress(byte[] encodedPDU){
		return (encodedPDU[1] & 0xFFL) << 8 | (encodedPDU[0] & 0xFFL);
	}
}
