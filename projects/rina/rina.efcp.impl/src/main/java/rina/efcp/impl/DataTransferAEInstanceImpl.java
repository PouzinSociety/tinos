package rina.efcp.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import rina.efcp.api.DataTransferAEInstance;
import rina.efcp.api.DataTransferConstants;
import rina.flowallocator.api.Connection;
import rina.ipcprocess.api.BaseIPCProcessComponent;
import rina.ipcprocess.api.IPCProcess;
import rina.rmt.api.RMT;
import rina.utils.types.Unsigned;

public class DataTransferAEInstanceImpl extends BaseIPCProcessComponent implements DataTransferAEInstance {
	
	/**
	 * The Data Transfer state vector
	 */
	private DTAEIState stateVector = null;
	
	/**
	 * A pointer to the IPC process
	 */
	private IPCProcess ipcProcess = null;
	
	/**
	 * The data transfer constants in this DIF
	 */
	private DataTransferConstants dataTransferConstants = null;
	
	public DataTransferAEInstanceImpl(Connection connection, DataTransferConstants dataTransferConstants){
		this.dataTransferConstants = dataTransferConstants;
		stateVector = new DTAEIState(connection, dataTransferConstants);
	}

	public void setIPCProcess(IPCProcess ipcProcess) {
		this.ipcProcess = ipcProcess;
	}

	public DTAEIState getStateVector(){
		return stateVector;
	}
	
	/**
	 * @see DataTransferAEInstance.pduDelivered
	 */
	public void pduDelivered(byte[] pdu) {
		PDU currentPDU = PDU.createPDUFromByteArray(pdu, dataTransferConstants);
		
		if (pduAlreadyDelivered(currentPDU)){
			return;
		}
		
		if (!addPDUToReassemblyQueue(currentPDU)){
			return;
		}
		
		checkSequenceNumberRollover(currentPDU);
		
		List<PDU> completeSDUs = processReassemblyQueue();
		
		postCompleteSDUs(completeSDUs);
		
		updateTimers();
	}
	
	/**
	 * Checks if the PDU was already delivered
	 * @param pdu
	 * @return
	 */
	private boolean pduAlreadyDelivered(PDU pdu){
		//If this PDU has already been delivered, it's either duplicate or 
		//it's in a gap that we've already passed over
		if (pdu.getSequenceNumber().getValue() < stateVector.getLastSequenceDelivered().getValue()){
			//TODO Drop PDU and increment counter of dropped duplicates
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Add a pdu to the reassembly queue if it is not already there
	 * @param pdu
	 * @return true if the pdu was added, false otherwise
	 */
	private boolean addPDUToReassemblyQueue(PDU pdu){
		//Ditto if it is on the Reassembly queue already. We notice that 
		//when we find where to insert this PDU
		//***Defer starting reassembly timer until later; this may not be a fragment or may be 
		//***a fragment that completes an SDU
		if (stateVector.getReasemblyQeueue().contains(pdu)){
			//TODO drop PDU
			return false;
		}else{
			stateVector.getReasemblyQeueue().add(pdu);
			return true;
		}
	}
	
	private void checkSequenceNumberRollover(PDU pdu){
		//If we are encrypting, we can't let PDU sequence numbers roll over.
		//***define exactly what the Flow Allocator needs to do
		if (dataTransferConstants.isDIFIntegrity() && 
				pdu.getSequenceNumber().getValue() > stateVector.getSequenceNumberRollOverThreshold().getValue()){
			//Security requires a new flow
			//TODO requestFAICraeteNewConnection(connectionID)
		}
	}
	
	/**
	 * Look for complete SDUs at the reassembly queue, and pop them from the queue into
	 * completeSDUs
	 * @returns completeSDUs
	 */
	private List<PDU> processReassemblyQueue(){
		PDU currentPDU = null;
		boolean reassembling = false;
		List<PDU> pdusInQueue = stateVector.getReasemblyQeueue().getQueue();
		Unsigned nextSequenceNumber = stateVector.getLastSequenceDelivered().clone();
		nextSequenceNumber.increment();
		List<PDU> completeSDUs = new ArrayList<PDU>();
		
		//We've added a new PDU to the reassembly queue. Collect PDUs fom the reassembly queue until 
		//we either reach an incomplete SDU or the gap to the next SDU is too large
		for(int i=0; i<pdusInQueue.size(); i++){
			currentPDU = pdusInQueue.get(i);
			if (reassembling){
				//If we are reassembling, then PDU sequence numbers must be sequential
				if (currentPDU.getSequenceNumber().equals(nextSequenceNumber)){
					//This is the next expected fragment, accept it
					nextSequenceNumber.increment();
					//If this is the last fragment, we have a complete SDU, assuming 0x01 means 
					if (currentPDU.getFlags().getValue() == dataTransferConstants.getLastFragmentFlag()){
						reassembling = false;
						if ((currentPDU.getSequenceNumber().getValue() - stateVector.getLastSequenceDelivered().getValue()) 
								> stateVector.getConnection().getMaxGapAllowed()){
							break;
						}else{
							//TODO if application is ready to accept data pop PDUs from connection.reassemblyQueue
							//on to complete SDUs up to and including this one
						}
					}else if (currentPDU.getFlags().getValue() != dataTransferConstants.getFragmentFlag()){
						//Sanity check, we have received a FIRST_FRAGMENT, this sequence in in order
						//and is neither a FRAGMENT or a LAST_FRAGMENT
						//TODO signal and/or log a protocol error
					}
				}else{
					//We are missing a fragment, nothing more to do
					break;
				}
			}else{
				if (currentPDU.getFlags().getValue() == dataTransferConstants.getFirstFragmentFlag()){
					//We have the first fragment of an SDU
					reassembling = true;
					nextSequenceNumber.increment();
				}else if (currentPDU.getFlags().getValue() == dataTransferConstants.getCompleteFlag() ||
						currentPDU.getFlags().getValue() == dataTransferConstants.getMultipleFlag()){
					//TODO check the next conditional
					if ((currentPDU.getSequenceNumber().getValue() - stateVector.getLastSequenceDelivered().getValue()) 
							> stateVector.getConnection().getMaxGapAllowed()){
						//Complete SDU(s), but too far away. Wait for gap to fill
						break;
					}else{
						//pop first PDU from connection.reassemblyQueue to complete SDUs
						completeSDUs.add(currentPDU);
						pdusInQueue.remove(currentPDU);
						nextSequenceNumber.increment();
					}
				}else{
					//We are missing the first fragment of an SDU. Nothing more to do
					break;
				}
			}
		}
		
		return completeSDUs;
	}
	
	/**
	 * Post the complete SDUs to the port-id and update the SDUGapTimer and DTCP state if present
	 * @param completeSDUs
	 */
	private void postCompleteSDUs(List<PDU> pdus){
		PDU currentPDU = null;
		List<byte[]> sdus = new ArrayList<byte[]>();
		
		for(int i=0; i<pdus.size(); i++){
			currentPDU = pdus.get(i);
			stateVector.getLastSequenceDelivered().setValue(currentPDU.getSequenceNumber().getValue());
			//TODO cancel reassembly timer associated with this PDU
			//TODO post all SDUs in this PDU to port-id, dealing with the reassembly, complete and multiple SDUs cases
			//TODO, deal with reassembly
			sdus.addAll(currentPDU.getUserData());
		}
		
		ipcProcess.deliverSDUsToApplicationProcess(sdus, 
				new Long(stateVector.getConnection().getSourcePortId()).intValue());
		
		//We have delivered some SDUs. That satisfies the gap timer - for now.
		if (stateVector.getConnection().getSduGapTimer() != null){
			stateVector.getConnection().getSduGapTimer().cancel();
			stateVector.getConnection().setSduGapTimer(null);
		}
		
		//Tell DTCP we've moved the left edge of the receive window by delivering one or more SDUs
		//TODO if DTPCP present then update state vector
	}
	
	/**
	 * Update the reassembly timers and SDUGapTimer
	 */
	private void updateTimers(){
		List<PDU> pdusInQueue = stateVector.getReasemblyQeueue().getQueue();
		
		//If there are still PDUs in the reassembly queue, deal with timers.
		if (pdusInQueue.size() >0){
			//TODO for (int i=0; i<pdusInQueue.size(); i++){
				//TODO Ensure that fragments of incomplete SDUs have reassembly timer
				//TODO Ensure that fragments of complete SDUs do not have a reassembly timer
			//TODO}
			
			if ((pdusInQueue.get(0).getSequenceNumber().getValue() - stateVector.getLastSequenceDelivered().getValue()) 
					> stateVector.getConnection().getMaxGapAllowed()){
				//If we are not currently running the SDUGapTimer start it now
				//We canceled it above, if we delivered SDUs
				if (stateVector.getConnection().getSduGapTimer() == null){
					Timer sduGapTimer = new Timer();
					sduGapTimer.schedule(new SDUGapTimerTask(this), dataTransferConstants.getSDUGapTimerDelay());
					stateVector.getConnection().setSduGapTimer(sduGapTimer);
				}
			}
		}
	}

	/**
	 * @see DataTransferAEInstance.sdusDelivered
	 */
	public void sdusDelivered(List<byte[]> sdus) {
		//Iterate over SDUs and generate PDUs
		//PDU sequence numbers start at stateVector.NextSequenceToSend
		List<PDU> generatedPDUs = new ArrayList<PDU>();
		PDU currentPDU = null;
		
		for(int i=0; i<sdus.size(); i++){
			currentPDU = processCurrentSDU(sdus.get(i), currentPDU, generatedPDUs);
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
			RMT rmt = (RMT) ipcProcess.getIPCProcessComponent(RMT.class.getName());
			rmt.sendEFCPPDU(postablePDUs.get(i).getSerializedPDU());
		}
		
	}
	
	/**
	 * Processes and SDU to be delivered to the RMT (the result is one or more PDUs being generated)
	 * @param currentSDU the SDU to be processed
	 * @param currentPDU the current PDU being assembled
	 * @param generatedPDUs the list of generated PDUs
	 */
	private PDU processCurrentSDU(byte[] currentSDU, PDU currentPDU, List<PDU> generatedPDUs){
		if (currentPDU != null){
			if (dataTransferConstants.isDIFConcatenation() && 
					currentSDU.length < (stateVector.getMaxFlowPDUSize() - currentPDU.getPduLength())){
				//There is room in the current PDU for this SDU
				currentPDU.appendSDU(currentSDU);
			}else{
				//The current SDU is too big for the current PDU
				generatedPDUs.add(currentPDU);
				currentPDU = null;
			}
		}
		
		if (currentPDU == null){
			if (currentSDU.length > (stateVector.getMaxFlowPDUSize() - dataTransferConstants.getPciLength())){
				//Fragmentation can roll over to the next flow in the connection, which must already be allocated
				//TODO create complete PDUs including SDU protection, assigning PDU sequence numbers
				//sequentially starting from StateVector.NextSequenceToSend
				//Increment StateVector.nextSequenceToSend by number of PDUs created
				//Add the generated PDUs to genereatedPDUs
			}else{
				currentPDU = createNewPDU(currentSDU);
			}
		}
		
		return currentPDU;
	}
	
	/**
	 * Creates and initialize a new PDU that will carry the passed SDU.
	 * @param sdu
	 * @return
	 */
	private PDU createNewPDU(byte[] sdu){
		PDU pdu = new PDU(stateVector.getConnection(), dataTransferConstants);
		pdu.setSequenceNumber(stateVector.getNextSequenceToSend().clone());
		pdu.appendSDU(sdu);
		stateVector.getNextSequenceToSend().increment();
		
		return pdu;
	}

	/**
	 * @see DataTransferAEInstance.getConnection
	 */
	public Connection getConnection() {
		return stateVector.getConnection();
	}
	
	/**
	 * @see DataTransferAEInstance.sduGapTimerFired
	 */
	public void sduGapTimerFired() {
		// TODO invoke the SDU gap timer policy
		//Typically, the action would be to signal an error or abort the flow
		
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
}