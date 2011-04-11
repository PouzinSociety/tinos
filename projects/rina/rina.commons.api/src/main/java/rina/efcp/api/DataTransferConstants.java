package rina.efcp.api;

/**
 * Contains the constants for DTP and DTCP, defined at DIF compile time
 * TODO move to a config file after the demo
 * @author eduardgrasa
 *
 */
public class DataTransferConstants {
	
	/**
	 * The length of QoS-id field in the DTP PCI, in bytes
	 */
	private int qosIdLength = 1;
	
	/**
	 * The length of the Port-id field in the DTP PCI, in bytes
	 */
	private int portIdLength = 2;
	
	/**
	 * The length of the CEP-id field in the DTP PCI, in bytes
	 */
	private int cepIdLength = 2;
	
	/**
	 * The length of the sequence number field in the DTP PCI, in bytes
	 */
	private int sequenceNumberLength = 2;
	
	/**
	 * The length of the address field in the DTP PCI, in bytes
	 */
	private int addressLength = 2;
	
	/**
	 * The length of the length field in the DTP PCI, in bytes
	 */
	private int lengthLength = 2;
	
	/**
	 * The maximum length allowed for a PDU in this DIF, in bytes
	 */
	private int maxPDUSize = 1500;
	
	/**
	 * True if the PDUs in this DIF have CRC, TTL, and/or encryption. Since 
	 * headers are encrypted, not just user data, if any flow uses encryption, 
	 * all flows within the same DIF must do so and the same encryption 
	 * algorithm must be used for every PDU; we cannot identify which flow owns 
	 * a particular PDU until it has been decrypted.
	 */
	private boolean DIFIntegrity = false;
	
	/**
	 * This is true if multiple SDUs can be delimited and concatenated within 
	 * a single PDU.
	 */
	private boolean DIFConcatenation = true;
	
	/**
	 * This is true if multiple SDUs can be fragmented and reassembled within 
	 * a single PDU.
	 */
	private boolean DIFFragmentation = false;
	
	/**
	 * Value of a flag that indicates a SDU fragment that is neither the first nor the last
	 */
	private byte fragmentFlag = 0x00;
	
	/**
	 * Value of a flag that indicates a SDU fragment that is the first fragment of an SDU
	 */
	private byte firstFragmentFlag = 0x01;
	
	/**
	 * Value of a flag that indicates a SDU fragment that is the last fragment of an SDU
	 */
	private byte lastFragmentFlag = 0x02;
	
	/**
	 * Value of a flag that indicates a PDU carrying a complete SDU
	 */
	private byte completeFlag = 0x03;
	
	/**
	 * Value of a flag that indicates a PDU carrying multiple SDUs
	 */
	private byte multipleFlag = 0x07;
	
	/**
	 * The SDU gap timer delay in ms.
	 */
	private long SDUGapTimerDelay = 2*1000;
	
	/**
	 * The maximum PDU lifetime in this DIF, in seconds.
	 */
	private int maxPDULifetime  = 10*1000;

	public int getQosIdLength() {
		return qosIdLength;
	}

	public void setQosIdLength(int qosIdLength) {
		this.qosIdLength = qosIdLength;
	}

	public int getPortIdLength() {
		return portIdLength;
	}

	public void setPortIdLength(int portIdLength) {
		this.portIdLength = portIdLength;
	}

	public int getCepIdLength() {
		return cepIdLength;
	}

	public void setCepIdLength(int cepIdLength) {
		this.cepIdLength = cepIdLength;
	}

	public int getSequenceNumberLength() {
		return sequenceNumberLength;
	}

	public void setSequenceNumberLength(int sequenceNumberLength) {
		this.sequenceNumberLength = sequenceNumberLength;
	}

	public int getAddressLength() {
		return addressLength;
	}

	public void setAddressLength(int addressLength) {
		this.addressLength = addressLength;
	}

	public int getLengthLength() {
		return lengthLength;
	}

	public void setLengthLength(int lengthLength) {
		this.lengthLength = lengthLength;
	}

	public int getPciLength() {
		return 1 + 2*addressLength + qosIdLength + 2*portIdLength + 2 + lengthLength + sequenceNumberLength;
	}

	public int getMaxPDUSize() {
		return maxPDUSize;
	}

	public void setMaxPDUSize(int maxPDUSize) {
		this.maxPDUSize = maxPDUSize;
	}

	public int getMaxSDUSize() {
		return maxPDUSize - getPciLength();
	}

	public boolean isDIFIntegrity() {
		return DIFIntegrity;
	}

	public void setDIFIntegrity(boolean dIFIntegrity) {
		DIFIntegrity = dIFIntegrity;
	}

	public boolean isDIFConcatenation() {
		return DIFConcatenation;
	}

	public void setDIFConcatenation(boolean dIFConcatenation) {
		DIFConcatenation = dIFConcatenation;
	}

	public boolean isDIFFragmentation() {
		return DIFFragmentation;
	}

	public void setDIFFragmentation(boolean dIFFragmentation) {
		DIFFragmentation = dIFFragmentation;
	}

	public byte getFragmentFlag() {
		return fragmentFlag;
	}

	public void setFragmentFlag(byte fragmentFlag) {
		this.fragmentFlag = fragmentFlag;
	}

	public byte getFirstFragmentFlag() {
		return firstFragmentFlag;
	}

	public void setFirstFragmentFlag(byte firstFragmentFlag) {
		this.firstFragmentFlag = firstFragmentFlag;
	}

	public byte getLastFragmentFlag() {
		return lastFragmentFlag;
	}

	public void setLastFragmentFlag(byte lastFragmentFlag) {
		this.lastFragmentFlag = lastFragmentFlag;
	}

	public byte getCompleteFlag() {
		return completeFlag;
	}

	public void setCompleteFlag(byte completeFlag) {
		this.completeFlag = completeFlag;
	}

	public byte getMultipleFlag() {
		return multipleFlag;
	}

	public void setMultipleFlag(byte multipleFlag) {
		this.multipleFlag = multipleFlag;
	}

	public long getSDUGapTimerDelay() {
		return SDUGapTimerDelay;
	}

	public void setSDUGapTimerDelay(long sDUGapTimerDelay) {
		SDUGapTimerDelay = sDUGapTimerDelay;
	}

	public int getMaxPDULifetime() {
		return maxPDULifetime;
	}

	public void setMaxPDULifetime(int maxPDULifetime) {
		this.maxPDULifetime = maxPDULifetime;
	}
}
