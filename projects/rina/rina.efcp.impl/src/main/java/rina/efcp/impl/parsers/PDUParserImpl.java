package rina.efcp.impl.parsers;

import rina.efcp.api.AckAndFlowControlDTCPPDU;
import rina.efcp.api.AckOnlyDTCPPDU;
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
	 * Encode the fields that are always the same during the flow lifetime using Little Endian byte order
	 * @param destinationAddress
	 * @param sourceAddress
	 * @param sourceCEPid
	 * @param destinationCEPid
	 * @param qosid
	 * @param pduType
	 * @return
	 */
	public byte[] preComputePCI(long destinationAddress, long sourceAddress, long sourceCEPid, long destinationCEPid, int qosid){
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
		preComputedPCI[9] = 0x00;
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
	 * Generate a DTP PDU data structure from a pre-computed PCI. Use unsigned types and little-endian byte order
	 * @param pci
	 * @param sequenceNumber
	 * @param pduType
	 * @param flags
	 * @param userData
	 * @return
	 */
	public PDU generateDTPPDU(byte[] pci, long sequenceNumber, long destinationAddress, 
			ConnectionId connectionId, int flags, byte[] userData){
		return generateGenericPDU(new PDU(), 
				pci, sequenceNumber, destinationAddress, 
				connectionId, PDU.DTP_PDU_TYPE, flags, userData);
	}
	
	/**
	 * Generates an EFCP Management PDU
	 * @param managementData
	 * @return
	 */
	public PDU generateManagementPDU(byte[] managementData){
		byte[] pci = preComputePCI(0,0,0,0,0);
		return generateGenericPDU(new PDU(), pci, 0, 0, null, PDU.MANAGEMENT_PDU_TYPE, 0, managementData);
	}
	
	public PDU generateIdentifySenderPDU(long address, int qosId){
		byte[] pci = preComputePCI(0, address, 0, 0, qosId);
		ConnectionId conId = new ConnectionId();
		conId.setQosId(qosId);
		return generateGenericPDU(new PDU(), pci, 0, 0, conId, PDU.IDENTIFY_SENDER_PDU_TYPE, 0, new byte[0]);
	}
	
	/**
	 * Generate a DTCP Flow control only PDU
	 * @param pci
	 * @param sequenceNumber
	 * @param destinationAddress
	 * @param connectionId
	 * @param rightWindowEdge
	 * @param newRate
	 * @param timeUnit
	 * @return
	 */
	public FlowControlOnlyDTCPPDU generateFlowControlOnlyDTCPPDU(byte[] pci, long sequenceNumber, long destinationAddress, 
			ConnectionId connectionId, long rightWindowEdge, long newRate, long timeUnit){
		FlowControlOnlyDTCPPDU pdu = new FlowControlOnlyDTCPPDU();
		byte[] userData = new byte[8];
		//Encode right window edge
		byte[] aux = Longs.toByteArray(rightWindowEdge);
		userData[0] = aux[7];
		userData[1] = aux[6];
		userData[2] = aux[5];
		userData[3] = aux[4];
		//Encode newRate
		aux = Longs.toByteArray(newRate);
		userData[4] = aux[7];
		userData[5] = aux[6];
		//Encode timeUnit
		aux = Longs.toByteArray(timeUnit);
		userData[6] = aux[7];
		userData[7] = aux[6];
		
		this.generateGenericPDU(pdu, pci, sequenceNumber, destinationAddress, connectionId, PDU.FLOW_CONTROL_ONLY_DTCP_PDU, 0x00, userData);
		pdu.setRightWindowEdge(rightWindowEdge);
		pdu.setNewRate(newRate);
		pdu.setTimeUnit(timeUnit);
		
		return pdu;
	}
	
	/**
	 * Generate a DTCP ACK only PDU
	 * @param pci
	 * @param sequenceNumber
	 * @param destinationAddress
	 * @param connectionId
	 * @param ack
	 * @return
	 */
	public AckOnlyDTCPPDU generateAckOnlyDTCPPDU(byte[] pci, long sequenceNumber, long destinationAddress, 
			ConnectionId connectionId, long ack){
		AckOnlyDTCPPDU pdu = new AckOnlyDTCPPDU();
		byte[] userData = new byte[4];
		//Encode ack
		byte[] aux = Longs.toByteArray(ack);
		userData[0] = aux[7];
		userData[1] = aux[6];
		userData[2] = aux[5];
		userData[3] = aux[4];
		
		this.generateGenericPDU(pdu, pci, sequenceNumber, destinationAddress, connectionId, PDU.ACK_ONLY_DTCP_PDU, 0x00, userData);
		pdu.setAck(ack);
		
		return pdu;
	}
	
	/**
	 * Generate a DTCP Flow control only PDU
	 * @param pci
	 * @param sequenceNumber
	 * @param destinationAddress
	 * @param connectionId
	 * @param rightWindowEdge
	 * @param newRate
	 * @param timeUnit
	 * @return
	 */
	public AckAndFlowControlDTCPPDU generateAckAndFlowControlDTCPPDU(byte[] pci, long sequenceNumber, long destinationAddress, 
			ConnectionId connectionId, long ack, long rightWindowEdge, long newRate, long timeUnit){
		AckAndFlowControlDTCPPDU pdu = new AckAndFlowControlDTCPPDU();
		byte[] userData = new byte[12];
		//Encode ack
		byte[] aux = Longs.toByteArray(ack);
		userData[0] = aux[7];
		userData[1] = aux[6];
		userData[2] = aux[5];
		userData[3] = aux[4];
		//Encode right window edge
		aux = Longs.toByteArray(rightWindowEdge);
		userData[4] = aux[7];
		userData[5] = aux[6];
		userData[6] = aux[5];
		userData[7] = aux[4];
		//Encode newRate
		aux = Longs.toByteArray(newRate);
		userData[8] = aux[7];
		userData[9] = aux[6];
		//Encode timeUnit
		aux = Longs.toByteArray(timeUnit);
		userData[10] = aux[7];
		userData[11] = aux[6];
		
		this.generateGenericPDU(pdu, pci, sequenceNumber, destinationAddress, connectionId, PDU.ACK_AND_FLOW_CONTROL_DTCP_PDU, 0x00, userData);
		pdu.setAck(ack);
		pdu.setRightWindowEdge(rightWindowEdge);
		pdu.setNewRate(newRate);
		pdu.setTimeUnit(timeUnit);
		
		return pdu;
	}
	
	private PDU generateGenericPDU(PDU pdu, byte[] pci, long sequenceNumber, long destinationAddress, 
			ConnectionId connectionId, int pduType, int flags, byte[] userData){
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
		pdu.setFlags(flags);
		pdu.setSequenceNumber(sequenceNumber);
		pdu.setDestinationAddress(destinationAddress);
		pdu.setConnectionId(connectionId);
		pdu.setUserData(userData);
		
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
		case PDU.DTP_PDU_TYPE:;
			pdu.setFlags(encodedPDU[10+offset] & 0xFF);
			pdu.setSequenceNumber(this.parse4ByteLittleEndianLong(encodedPDU, 14 + offset));
			sdu = new byte[encodedPDU.length - 15 - offset];
			System.arraycopy(encodedPDU, 15 + offset, sdu, 0, sdu.length);
			pdu.setUserData(sdu);
			return pdu;
		case PDU.MANAGEMENT_PDU_TYPE:
			sdu = new byte[encodedPDU.length - 15 - offset];
			System.arraycopy(encodedPDU, 15 + offset, sdu, 0, sdu.length);
			pdu.setUserData(sdu);
			return pdu;
		case PDU.FLOW_CONTROL_ONLY_DTCP_PDU:
			FlowControlOnlyDTCPPDU dtcpPDU = new FlowControlOnlyDTCPPDU(pdu);
			dtcpPDU.setSequenceNumber(this.parse4ByteLittleEndianLong(encodedPDU, 14 + offset));
			dtcpPDU.setRightWindowEdge(this.parse4ByteLittleEndianLong(encodedPDU, 18 + offset));
			dtcpPDU.setNewRate(this.parse2ByteLittleEndianLong(encodedPDU, 20 + offset));
			dtcpPDU.setTimeUnit(this.parse2ByteLittleEndianLong(encodedPDU, 22 + offset));
			return dtcpPDU;
		case PDU.ACK_ONLY_DTCP_PDU:
			AckOnlyDTCPPDU adtcpPDU = new AckOnlyDTCPPDU(pdu);
			adtcpPDU.setSequenceNumber(this.parse4ByteLittleEndianLong(encodedPDU, 14 + offset));
			adtcpPDU.setAck(this.parse4ByteLittleEndianLong(encodedPDU, 18 + offset));
			return adtcpPDU;
		case PDU.ACK_AND_FLOW_CONTROL_DTCP_PDU:
			AckAndFlowControlDTCPPDU afdtcpPDU = new AckAndFlowControlDTCPPDU(pdu);
			afdtcpPDU.setSequenceNumber(this.parse4ByteLittleEndianLong(encodedPDU, 14 + offset));
			afdtcpPDU.setAck(this.parse4ByteLittleEndianLong(encodedPDU, 18 + offset));
			afdtcpPDU.setRightWindowEdge(this.parse4ByteLittleEndianLong(encodedPDU, 22 + offset));
			afdtcpPDU.setNewRate(this.parse2ByteLittleEndianLong(encodedPDU, 24 + offset));
			afdtcpPDU.setTimeUnit(this.parse2ByteLittleEndianLong(encodedPDU, 26 + offset));
			return afdtcpPDU;
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
