package rina.efcp.impl;

import java.util.ArrayList;
import java.util.List;

import rina.efcp.api.DataTransferConstants;
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
	private long sourceAddress = 0;
	
	/**
	 * A synonym for the application process name designating an IPC process 
	 * with scope limited to the DIF and a binding to the destination application process
	 */
	private long destinationAddress = 0;
	
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
	private List<byte[]> userData = null;
	
	/**
	 * The data transfer constants in this DIF
	 */
	private DataTransferConstants dataTransferConstants = null;
	
	public PDU(Connection connection, DataTransferConstants dataTransferConstants){
		this.connectionId = connection.getCurrentConnectionId();
		this.sourceAddress = connection.getSourceAddress();
		this.destinationAddress = connection.getDestinationAddress();
		this.dataTransferConstants = dataTransferConstants;
		userData = new ArrayList<byte[]>();
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
	public static PDU createPDUFromByteArray(byte[] buffer, DataTransferConstants dataTransferConstants){
		PDU pdu = new PDU();
		pdu.setDataTransferConstants(dataTransferConstants);
		ConnectionId connectionId = new ConnectionId();
		Unsigned unsigned = null;
		byte[] value = null;
		int index = 0;
		int userDataLength = 0;
		
		value = new byte[1];
		index = readFieldFromPCI(value, index, buffer);
		unsigned = new Unsigned(value);
		pdu.setVersion(unsigned);
		
		//TODO fix this
		/*value = new byte[dataTransferConstants.getAddressLength()];
		index = readFieldFromPCI(value, index, buffer);
		pdu.setSourceAddress(value);
		
		value = new byte[dataTransferConstants.getAddressLength()];
		index = readFieldFromPCI(value, index, buffer);
		pdu.setDestinationAddress(value);
		
		value = new byte[dataTransferConstants.getQosIdLength()];
		index = readFieldFromPCI(value, index, buffer);
		unsigned = new Unsigned(value);
		connectionId.setQosId(value);
		
		value = new byte[dataTransferConstants.getPortIdLength()];
		index = readFieldFromPCI(value, index, buffer);
		unsigned = new Unsigned(value);
		connectionId.setSourceCEPId(unsigned);
		
		value = new byte[dataTransferConstants.getPortIdLength()];
		index = readFieldFromPCI(value, index, buffer);
		unsigned = new Unsigned(value);
		connectionId.setDestinationCEPId(unsigned);
		pdu.setConnectionId(connectionId);*/
		
		value = new byte[1];
		index = readFieldFromPCI(value, index, buffer);
		unsigned = new Unsigned(value);
		pdu.setPduType(unsigned);
		
		value = new byte[1];
		index = readFieldFromPCI(value, index, buffer);
		unsigned = new Unsigned(value);
		pdu.setFlags(unsigned);
		
		value = new byte[dataTransferConstants.getLengthLength()];
		index = readFieldFromPCI(value, index, buffer);
		userDataLength = (int) new Unsigned(value).getValue() - dataTransferConstants.getPciLength();
		
		value = new byte[dataTransferConstants.getSequenceNumberLength()];
		index = readFieldFromPCI(value, index, buffer);
		unsigned = new Unsigned(value);
		pdu.setSequenceNumber(unsigned);
		
		
		if (pdu.getFlags().getValue() == dataTransferConstants.getCompleteFlag()){
			value = new byte[userDataLength];
			index = readFieldFromPCI(value, index, buffer);
			pdu.appendSDU(value);
		}else if (pdu.getFlags().getValue() == dataTransferConstants.getMultipleFlag()){
			//TODO to implement delimiting to handle the case
			//of multiple SDUs within a PDU
		}
		
		return pdu;
	}
	
	public void setDataTransferConstants(DataTransferConstants dataTransferConstants){
		this.dataTransferConstants = dataTransferConstants;
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
		userData = new ArrayList<byte[]>();
	}

	public Unsigned getVersion() {
		return version;
	}

	public void setVersion(Unsigned version) {
		this.version = version;
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

	public int getPduLength() {
		int length = 0;
		for(int i=0; i<userData.size(); i++){
			length = length + userData.get(i).length;
		}
		
		return dataTransferConstants.getPciLength() + length;
	}

	public Unsigned getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(Unsigned sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public List<byte[]> getUserData() {
		return userData;
	}

	public void setUserData(List<byte[]> userData) {
		this.userData = userData;
	}
	
	public void appendSDU(byte[] sdu){
		userData.add(sdu);
		//Set the flag accordingly
		if (userData.size()==1){
			//There's a single SDU in this PDU
			this.flags.setValue(dataTransferConstants.getCompleteFlag());
		}else if (userData.size() > 1){
			//There are multiple SDUs in this PDU
			this.flags.setValue(dataTransferConstants.getMultipleFlag());
		}
	}
	
	/**
	 * Returns the PDU as a byte array, ready to be 
	 * delivered to the RMT
	 * @return
	 */
	public byte[] getSerializedPDU(){
		byte[] pci = new byte[dataTransferConstants.getPciLength()];
		Unsigned length = new Unsigned(dataTransferConstants.getLengthLength());
		length.setValue(this.getPduLength());
		int index = 0;
		
		//TODO fix this
		index = addBytesToPCI(pci, index, version.getBytes());
		/*index = addBytesToPCI(pci, index, sourceAddress);
		index = addBytesToPCI(pci, index, destinationAddress);
		index = addBytesToPCI(pci, index, connectionId.getQosId());
		index = addBytesToPCI(pci, index, connectionId.getSourceCEPId());
		index = addBytesToPCI(pci, index, connectionId.getDestinationCEPId();*/
		index = addBytesToPCI(pci, index, pduType.getBytes());
		index = addBytesToPCI(pci, index, flags.getBytes());
		index = addBytesToPCI(pci, index, length.getBytes());
		index = addBytesToPCI(pci, index, sequenceNumber.getBytes());
		
		//Add PCI
		index = 0;
		byte[] pdu = new byte[this.getPduLength()];
		for(int i=0; i<pci.length; i++){
			pdu[i] = pci[i];
		}
		index = index + pci.length;
		
		//Add user data
		for(int i=0; i<userData.size(); i++){
			for(int j=0; j<userData.get(i).length; j++){
				pdu[index+j] = userData.get(i)[j];
			}
			index = index + userData.get(i).length;
		}
		
		return pdu;
	}
	
	private int addBytesToPCI(byte[] pci, int index, byte[] toAdd){
		for (int i=0; i<toAdd.length; i++){
			pci[index] = toAdd[i];
			index++;
		}
		
		return index;
	}
	
	public boolean equals(Object candidate){
		if (candidate == null){
			return false;
		}
		
		if (!(candidate instanceof PDU)){
			return false;
		}
		
		PDU pdu = (PDU) candidate;
		return this.getSequenceNumber().equals(pdu.getSequenceNumber());
	}
}