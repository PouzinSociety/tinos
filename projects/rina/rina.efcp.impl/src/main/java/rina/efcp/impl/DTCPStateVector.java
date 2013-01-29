package rina.efcp.impl;

public class DTCPStateVector {
	private boolean flowControlEnabled = false;
	private boolean transmissionControlEnabled = false;
	private boolean retransmissionControlEnabled = false;
	
	/**
	 * The type of flow control
	 */
	private String flowControlType = null;
	
	/**
	 * The highest sequence number that the remote application is currently 
	 * willing to accept on this connection.
	 */
	private long sendRightWindowEdge = 0L;
	
	private long receiveRightWindowEdge = 0L;
	
	/**
	 * The PCI of a flowControl Only PDU
	 */
	private byte[] flowControlOnlyPCI = null;
	
	/**
	 * The value of the next sequence number to be assigned to a 
	 * DTCP PDU being sent on this connection
	 */
	private long nextSequenceToSend = 0L;
	
	public DTCPStateVector(){
	}
	
	public byte[] getFlowControlOnlyPCI() {
		return flowControlOnlyPCI;
	}

	public void setFlowControlOnlyPCI(byte[] flowControlOnlyPCI) {
		this.flowControlOnlyPCI = flowControlOnlyPCI;
	}

	public boolean isFlowControlEnabled() {
		return flowControlEnabled;
	}
	
	public void setFlowControlEnabled(boolean flowControlEnabled) {
		this.flowControlEnabled = flowControlEnabled;
	}
	
	public boolean isTransmissionControlEnabled() {
		return transmissionControlEnabled;
	}
	
	public void setTransmissionControlEnabled(boolean transmissionControlEnabled) {
		this.transmissionControlEnabled = transmissionControlEnabled;
	}
	
	public boolean isRetransmissionControlEnabled() {
		return retransmissionControlEnabled;
	}
	
	public void setRetransmissionControlEnabled(boolean retransmissionControlEnabled) {
		this.retransmissionControlEnabled = retransmissionControlEnabled;
	}

	public String getFlowControlType() {
		return flowControlType;
	}

	public void setFlowControlType(String flowControlType) {
		this.flowControlType = flowControlType;
	}

	public long getSendRightWindowEdge() {
		return sendRightWindowEdge;
	}

	public void setSendRightWindowEdge(long sendRightWindowEdge) {
		this.sendRightWindowEdge = sendRightWindowEdge;
	}

	public long getReceiveRightWindowEdge() {
		return receiveRightWindowEdge;
	}

	public void setReceiveRightWindowEdge(long receiveRightWindowEdge) {
		this.receiveRightWindowEdge = receiveRightWindowEdge;
	}

	public long getNextSequenceToSend() {
		return nextSequenceToSend;
	}

	public void incrementNextSequenceToSend(){
		this.nextSequenceToSend++;
	}
}
