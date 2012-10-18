package rina.efcp.impl.parsers;

import rina.efcp.api.DTPPDU;
import rina.efcp.api.FlowControlOnlyDTCPPDU;
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
	public byte[] computeDTPPCI(long destinationAddress, long sourceAddress, long sourceCEPid, long destinationCEPid, int qosid){
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
		preComputedPCI[9] = (byte) PDU.DTP_PDU_TYPE;
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
	 * Encode the fields that are always the same in the Flow Control Only DTCP PCI during the flow lifetime, using Little Endian 
	 * byte order
	 * @param destinationAddress
	 * @param sourceAddress
	 * @param sourceCEPid
	 * @param destinationCEPid
	 * @param qosid
	 * @return
	 */
	public byte[] computeFlowControlOnlyDTCPPCI(long destinationAddress, long sourceAddress, long sourceCEPid, long destinationCEPid, int qosid){
		byte[] aux = null;
		byte[] preComputedPCI = new byte[18];
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
		preComputedPCI[9] = (byte) PDU.FLOW_CONTROL_ONLY_DTCP_PDU;
		//Encode initial sequence number
		preComputedPCI[10] = 0;
		preComputedPCI[11] = 0;
		preComputedPCI[12] = 0;
		preComputedPCI[13] = 0;
		//Encode initial right window edge
		preComputedPCI[14] = 0;
		preComputedPCI[15] = 0;
		preComputedPCI[16] = 0;
		preComputedPCI[17] = 0;
		
		return preComputedPCI;
	}
	
	/**
	 * Generate a DTP PDU data structure from a pre-computed PCI. Use unsigned types and little-endian byte order
	 * @param pci
	 * @param sequenceNumber
	 * @param pduType
	 * @param flags
	 * @param sdu
	 * @return
	 */
	public DTPPDU generateDTPPDU(byte[] pci, long sequenceNumber, long destinationAddress, 
			ConnectionId connectionId, int flags, byte[] sdu){
		return (DTPPDU) generateGenericPDU(new DTPPDU(), 
				pci, sequenceNumber, destinationAddress, 
				connectionId, PDU.DTP_PDU_TYPE, flags, sdu);
	}
	
	public PDU generateManagementPDU(byte[] sdu){
		byte[] pci = computeDTPPCI(0,0,0,0,0);
		return generateGenericPDU(new PDU(), pci, 0, 0, null, PDU.MANAGEMENT_PDU_TYPE, 0, sdu);
	}
	
	public PDU generateIdentifySenderPDU(long address, int qosId){
		byte[] pci = computeDTPPCI(0, address, 0, 0, 0);
		ConnectionId conId = new ConnectionId();
		conId.setQosId(qosId);
		return generateGenericPDU(new PDU(), pci, 0, 0, conId, PDU.IDENTIFY_SENDER_PDU_TYPE, 0, new byte[0]);
	}
	
	private PDU generateGenericPDU(PDU pdu, byte[] pci, long sequenceNumber, long destinationAddress, 
			ConnectionId connectionId, int pduType, int flags, byte[] sdu){
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
	
	public FlowControlOnlyDTCPPDU generateFlowControlOnlyDTCPPDU(byte[] pci, long sequenceNumber, long destinationAddress, 
			ConnectionId connectionId, long rightWindowEdge){
		FlowControlOnlyDTCPPDU pdu = new FlowControlOnlyDTCPPDU();
		
		//Encode sequence number
		byte[] aux = Longs.toByteArray(sequenceNumber);
		pci[10] = aux[7];
		pci[11] = aux[6];
		pci[12] = aux[5];
		pci[13] = aux[4];
		
		//Encode right window edge
		aux = Longs.toByteArray(sequenceNumber);
		pci[14] = aux[7];
		pci[15] = aux[6];
		pci[16] = aux[5];
		pci[17] = aux[4];
		
		pdu.setEncodedPCI(pci);
		pdu.setSequenceNumber(sequenceNumber);
		pdu.setDestinationAddress(destinationAddress);
		pdu.setConnectionId(connectionId);
		pdu.setRightWindowEdge(rightWindowEdge);
		pdu.setUserData(new byte[0]);
		
		return pdu;
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
		pdu.setDestinationAddress(this.parse2ByteLittleEndianLong(encodedPDU, 1 + offset));
		pdu.setSourceAddress(this.parse2ByteLittleEndianLong(encodedPDU, 3 + offset));
		ConnectionId connectionId = new ConnectionId();
		connectionId.setQosId(encodedPDU[8+offset] & 0xFF); //has to be 'anded' with 0xFF to remove the sign bit
		pdu.setConnectionId(connectionId);
		pdu.setPduType(encodedPDU[9+offset] & 0xFF);
		
		return pdu;
	}
	
	public PDU parsePCIForEFCP(PDU pdu){
		byte[] encodedPDU = pdu.getRawPDU();
		byte offset = pdu.getPciStartIndex();
		ConnectionId connectionId = pdu.getConnectionId();
		connectionId.setDestinationCEPId(this.parse2ByteLittleEndianLong(encodedPDU, 5 + offset));
		connectionId.setSourceCEPId(this.parse2ByteLittleEndianLong(encodedPDU, 7 + offset));
		
		byte[] sdu = null;
		switch(pdu.getPduType()){
		case PDU.DTP_PDU_TYPE:
			DTPPDU dtpPDU = new DTPPDU(pdu);
			dtpPDU.setFlags(encodedPDU[10+offset] & 0xFF);
			dtpPDU.setSequenceNumber(this.parse4ByteLittleEndianLong(encodedPDU, 14 + offset));
			sdu = new byte[encodedPDU.length - 15 - offset];
			System.arraycopy(encodedPDU, 15 + offset, sdu, 0, sdu.length);
			dtpPDU.setUserData(sdu);
			return dtpPDU;
		case PDU.MANAGEMENT_PDU_TYPE:
			sdu = new byte[encodedPDU.length - 15 - offset];
			System.arraycopy(encodedPDU, 15 + offset, sdu, 0, sdu.length);
			pdu.setUserData(sdu);
			return pdu;
		case PDU.FLOW_CONTROL_ONLY_DTCP_PDU:
			FlowControlOnlyDTCPPDU dtcpPDU = new FlowControlOnlyDTCPPDU(pdu);
			dtcpPDU.setSequenceNumber(this.parse4ByteLittleEndianLong(encodedPDU, 13 + offset));
			dtcpPDU.setRightWindowEdge(this.parse4ByteLittleEndianLong(encodedPDU, 17 + offset));
			return dtcpPDU;
		}
		
		return pdu;
	}
	
	private long parse4ByteLittleEndianLong(byte[] encodedBytes, int startIndex){
		return (encodedBytes[startIndex] & 0xFFL) << 24 | (encodedBytes[startIndex - 1] & 0xFFL) << 16 | 
		(encodedBytes[startIndex - 2] & 0xFFL) << 8 | (encodedBytes[startIndex - 3] & 0xFFL);
	}
	
	private long parse2ByteLittleEndianLong(byte[] encodedBytes, int startIndex){
		return (encodedBytes[startIndex] & 0xFFL) << 8 | (encodedBytes[startIndex -1] & 0xFFL);
	}
}
