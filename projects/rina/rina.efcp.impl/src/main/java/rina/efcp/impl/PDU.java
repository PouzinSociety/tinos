package rina.efcp.impl;

import java.nio.ByteBuffer;

import rina.flowallocator.api.Connection;
import rina.flowallocator.api.ConnectionId;

/**
 * An EFCP PDU, consisting of the PCI plus user data
 * @author eduardgrasa
 *
 */
public class PDU {
	
	/**
	 * An identifier indicating the version of the protocol, seems prudent
	 */
	private byte version = 0x01;
	
	/**
	 * A synonym for the application process name designating an IPC process 
	 * with scope limited to the DIF and a binding to the source application process
	 */
	private byte[] sourceAddress = null;
	
	/**
	 * A synonym for the application process name designating an IPC process 
	 * with scope limited to the DIF and a binding to the destination application process
	 */
	private byte[] destinationAddress = null;
	
	/**
	 * A three part identifier unambiguous within the scope of two communicating 
	 * IPC processes used to distinguish connections between them.
	 */
	private ConnectionId connectionId = null;
	
	/**
	 * The field indicates the type of PDU.
	 */
	private byte pduType = 0x00;
	
	/**
	 * This field indicates conditions that affect the handling of the PDU. Flags should only indicate 
	 * conditions that can change from one PDU to the next. Conditions that are invariant over the life 
	 * of the connection should be established during allocation or by the action of management. The 
	 * interpretation of the flags depends on the PDU Type.
	 */
	private byte flags = 0x00;
	
	/**
	 * The total length of the PDU in bytes
	 */
	private int[] pduLength = null;
	
	/**
	 * Sequence number of the PDU
	 */
	private int[] sequenceNumber = null;
	
	/**
	 * This field contains one or more octets that are uninterpreted by EFCP. This field contains a 
	 * SDU-Fragment or one or more Delimited-SDUs up to the MaxPDUSize. PDUs containing SDU Fragments other 
	 * than the last fragment should be MaxPDUSize. (Because the PCI is fixed length a field is not 
	 * required to specify the length of the last fragment).
	 */
	private ByteBuffer userData = null;
	
	public PDU(Connection connection){
		this.connectionId = connection.getCurrentConnectionId();
		this.sourceAddress = connection.getSourceAddress();
		this.destinationAddress = connection.getDestinationAddress();
	}

	public byte getVersion() {
		return version;
	}

	public void setVersion(byte version) {
		this.version = version;
	}

	public byte[] getSourceAddress() {
		return sourceAddress;
	}

	public void setSourceAddress(byte[] sourceAddress) {
		this.sourceAddress = sourceAddress;
	}

	public byte[] getDestinationAddress() {
		return destinationAddress;
	}

	public void setDestinationAddress(byte[] destinationAddress) {
		this.destinationAddress = destinationAddress;
	}

	public ConnectionId getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(ConnectionId connectionId) {
		this.connectionId = connectionId;
	}

	public byte getPduType() {
		return pduType;
	}

	public void setPduType(byte pduType) {
		this.pduType = pduType;
	}

	public byte getFlags() {
		return flags;
	}

	public void setFlags(byte flags) {
		this.flags = flags;
	}

	public int[] getPduLength() {
		return pduLength;
	}

	public void setPduLength(int[] pduLength) {
		this.pduLength = pduLength;
	}

	public int[] getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(int[] sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public ByteBuffer getUserData() {
		return userData;
	}

	public void setUserData(ByteBuffer userData) {
		this.userData = userData;
	}
	
	public int computePDULength(){
		//TODO do it right
		return 0;
	}
	
	public void appendSDU(byte[] sdu){
		//TODO do it right;
	}
}