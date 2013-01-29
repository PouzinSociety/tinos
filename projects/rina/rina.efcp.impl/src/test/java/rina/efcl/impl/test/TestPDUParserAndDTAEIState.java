package rina.efcl.impl.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import rina.efcp.api.DataTransferConstants;
import rina.efcp.impl.DTAEIState;
import rina.efcp.impl.parsers.PDUParserImpl;
import rina.efcp.api.PDU;
import rina.efcp.api.PDUParser;
import rina.flowallocator.api.ConnectionId;
import rina.flowallocator.api.Flow;

public class TestPDUParserAndDTAEIState {

	private Flow flow = null;
	private DTAEIState state = null;
	private ConnectionId connectionId = null;
	private DataTransferConstants dataTransferConstants = null;
	private PDUParser pduParser = null;
	
	@Before
	public void setup(){
		flow = new Flow();
		flow.setSource(true);
		flow.setSourceAddress(29);
		flow.setDestinationAddress(44);
		flow.setSourcePortId(1);
		flow.setDestinationPortId(2);
		connectionId = new ConnectionId();
		connectionId.setDestinationCEPId(32);
		connectionId.setSourceCEPId(12);
		connectionId.setQosId((byte)1);
		flow.getConnectionIds().add(connectionId);
		pduParser = new PDUParserImpl();
		
		dataTransferConstants = new DataTransferConstants();
		
		state = new DTAEIState(flow, dataTransferConstants, pduParser);
	}
	
	@Test
	public void testPDUEncodingDecoding(){
		PDU pdu = null;
		byte[] sdu = new String("Testing EFCP encoding and decoding").getBytes();
		
		for(int i=0; i<3; i++){
			pdu = pduParser.generateDTPPDU(state.getPreComputedPCI(), 
					state.getNextSequenceToSend(), flow.getDestinationAddress(), 
					flow.getConnectionIds().get(0), 0x00, sdu);
			printBytes(pdu.getEncodedPCI());

			PDU decodedPDU = pduParser.parsePCIForEFCP(pduParser.parsePCIForRMT(pdu));
			state.incrementNextSequenceToSend();
			
			System.out.println(decodedPDU.toString());
			System.out.println();
			Assert.assertEquals(flow.getSourceAddress(), decodedPDU.getSourceAddress());
			Assert.assertEquals(flow.getDestinationAddress(), decodedPDU.getDestinationAddress());
			Assert.assertEquals(connectionId.getDestinationCEPId(), decodedPDU.getConnectionId().getDestinationCEPId());
			Assert.assertEquals(connectionId.getSourceCEPId(), decodedPDU.getConnectionId().getSourceCEPId());
			Assert.assertEquals(connectionId.getQosId(), decodedPDU.getConnectionId().getQosId());
			Assert.assertEquals(i, decodedPDU.getSequenceNumber());
			Assert.assertEquals(0x81, decodedPDU.getPduType());
			Assert.assertEquals(0x00, decodedPDU.getFlags());
			Assert.assertEquals(new String(sdu), new String(decodedPDU.getUserData()));
		}
	}
	
	private void printBytes(byte[] pdu){
		for(int i=0; i<pdu.length; i++){
			System.out.print(String.format("%02X ", pdu[i]));
		}
		System.out.print("\n");
	}
}
