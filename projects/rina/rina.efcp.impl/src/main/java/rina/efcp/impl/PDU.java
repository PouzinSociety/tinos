package rina.efcp.impl;

import java.nio.ByteBuffer;

import rina.efcp.api.EFCPConstants;
import rina.flowallocator.api.Connection;
import rina.flowallocator.api.ConnectionId;
import rina.utils.types.Unsigned;

/**
 * An EFCP PDU, consisting of the PCI plus user data
 * @author eduardgrasa
 *
 */
public class PDU {
	
	/**
	 * An identifier indicating the version of the protocol, seems prudent
	 */
	private Unsigned version = null;
	
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
	private Unsigned pduType = null;
	
	/**
	 * This field indicates conditions that affect the handling of the PDU. Flags should only indicate 
	 * conditions that can change from one PDU to the next. Conditions that are invariant over the life 
	 * of the connection should be established during allocation or by the action of management. The 
	 * interpretation of the flags depends on the PDU Type.
	 */
	private Unsigned flags = null;
	
	/**
	 * Sequence number of the PDU
	 */
	private Unsigned sequenceNumber = null;
	
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
		userData = ByteBuffer.allocate(EFCPConstants.maxSDUSize);
		version = new Unsigned(1);
		version.setValue(0x01);
		pduType = new Unsigned(1);
		flags = new Unsigned(1);
		sequenceNumber = new Unsigned(1);
	}
	
	/**
	 * Return a PDU object created by deserializing a 
	 * serialized PDU
	 * @param pdu
	 * @return
	 */
	public static PDU createPDUFromByteArray(byte[] buffer){
		PDU pdu = new PDU();
		ConnectionId connectionId = new ConnectionId();
		Unsigned unsigned = null;
		byte[] value = null;
		int index = 0;
		int userDataLength = 0;
		
		value = new byte[1];
		index = readFieldFromPCI(value, index, buffer);
		unsigned = new Unsigned(value);
		pdu.setVersion(unsigned);
		
		value = new byte[EFCPConstants.addressLength];
		index = readFieldFromPCI(value, index, buffer);
		pdu.setSourceAddress(value);
		
		value = new byte[EFCPConstants.addressLength];
		index = readFieldFromPCI(value, index, buffer);
		pdu.setDestinationAddress(value);
		
		value = new byte[EFCPConstants.QoSidLength];
		index = readFieldFromPCI(value, index, buffer);
		unsigned = new Unsigned(value);
		connectionId.setQosId(unsigned);
		
		value = new byte[EFCPConstants.PortIdLength];
		index = readFieldFromPCI(value, index, buffer);
		unsigned = new Unsigned(value);
		connectionId.setSourceCEPId(unsigned);
		
		value = new byte[EFCPConstants.PortIdLength];
		index = readFieldFromPCI(value, index, buffer);
		unsigned = new Unsigned(value);
		connectionId.setDestinationCEPId(unsigned);
		pdu.setConnectionId(connectionId);
		
		value = new byte[1];
		index = readFieldFromPCI(value, index, buffer);
		unsigned = new Unsigned(value);
		pdu.setPduType(unsigned);
		
		value = new byte[1];
		index = readFieldFromPCI(value, index, buffer);
		unsigned = new Unsigned(value);
		pdu.setFlags(unsigned);
		
		value = new byte[EFCPConstants.lengthLength];
		index = readFieldFromPCI(value, index, buffer);
		userDataLength = (int) new Unsigned(value).getValue() - EFCPConstants.pciLength;
		
		value = new byte[EFCPConstants.SequenceNumberLength];
		index = readFieldFromPCI(value, index, buffer);
		unsigned = new Unsigned(value);
		pdu.setSequenceNumber(unsigned);
		
		value = new byte[userDataLength];
		index = readFieldFromPCI(value, index, buffer);
		pdu.appendSDU(value);
		
		return pdu;
	}
	
	/**
	 * Copies toRead.length bytes from the PDU into the toRead byte array
	 * @param toRead
	 * @param index
	 * @param pdu
	 * @param unsigned
	 * @return
	 */
	private static int readFieldFromPCI(byte[] toRead, int index, byte[] pdu){
		for(int i=0; i<toRead.length; i++){
			toRead[i] = pdu[index];
			index++;
		}
		
		return index;
	}
	
	private PDU(){
	}

	public Unsigned getVersion() {
		return version;
	}

	public void setVersion(Unsigned version) {
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

	public Unsigned getPduType() {
		return pduType;
	}

	public void setPduType(Unsigned pduType) {
		this.pduType = pduType;
	}

	public Unsigned getFlags() {
		return flags;
	}

	public void setFlags(Unsigned flags) {
		this.flags = flags;
	}

	public long getPduLength() {
		return EFCPConstants.pciLength + userData.position();
	}

	public Unsigned getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(Unsigned sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public ByteBuffer getUserData() {
		return userData;
	}

	public void setUserData(ByteBuffer userData) {
		this.userData = userData;
	}
	
	public void appendSDU(byte[] sdu){
		userData.put(sdu);
	}
	
	/**
	 * Returns the PDU as a byte array, ready to be 
	 * delivered to the RMT
	 * @return
	 */
	public byte[] getSerializedPDU(){
		byte[] pci = new byte[EFCPConstants.pciLength];
		Unsigned length = new Unsigned(EFCPConstants.lengthLength);
		length.setValue(this.getPduLength());
		int index = 0;
		
		index = addBytesToPCI(pci, index, version.getBytes());
		index = addBytesToPCI(pci, index, sourceAddress);
		index = addBytesToPCI(pci, index, destinationAddress);
		index = addBytesToPCI(pci, index, connectionId.getQosId().getBytes());
		index = addBytesToPCI(pci, index, connectionId.getSourceCEPId().getBytes());
		index = addBytesToPCI(pci, index, connectionId.getDestinationCEPId().getBytes());
		index = addBytesToPCI(pci, index, pduType.getBytes());
		index = addBytesToPCI(pci, index, flags.getBytes());
		index = addBytesToPCI(pci, index, length.getBytes());
		index = addBytesToPCI(pci, index, sequenceNumber.getBytes());
		
		ByteBuffer pdu = ByteBuffer.allocate(EFCPConstants.maxPDUSize);
		pdu.put(pci);
		pdu.put(userData.array());
		
		return pdu.array();
	}
	
	private int addBytesToPCI(byte[] pci, int index, byte[] toAdd){
		for (int i=0; i<toAdd.length; i++){
			pci[index] = toAdd[i];
			index++;
		}
		
		return index;
	}
}