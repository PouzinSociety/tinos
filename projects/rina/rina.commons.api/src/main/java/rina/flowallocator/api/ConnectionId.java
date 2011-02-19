package rina.flowallocator.api;

import rina.utils.types.Unsigned;

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
	private Unsigned qosId = null;
	
	/**
	 * An identifier unique within the DT-AEI of the source IPC Process that identifies 
	 * the source endpoint of this connection
	 */
	private Unsigned sourceCEPId = null;
	
	/**
	 * An identifier unique within the DT-AEI of the destination IPC Process that identifies 
	 * the destination endpoint of this connection
	 */
	private Unsigned destinationCEPId = null;

	public Unsigned getQosId() {
		return qosId;
	}

	public void setQosId(Unsigned qosId) {
		this.qosId = qosId;
	}

	public Unsigned getSourceCEPId() {
		return sourceCEPId;
	}

	public void setSourceCEPId(Unsigned sourceCEPId) {
		this.sourceCEPId = sourceCEPId;
	}

	public Unsigned getDestinationCEPId() {
		return destinationCEPId;
	}

	public void setDestinationCEPId(Unsigned destinationCEPId) {
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
		
		return (connectionId.getDestinationCEPId().equals(this.getDestinationCEPId()) &&
				connectionId.getSourceCEPId().equals(this.getSourceCEPId()) && 
				connectionId.getQosId().equals(this.getQosId()));
	}
}