package rina.flowallocator.api;

/**
 * Encapsulates the data that identifies a connection
 * @author eduardgrasa
 *
 */
public class ConnectionId {
	/**
	 * A DIF-assigned identifier only known within the DIF that stands for a 
	 * particular QoS hypercube.
	 */
	private long qosId = 0;
	
	/**
	 * An identifier unique within the DT-AEI of the source IPC Process that identifies 
	 * the source endpoint of this connection
	 */
	private long sourceCEPId = 0;
	
	/**
	 * An identifier unique within the DT-AEI of the destination IPC Process that identifies 
	 * the destination endpoint of this connection
	 */
	private long destinationCEPId = 0;

	public long getQosId() {
		return qosId;
	}

	public void setQosId(long qosId) {
		this.qosId = qosId;
	}

	public long getSourceCEPId() {
		return sourceCEPId;
	}

	public void setSourceCEPId(long sourceCEPId) {
		this.sourceCEPId = sourceCEPId;
	}

	public long getDestinationCEPId() {
		return destinationCEPId;
	}

	public void setDestinationCEPId(long destinationCEPId) {
		this.destinationCEPId = destinationCEPId;
	}
	
	@Override
	public boolean equals(Object candidate){
		if (candidate == null){
			return false;
		}
		
		if (!(candidate instanceof ConnectionId)){
			return false;
		}
		
		ConnectionId connectionId = (ConnectionId) candidate;
		
		return (connectionId.getDestinationCEPId() == this.getDestinationCEPId() &&
				connectionId.getSourceCEPId() == this.getSourceCEPId() && 
				connectionId.getQosId() == this.getQosId());
	}
}