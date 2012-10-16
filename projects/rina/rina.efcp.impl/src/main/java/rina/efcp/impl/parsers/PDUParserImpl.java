package rina.efcp.impl.parsers;

import rina.efcp.api.PDU;
import rina.efcp.api.PDUParser;
import rina.flowallocator.api.ConnectionId;

import com.google.common.primitives.Longs;

/**
 * Generates/parses EFCP PDUs according to a specific policy
 * @author eduardgrasa
 *
 */
public class PDUParserImpl implements PDUParser{
	
	/**
	 * Encode the fields that are always the same using Little Endian byte order
	 */
	public byte[] computePCI(long destinationAddress, long sourceAddress, long sourceCEPid, long destinationCEPid, int qosid){
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
		aux = Longs.toByteArray(destinationCEPid);
		preComputedPCI[4] = aux[7];
		preComputedPCI[5] = aux[6];
		//Encode source CEP-id
		aux = Longs.toByteArray(sourceCEPid);
		preComputedPCI[6] = aux[7];
		preComputedPCI[7] = aux[6];
		//Encode QoS-id
		preComputedPCI[8] = (byte) (qosid & 0xFF);
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
	 * Generate a PDU data structure from a pre-computed PCI. Use unsigned types and little-endian byte order
	 * @param pci
	 * @param sequenceNumber
	 * @param pduType
	 * @param flags
	 * @param sdu
	 * @return
	 */
	public PDU generatePDU(byte[] pci, long sequenceNumber, long destinationAddress, 
			ConnectionId connectionId, int pduType, int flags, byte[] sdu){
		PDU pdu = new PDU();
		
		//Encode pduType
		pci[9] = (byte) (pduType & 0xFF);
		
		//Encode flags
		pci[10] = (byte) (flags & 0xFF);
		
		//Encode sequence number
		byte[] aux = Longs.toByteArray(sequenceNumber);
		pci[11] = aux[7];
		pci[12] = aux[6];
		pci[13] = aux[5];
		pci[14] = aux[4];
		
		pdu.setEncodedPCI(pci);
		pdu.setSequenceNumber(sequenceNumber);
		pdu.setDestinationAddress(destinationAddress);
		pdu.setConnectionId(connectionId);
		pdu.setUserData(sdu);
		
		return pdu;
	}
	
	public PDU generateManagementPDU(byte[] sdu){
		byte[] pci = computePCI(0,0,0,0,0);
		return generatePDU(pci, 0, 0, null, PDU.MANAGEMENT_PDU_TYPE, 0, sdu);
	}
	
	public PDU generateIdentifySenderPDU(long address, int qosId){
		byte[] pci = computePCI(0, address, 0, 0, 0);
		ConnectionId conId = new ConnectionId();
		conId.setQosId(qosId);
		return generatePDU(pci, 0, 0, conId, PDU.IDENTIFY_SENDER_PDU_TYPE, 0, new byte[0]);
	}
	
	/**
	 * Parse the values of the encoded PDU relevant to the RMT into a canonical data structure.
	 * Uses unsigned types and little-endian byte order
	 * @param encodedPDU
	 * @return
	 */
	public PDU parsePCIForRMT(PDU pdu){
		byte[] encodedPDU = pdu.getRawPDU();
		byte offset = pdu.getPciStartIndex();
		pdu.setDestinationAddress((encodedPDU[1+offset] & 0xFFL) << 8 | (encodedPDU[0+offset] & 0xFFL));
		pdu.setSourceAddress((encodedPDU[3+offset] & 0xFFL) << 8 | (encodedPDU[2+offset] & 0xFFL));
		ConnectionId connectionId = new ConnectionId();
		connectionId.setQosId(encodedPDU[8+offset] & 0xFF); //has to be 'anded' with 0xFF to remove the sign bit
		pdu.setConnectionId(connectionId);
		pdu.setPduType(encodedPDU[9+offset] & 0xFF);
		pdu.setFlags(encodedPDU[10+offset] & 0xFF);
		
		return pdu;
	}
	
	public PDU parsePCIForEFCP(PDU pdu){
		byte[] encodedPDU = pdu.getRawPDU();
		byte offset = pdu.getPciStartIndex();
		pdu.setSequenceNumber((encodedPDU[14+offset] & 0xFFL) << 24 | (encodedPDU[13+offset] & 0xFFL) << 16 | 
				(encodedPDU[12+offset] & 0xFFL) << 8 | (encodedPDU[11+offset] & 0xFFL));
		ConnectionId connectionId = pdu.getConnectionId();
		connectionId.setDestinationCEPId((encodedPDU[5+offset] & 0xFFL) << 8 | (encodedPDU[4+offset] & 0xFFL));
		connectionId.setSourceCEPId((encodedPDU[7+offset] & 0xFFL) << 8 | (encodedPDU[6+offset] & 0xFFL));
		
		byte[] sdu = new byte[encodedPDU.length - 15 - offset];
		System.arraycopy(encodedPDU, 15 + offset, sdu, 0, sdu.length);
		pdu.setUserData(sdu);
		
		return pdu;
	}
}
