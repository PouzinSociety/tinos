package rina.efcp.impl;

import java.util.ArrayList;
import java.util.List;

import rina.efcp.api.DataTransferAEInstance;
import rina.efcp.api.EFCPConstants;
import rina.flowallocator.api.Connection;
import rina.rmt.api.RMT;

public class DataTransferAEInstanceImpl implements DataTransferAEInstance{
	
	/**
	 * The Data Transfer state vector
	 */
	private DTAEIState stateVector = null;
	
	/**
	 * A pointer to the relaying and multiplexing task
	 */
	private RMT rmt = null;
	
	public DataTransferAEInstanceImpl(Connection connection){
		stateVector = new DTAEIState();
		stateVector.setConnection(connection);
	}

	public RMT getRmt() {
		return rmt;
	}

	public void setRmt(RMT rmt) {
		this.rmt = rmt;
	}
	
	public void pduDelivered(byte[] pdu) {
	}

	public void sdusDelivered(List<byte[]> sdus) {
		//Iterate over SDUs and generate PDUs
		//PDU sequence numbers start at stateVector.NextSequenceToSend
		List<PDU> generatedPDUs = new ArrayList<PDU>();
		PDU currentPDU = null;
		
		for(int i=0; i<sdus.size(); i++){
			processCurrentSDU(sdus.get(i), currentPDU, generatedPDUs);
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
			rmt.send(postablePDUs.get(i).getSerializedPDU());
		}
		
	}
	
	/**
	 * Processes and SDU to be delivered to the RMT (the result is one or more PDUs being generated)
	 * @param currentSDU the SDU to be processed
	 * @param currentPDU the current PDU being assembled
	 * @param generatedPDUs the list of generated PDUs
	 */
	private void processCurrentSDU(byte[] currentSDU, PDU currentPDU, List<PDU> generatedPDUs){
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
				currentPDU = createNewPDU(currentSDU);
			}
		}
	}
	
	/**
	 * Creates and initialize a new PDU that will carry the passed SDU.
	 * @param sdu
	 * @return
	 */
	private PDU createNewPDU(byte[] sdu){
		PDU pdu = new PDU(stateVector.getConnection());
		pdu.setSequenceNumber(stateVector.getNextSequenceToSend());
		pdu.appendSDU(sdu);
		//TODO increment stateVector.NextSequenceToSend
		
		return pdu;
	}

	public Connection getConnection() {
		return stateVector.getConnection();
	}
}