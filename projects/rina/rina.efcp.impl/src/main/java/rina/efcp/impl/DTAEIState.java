package rina.efcp.impl;

import rina.efcp.api.EFCPConstants;
import rina.flowallocator.api.Connection;

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
	private long initialSequenceNumber = 0L;
	
	/**
	 * After this sequence number, EFCP will ask the FAI to create another 
	 * connection for this flow, so that sequence numbers do not repeat
	 */
	private long sequenceNumberRollOverThreshold = Long.MAX_VALUE/2 -1;
	
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
	private long lastSequenceDelivered = 0L;
	
	/**
	 * The value of the next sequence number to be assigned to a 
	 * PDU being sent on this connection
	 */
	private long nextSequenceToSend = 0L;
	
	/**
	 * The highest sequence number that the remote application is currently 
	 * willing to accept on this connection.
	 */
	private long rightWindowEdge = 0L;
	
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

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
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

	public long getInitialSequenceNumber() {
		return initialSequenceNumber;
	}

	public void setInitialSequenceNumber(long initialSequenceNumber) {
		this.initialSequenceNumber = initialSequenceNumber;
	}

	public long getSequenceNumberRollOverThreshold() {
		return sequenceNumberRollOverThreshold;
	}

	public void setSequenceNumberRollOverThreshold(long sequenceNumberRollOverThreshold) {
		this.sequenceNumberRollOverThreshold = sequenceNumberRollOverThreshold;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public long getLastSequenceDelivered() {
		return lastSequenceDelivered;
	}

	public void setLastSequenceDelivered(long lastSequenceDelivered) {
		this.lastSequenceDelivered = lastSequenceDelivered;
	}

	public long getNextSequenceToSend() {
		return nextSequenceToSend;
	}

	public void setNextSequenceToSend(long nextSequenceToSend) {
		this.nextSequenceToSend = nextSequenceToSend;
	}

	public long getRightWindowEdge() {
		return rightWindowEdge;
	}

	public void setRightWindowEdge(long rightWindowEdge) {
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