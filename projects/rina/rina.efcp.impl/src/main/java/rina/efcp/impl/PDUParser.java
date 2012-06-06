package rina.efcp.impl;

import rina.flowallocator.api.ConnectionId;

import com.google.common.primitives.Longs;

/**
 * Generates/parses EFCP PDUs
 * @author eduardgrasa
 *
 */
public class PDUParser {
	
	/**
	 * Encode the fields that are always the same using Little Endian byte order
	 */
	public static byte[] computePCI(long destinationAddress, long sourceAddress, ConnectionId connectionId){
		byte[] aux = null;
		byte[] preComputedPCI = new byte[16];
		//Encode version
		preComputedPCI[0] = 0x01;
		//Encode destination address
		aux = Longs.toByteArray(destinationAddress);
		preComputedPCI[1] = aux[7];
		preComputedPCI[2] = aux[6];
		//Encode source address;
		aux = Longs.toByteArray(sourceAddress);
		preComputedPCI[3] = aux[7];
		preComputedPCI[4] = aux[6];
		//Encode destination CEP-id
		aux = Longs.toByteArray(connectionId.getDestinationCEPId());
		preComputedPCI[5] = aux[7];
		preComputedPCI[6] = aux[6];
		//Encode source CEP-id
		aux = Longs.toByteArray(connectionId.getSourceCEPId());
		preComputedPCI[7] = aux[7];
		preComputedPCI[8] = aux[6];
		//Encode QoS-id
		preComputedPCI[9] = (byte) connectionId.getQosId();
		//Encode pdu-type
		preComputedPCI[10] = 0x01;
		//Encode flags
		preComputedPCI[11] = 0x01;
		//Encode initial sequence number
		preComputedPCI[12] = 0;
		preComputedPCI[13] = 0;
		preComputedPCI[14] = 0;
		preComputedPCI[15] = 0;
		
		return preComputedPCI;
	}

	/**
	 * Generate a PDU from a pre-computed PCI
	 * @param pci
	 * @param sequenceNumber
	 * @param pduType
	 * @param flags
	 * @param sdu
	 * @return
	 */
	public static byte[] generatePDU(byte[] pci, long sequenceNumber, byte pduType, byte flags, byte[] sdu){
		byte[] pdu = new byte[pci.length + sdu.length];
		
		for(int i=0; i<10; i++){
			pdu[i] = pci[i];
		}
		
		//Encode pduType
		pdu[10] = pduType;
		
		//Encode flags
		pdu[11] = flags;
		
		//Encode sequence number
		byte[] aux = Longs.toByteArray(sequenceNumber);
		pdu[12] = aux[7];
		pdu[13] = aux[6];
		pdu[14] = aux[5];
		pdu[15] = aux[4];
		
		//Add user SDU
		for(int i=0; i<sdu.length; i++){
			pdu[i+16] = sdu[i];
		}
		
		return pdu;
	}
	
	public static PDU parsePDU(byte[] encodedPDU){
		PDU pdu = new PDU();
		pdu.setRawPDU(encodedPDU);
		pdu.setVersion(encodedPDU[0]);
		pdu.setDestinationAddress((encodedPDU[2] & 0xFFL) << 8 | (encodedPDU[1] & 0xFFL));
		pdu.setSourceAddress((encodedPDU[4] & 0xFFL) << 8 | (encodedPDU[3] & 0xFFL));
		ConnectionId connectionId = new ConnectionId();
		connectionId.setDestinationCEPId((encodedPDU[6] & 0xFFL) << 8 | (encodedPDU[5] & 0xFFL));
		connectionId.setSourceCEPId((encodedPDU[8] & 0xFFL) << 8 | (encodedPDU[7] & 0xFFL));
		connectionId.setQosId(encodedPDU[9]);
		pdu.setConnectionId(connectionId);
		pdu.setPduType(encodedPDU[10]);
		pdu.setFlags(encodedPDU[11]);
		pdu.setSequenceNumber((encodedPDU[15] & 0xFFL) << 24 | (encodedPDU[14] & 0xFFL) << 16 | 
				(encodedPDU[13] & 0xFFL) << 8 | (encodedPDU[12] & 0xFFL));
		
		byte[] sdu = new byte[encodedPDU.length - 16];
		for(int i=0; i<sdu.length; i++){
			sdu[i] = encodedPDU[i+16];
		}
		pdu.getUserData().add(sdu);
		
		return pdu;
	}
}
