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
	private int[] qosId = null;
	
	/**
	 * An identifier unique within the DT-AEI of the source IPC Process that identifies 
	 * the source endpoint of this connection
	 */
	private int[] sourceCEPId = null;
	
	/**
	 * An identifier unique within the DT-AEI of the destination IPC Process that identifies 
	 * the destination endpoint of this connection
	 */
	private int[] destinationCEPId = null;

	public int[] getQosId() {
		return qosId;
	}

	public void setQosId(int[] qosId) {
		this.qosId = qosId;
	}

	public int[] getSourceCEPId() {
		return sourceCEPId;
	}

	public void setSourceCEPId(int[] sourceCEPId) {
		this.sourceCEPId = sourceCEPId;
	}

	public int[] getDestinationCEPId() {
		return destinationCEPId;
	}

	public void setDestinationCEPId(int[] destinationCEPId) {
		this.destinationCEPId = destinationCEPId;
	}
}