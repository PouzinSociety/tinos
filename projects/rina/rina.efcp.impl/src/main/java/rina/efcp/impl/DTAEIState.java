package rina.efcp.impl;

import java.net.Socket;

import rina.efcp.api.DataTransferConstants;
import rina.flowallocator.api.ConnectionId;
import rina.flowallocator.api.Flow;

/**
 * The data associated with a single active flow
 * @author eduardgrasa
 *
 */
public class DTAEIState {
	
	/**
	 * Pointer to the connection state
	 */
	private Flow flow = null;
	
	/**
	 * The reassembly queue, should go into Flow 
	 * but I'll leave it here for now (if not PDU has to 
	 * got to the flow allocator API)
	 */
	private ReassemblyQueue reasemblyQeueue = null;
	
	/**
	 * The maximum length of an SDU for this flow, in bytes
	 */
	private int maxFlowSDUSize = 0;
	
	/**
	 * The maximum length of a PDU for this flow, in bytes
	 */
	private int maxFlowPDUSize = 0;
	
	/**
	 * After this sequence number, EFCP will ask the FAI to create another 
	 * connection for this flow, so that sequence numbers do not repeat
	 */
	private long sequenceNumberRollOverThreshold = Long.MAX_VALUE;
	
	/**
	 * The state
	 */
	private String state = null;
	
	/**
	 * NOTE: Will be implemented in the future, after the demo
	 */
	//private DTCPStateVector dtcpStateVector = null;
	
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
	
	/**
	 * The id of this connection
	 */
	private ConnectionId connectionId = null;
	
	/**
	 * The initial sequence number for the PDUs of this flow. It is 
	 * always 0.
	 */
	private long initialSequenceNumber = 0L;
	
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
	 * The precomputed PCI, an optimization for the PDU Generator
	 */
	private byte[] preComputedPCI = null;
	
	private long sourceAddress = 0;
	private long destinationAddress = 0;
	
	/**
	 * The socket used to transmit the PDU
	 */
	private Socket socket = null;
	
	/**
	 * The portId associated to the flow in this IPC Process
	 */
	private long portId = 0;
	
	public DTAEIState(Flow flow, DataTransferConstants dataTransferConstants){
		this.flow = flow;
		this.reasemblyQeueue = new ReassemblyQueue();
		this.maxFlowSDUSize = dataTransferConstants.getMaxSDUSize();
		this.maxFlowPDUSize = dataTransferConstants.getMaxPDUSize();
		this.connectionId = flow.getConnectionIds().get(flow.getCurrentConnectionIdIndex());
		if (flow.isSource()){
			this.sourceAddress = flow.getSourceAddress();
			this.destinationAddress = flow.getDestinationAddress();
			this.portId = flow.getSourcePortId();
		}else{
			this.sourceAddress = flow.getDestinationAddress();
			this.destinationAddress = flow.getSourceAddress();
			this.portId = flow.getDestinationPortId();
		}
		this.preComputedPCI = PDUParser.computePCI(this.destinationAddress, this.sourceAddress, this.connectionId);
	}
	
	public byte[] getPreComputedPCI(){
		return this.preComputedPCI;
	}
	
	public long getPortId(){
		return this.portId;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public Flow getFlow() {
		return flow;
	}
	
	public long getInitialSequenceNumber() {
		return initialSequenceNumber;
	}

	public void setInitialSequenceNumber(long initialSequenceNumber) {
		this.initialSequenceNumber = initialSequenceNumber;
	}

	public void setLastSequenceDelivered(long lastSequenceDelivered) {
		this.lastSequenceDelivered = lastSequenceDelivered;
	}
	
	public void incrementLastSequenceDelivered(){
		this.lastSequenceDelivered++;
	}

	public void setNextSequenceToSend(long nextSequenceToSend) {
		this.nextSequenceToSend = nextSequenceToSend;
	}

	public ConnectionId getConnectionId(){
		return this.connectionId;
	}

	public ReassemblyQueue getReasemblyQeueue() {
		return reasemblyQeueue;
	}

	public void setReasemblyQeueue(ReassemblyQueue reasemblyQeueue) {
		this.reasemblyQeueue = reasemblyQeueue;
	}

	public int getMaxFlowSDUSize() {
		return this.maxFlowSDUSize;
	}

	public void setMaxFlowSDUSize(int maxFlowSDUSize) {
		this.maxFlowSDUSize = maxFlowSDUSize;
	}

	public int getMaxFlowPDUSize() {
		return maxFlowPDUSize;
	}

	public void setMaxFlowPDUSize(int maxFlowPDUSize) {
		this.maxFlowPDUSize = maxFlowPDUSize;
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
		return this.lastSequenceDelivered;
	}

	public long getNextSequenceToSend() {
		return this.nextSequenceToSend;
	}
	
	public void incrementNextSequenceToSend(){
		this.nextSequenceToSend++;
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