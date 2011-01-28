package rina.efcp.impl;

import java.util.ArrayList;
import java.util.List;

import rina.efcp.api.DataTransferAEInstance;
import rina.efcp.api.EFCPConstants;
import rina.flowallocator.api.Connection;

public class DataTransferAEInstanceImpl implements DataTransferAEInstance{
	
	/**
	 * The Data Transfer state vector
	 */
	private DTAEIState stateVector = null;
	
	public DataTransferAEInstanceImpl(){
		stateVector = new DTAEIState();
	}

	public void pduDelivered(byte[] pdu) {
	}

	public void sdusDelivered(List<byte[]> sdus) {
		//Iterate over SDUs and generate PDUs
		//PDU sequence numbers start at stateVector.NextSequenceToSend
		List<PDU> generatedPDUs = new ArrayList<PDU>();
		PDU currentPDU = null;
		byte[] currentSDU = null;
		
		for(int i=0; i<sdus.size(); i++){
			currentSDU = sdus.get(i);
			if (currentPDU != null){
				if (EFCPConstants.DIFConcatenation && currentSDU.length < (stateVector.getMaxFlowPDUSize() - currentPDU.computePDULength())){
					//There is room in the current PDU for this SDU
					currentPDU.appendSDU(currentSDU);
				}else{
					//The current SDU is too big for the current PDU
					generatedPDUs.add(currentPDU);
					currentPDU = null;
				}
			}
			
			if (currentPDU == null){
				if (currentSDU.length > (stateVector.getMaxFlowPDUSize() - EFCPConstants.pciLength)){
					//Fragmentation can roll over to the next flow in the connection, which must already be allocated
					//TODO create complete PDUs including SDU protection, assigning PDU sequence numbers
					//sequentially starting from StateVector.NextSequenceToSend
					//Increment StateVector.nextSequenceToSend by number of PDUs created
					//Add the generated PDUs to genereatedPDUs
				}else{
					currentPDU = new PDU(stateVector.getConnection());
					currentPDU.setSequenceNumber(stateVector.getNextSequenceToSend());
					currentPDU.appendSDU(currentSDU);
					//TODO increment stateVector.NextSequenceToSend
				}
			}
		}
		
		if (currentPDU != null){
			generatedPDUs.add(currentPDU);
		}
		
		//TODO see spec page 21, add the code to generate
		//the list of posted PDUs if DTCP is present and flow control is enabled
		//For now the generated PDUs equals the list of generated PDUs
		List<PDU> postablePDUs = generatedPDUs;
		
		//Iterate over posted PDUs and give them to the RMT
		for (int i=0; i<postablePDUs.size(); i++){
			//TODO add the stuff if DTCP is present and there is retransmission control
			//TODO post PDU to RMT
		}
		
	}

	public Connection getConnection() {
		return stateVector.getConnection();
	}
}