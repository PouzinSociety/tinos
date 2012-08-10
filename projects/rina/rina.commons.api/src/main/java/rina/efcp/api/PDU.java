package rina.efcp.api;

import java.util.ArrayList;
import java.util.List;

import rina.flowallocator.api.ConnectionId;

/**
 * An EFCP PDU, consisting of the PCI plus user data
 * @author eduardgrasa
 *
 */
public class PDU {
	
	/**
	 * The PDU before parsing it
	 */
	private byte[] rawPDU = null;
	
	/**
	 * A synonym for the application process name designating an IPC process 
	 * with scope limited to the DIF and a binding to the source application process
	 */
	private long sourceAddress = -1L;
	
	/**
	 * A synonym for the application process name designating an IPC process 
	 * with scope limited to the DIF and a binding to the destination application process
	 */
	private long destinationAddress = -1L;
	
	/**
	 * A three part identifier unambiguous within the scope of two communicating 
	 * IPC processes used to distinguish connections between them.
	 */
	private ConnectionId connectionId = null;
	
	/**
	 * The field indicates the type of PDU.
	 */
	private int pduType = -1;
	
	/**
	 * This field indicates conditions that affect the handling of the PDU. Flags should only indicate 
	 * conditions that can change from one PDU to the next. Conditions that are invariant over the life 
	 * of the connection should be established during allocation or by the action of management. The 
	 * interpretation of the flags depends on the PDU Type.
	 */
	private int flags = 0;
	
	/**
	 * The PDU sequenceNumber
	 */
	private long sequenceNumber = 0;
	
	/**
	 * This field contains one or more octets that are uninterpreted by EFCP. This field contains a 
	 * SDU-Fragment or one or more Delimited-SDUs up to the MaxPDUSize. PDUs containing SDU Fragments other 
	 * than the last fragment should be MaxPDUSize. (Because the PCI is fixed length a field is not 
	 * required to specify the length of the last fragment).
	 */
	private List<byte[]> userData = null;
	
	
	public PDU(){
		userData = new ArrayList<byte[]>();
	}

	public long getSourceAddress() {
		return sourceAddress;
	}

	public void setSourceAddress(long sourceAddress) {
		this.sourceAddress = sourceAddress;
	}

	public long getDestinationAddress() {
		return destinationAddress;
	}

	public void setDestinationAddress(long destinationAddress) {
		this.destinationAddress = destinationAddress;
	}

	public ConnectionId getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(ConnectionId connectionId) {
		this.connectionId = connectionId;
	}

	public int getPduType() {
		return pduType;
	}

	public void setPduType(int pduType) {
		this.pduType = pduType;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public byte[] getRawPDU() {
		return rawPDU;
	}

	public void setRawPDU(byte[] rawPDU) {
		this.rawPDU = rawPDU;
	}

	public long getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public List<byte[]> getUserData() {
		return userData;
	}

	public void setUserData(List<byte[]> userData) {
		this.userData = userData;
	}
	
	public boolean equals(Object candidate){
		if (candidate == null){
			return false;
		}
		
		if (!(candidate instanceof PDU)){
			return false;
		}
		
		PDU pdu = (PDU) candidate;
		return this.getSequenceNumber() == pdu.getSequenceNumber();
	}
	
	@Override
	public String toString(){
		String result = "";
		result = result + "Destination @: " +this.destinationAddress + " CEPid: " + this.connectionId.getDestinationCEPId() + 
			" Source @: " +this.sourceAddress + " CEPid: " +this.connectionId.getSourceCEPId() + "\n" + 
			" QoSid: " +this.connectionId.getQosId() + " PDU type: " +this.getPduType() + 
			" Flags: " +this.flags + " Sequence number: " +this.sequenceNumber;
		
		return result;
	}
}