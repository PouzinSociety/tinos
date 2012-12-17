package rina.efcp.impl;

import rina.efcp.api.DataTransferConstants;
import rina.efcp.api.EFCPPolicyConstants;
import rina.efcp.api.PDUParser;
import rina.flowallocator.api.ConnectionId;
import rina.flowallocator.api.Flow;
import rina.ipcservice.api.APService;

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
	 * The callback to the local application
	 */
	private APService applicationCallback = null;
	
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
	 * DTCP State Vector
	 */
    private DTCPStateVector dtcpStateVector = null;
	
	/**
	 * The queue of PDUs that have been handed off to the RMT but not yet acknowledged.
	 * NOTE: The String type is just a placeholder, obviously it will be replaced by 
	 * the real type
	 */
	private String retransmissionQueue = null;
	
	/**
	 * The source Connection Endpoint ID
	 */
	private long sourceCEPid = 0L;
	
	/**
	 * The destination Connection Endpoint ID
	 */
	private long destinationCEPid = 0L;
	
	/**
	 * The QoS id
	 */
	private int qosid = 0;
	
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
	 * The portId associated to the flow in this IPC Process
	 */
	private long portId = 0;
	
	/**
	 * True if this is a local connection;
	 */
	private boolean local = false;
	
	/**
	 * In case this is a connection supporting a local flow, this
	 * is the remote port Id
	 */
	private int remotePortId = 0;
	
	/**
	 * The constructor used for connections supporting local flows
	 * @param remotePortId
	 */
	public DTAEIState(int portId, int remotePortId){
		this.local = true;
		this.portId = portId;
		this.remotePortId = remotePortId;
	}
	
	/**
	 * The constructor used for connections supporting remote flows
	 * @param flow
	 * @param dataTransferConstants
	 */
	public DTAEIState(Flow flow, DataTransferConstants dataTransferConstants, PDUParser pduParser){
		this.flow = flow;
		ConnectionId connectionId = flow.getConnectionIds().get(flow.getCurrentConnectionIdIndex());
		this.qosid = connectionId.getQosId(); 
		if (flow.isSource()){
			this.sourceAddress = flow.getSourceAddress();
			this.destinationAddress = flow.getDestinationAddress();
			this.portId = flow.getSourcePortId();
			this.sourceCEPid = connectionId.getSourceCEPId();
			this.destinationCEPid = connectionId.getDestinationCEPId();
		}else{
			this.sourceAddress = flow.getDestinationAddress();
			this.destinationAddress = flow.getSourceAddress();
			this.portId = flow.getDestinationPortId();
			this.sourceCEPid = connectionId.getDestinationCEPId();
			this.destinationCEPid = connectionId.getSourceCEPId();
		}
		this.reasemblyQeueue = new ReassemblyQueue();
		this.maxFlowSDUSize = dataTransferConstants.getMaxSDUSize();
		this.maxFlowPDUSize = dataTransferConstants.getMaxPDUSize();
		this.preComputedPCI = pduParser.preComputePCI(this.destinationAddress, 
				this.sourceAddress, this.sourceCEPid, this.destinationCEPid, this.qosid);

		this.dtcpStateVector = new DTCPStateVector();
		String flowControl = flow.getPolicies().get(EFCPPolicyConstants.DTCP_FLOW_CONTROL);
		if (flowControl != null){
			this.dtcpStateVector.setFlowControlEnabled(true);
			if (dtcpStateVector.isFlowControlEnabled()){
				this.dtcpStateVector.setFlowControlType(flowControl);
				this.dtcpStateVector.setReceiveRightWindowEdge(50);
				this.dtcpStateVector.setSendRightWindowEdge(50);
				this.dtcpStateVector.setFlowControlOnlyPCI(pduParser.preComputePCI(this.destinationAddress, this.sourceAddress, 
						this.sourceCEPid, this.destinationCEPid, this.qosid));
			}
		}
	}
	
	/**
	 * Returns whether we're allowed or not to send a PDU (determined by flow control)
	 * @return
	 */
	public boolean canSend(){
		if (!this.dtcpStateVector.isFlowControlEnabled()){
			return true;
		}
		
		if (this.dtcpStateVector.getFlowControlType().equals(EFCPPolicyConstants.CREDIT)){
			return this.nextSequenceToSend <= this.dtcpStateVector.getSendRightWindowEdge();
		}
		
		return true;
	}

	public APService getApplicationCallback() {
		return applicationCallback;
	}

	public void setApplicationCallback(APService applicationCallback) {
		this.applicationCallback = applicationCallback;
	}
	
	public DTCPStateVector getDTCPStateVector(){
		return this.dtcpStateVector;
	}
	
	public long getSourceAddress(){
		return this.sourceAddress;
	}
	
	public long getDestinationAddress(){
		return this.destinationAddress;
	}
	
	public long getSourceCEPid(){
		return this.sourceCEPid;
	}
	
	public long getDestinationCEPid(){
		return this.destinationCEPid;
	}
	
	public int getQoSId(){
		return this.qosid;
	}

	public boolean isLocal(){
		return this.local;
	}
	
	public int getRemotePortId(){
		return this.remotePortId;
	}
	
	public byte[] getPreComputedPCI(){
		return this.preComputedPCI;
	}
	
	public long getPortId(){
		return this.portId;
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

	public String getRetransmissionQueue() {
		return retransmissionQueue;
	}

	public void setRetransmissionQueue(String retransmissionQueue) {
		this.retransmissionQueue = retransmissionQueue;
	}
}