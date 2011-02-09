package rina.efcp.impl;

import rina.efcp.api.EFCPConstants;
import rina.flowallocator.api.Connection;
import rina.utils.types.Unsigned;

/**
 * The data associated with a single active flow
 * @author eduardgrasa
 *
 */
public class DTAEIState {
	
	/**
	 * Pointer to the connection state
	 */
	private Connection connection = null;
	
	/**
	 * The reassembly queue, should go into connection 
	 * but I'll leave it here for now (if not PDU has to 
	 * got to the flow allocator API)
	 */
	private ReassemblyQueue reasemblyQeueue = null;
	
	/**
	 * The maximum length of an SDU for this flow, in bytes
	 */
	private int maxFlowSDU = EFCPConstants.maxPDUSize;
	
	/**
	 * The maximum length of a PDU for this flow, in bytes
	 */
	private int maxFlowPDUSize = EFCPConstants.maxSDUSize;
	
	/**
	 * The initial sequence number for the PDUs of this flow. It is 
	 * always 0.
	 */
	private Unsigned initialSequenceNumber = null;
	
	/**
	 * After this sequence number, EFCP will ask the FAI to create another 
	 * connection for this flow, so that sequence numbers do not repeat
	 */
	private Unsigned sequenceNumberRollOverThreshold = null;
	
	/**
	 * The state
	 */
	private String state = null;
	
	/**
	 * NOTE: Will be implemented in the future, after the demo
	 */
	//private DTCPStateVector dtcpStateVector = null;
	
	/**
	 * The sequence number of the PDU received in order, if applicable
	 */
	private Unsigned lastSequenceDelivered = null;
	
	/**
	 * The value of the next sequence number to be assigned to a 
	 * PDU being sent on this connection
	 */
	private Unsigned nextSequenceToSend = null;
	
	/**
	 * The highest sequence number that the remote application is currently 
	 * willing to accept on this connection.
	 */
	private Unsigned rightWindowEdge = null;
	
	/**
	 * The queue of PDUs that have been handed off to the RMT but not yet acknowledged.
	 * NOTE: The String type is just a placeholder, obviously it will be replaced by 
	 * the real type
	 */
	private String retransmissionQueue = null;
	
	/**
	 * The queue of PDUs ready to be sent once the window opens.
	 * NOTE: The String type is just a placeholder, obviously it will be replaced by 
	 * the real type
	 */
	private String closedWindowQueue = null;
	
	public DTAEIState(Connection connection){
		this.connection = connection;
		this.reasemblyQeueue = new ReassemblyQueue();
		this.sequenceNumberRollOverThreshold = new Unsigned(EFCPConstants.SequenceNumberLength);
		this.sequenceNumberRollOverThreshold.setMaxValue();	
		this.initialSequenceNumber = new Unsigned(EFCPConstants.SequenceNumberLength, 0x00);
		this.lastSequenceDelivered = new Unsigned(EFCPConstants.SequenceNumberLength, 0x00);
		this.nextSequenceToSend = new Unsigned(EFCPConstants.SequenceNumberLength, 0x01);
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public ReassemblyQueue getReasemblyQeueue() {
		return reasemblyQeueue;
	}

	public void setReasemblyQeueue(ReassemblyQueue reasemblyQeueue) {
		this.reasemblyQeueue = reasemblyQeueue;
	}

	public int getMaxFlowSDU() {
		return maxFlowSDU;
	}

	public void setMaxFlowSDU(int maxFlowSDU) {
		this.maxFlowSDU = maxFlowSDU;
	}

	public int getMaxFlowPDUSize() {
		return maxFlowPDUSize;
	}

	public void setMaxFlowPDUSize(int maxFlowPDUSize) {
		this.maxFlowPDUSize = maxFlowPDUSize;
	}

	public Unsigned getInitialSequenceNumber() {
		return initialSequenceNumber;
	}

	public void setInitialSequenceNumber(Unsigned initialSequenceNumber) {
		this.initialSequenceNumber = initialSequenceNumber;
	}

	public Unsigned getSequenceNumberRollOverThreshold() {
		return sequenceNumberRollOverThreshold;
	}

	public void setSequenceNumberRollOverThreshold(Unsigned sequenceNumberRollOverThreshold) {
		this.sequenceNumberRollOverThreshold = sequenceNumberRollOverThreshold;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Unsigned getLastSequenceDelivered() {
		return lastSequenceDelivered;
	}

	public void setLastSequenceDelivered(Unsigned lastSequenceDelivered) {
		this.lastSequenceDelivered = lastSequenceDelivered;
	}

	public Unsigned getNextSequenceToSend() {
		return nextSequenceToSend;
	}

	public void setNextSequenceToSend(Unsigned nextSequenceToSend) {
		this.nextSequenceToSend = nextSequenceToSend;
	}

	public Unsigned getRightWindowEdge() {
		return rightWindowEdge;
	}

	public void setRightWindowEdge(Unsigned rightWindowEdge) {
		this.rightWindowEdge = rightWindowEdge;
	}

	public String getRetransmissionQueue() {
		return retransmissionQueue;
	}

	public void setRetransmissionQueue(String retransmissionQueue) {
		this.retransmissionQueue = retransmissionQueue;
	}

	public String getClosedWindowQueue() {
		return closedWindowQueue;
	}

	public void setClosedWindowQueue(String closedWindowQueue) {
		this.closedWindowQueue = closedWindowQueue;
	}
}