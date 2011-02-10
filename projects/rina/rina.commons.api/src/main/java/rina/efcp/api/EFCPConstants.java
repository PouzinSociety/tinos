package rina.efcp.api;

/**
 * Contains the constants for DTP and DTCP, defined at DIF compile time
 * TODO move to a config file after the demo
 * @author eduardgrasa
 *
 */
public interface EFCPConstants {
	
	/**
	 * The length of QoS-id field in the DTP PCI, in bytes
	 */
	public static final int QoSidLength = 1;
	
	/**
	 * The length of the Port-id field in the DTP PCI, in bytes
	 */
	public static final int PortIdLength = 2;
	
	/**
	 * The length of the CEP-id field in the DTP PCI, in bytes
	 */
	public static final int CEPIdLength = 2;
	
	/**
	 * The length of the sequence number field in the DTP PCI, in bytes
	 */
	public static final int SequenceNumberLength = 2;
	
	/**
	 * The length of the address field in the DTP PCI, in bytes
	 */
	public static final int addressLength = 2;
	
	/**
	 * The length of the length field in the DTP PCI, in bytes
	 */
	public static final int lengthLength = 2;

	/**
	 * The length of the PCI, in bytes
	 */
	public static final int pciLength = 1 + 2*addressLength + QoSidLength + 2*PortIdLength + 2 + lengthLength + SequenceNumberLength;
	
	/**
	 * The maximum length allowed for a PDU in this DIF, in bytes
	 */
	public static final int maxPDUSize = 1500;
	
	/**
	 * The maximum length allowed for an SDU in this DIF, in bytes
	 */
	public static final int maxSDUSize = maxPDUSize - pciLength;
	
	/**
	 * True if the PDUs in this DIF have CRC, TTL, and/or encryption. Since 
	 * headers are encrypted, not just user data, if any flow uses encryption, 
	 * all flows within the same DIF must do so and the same encryption 
	 * algorithm must be used for every PDU; we cannot identify which flow owns 
	 * a particular PDU until it has been decrypted.
	 */
	public static final boolean DIFIntegrity = false;
	
	/**
	 * This is true if multiple SDUs can be delimited and concatenated within 
	 * a single PDU.
	 */
	public static final boolean DIFConcatenation = true;
	
	/**
	 * This is true if multiple SDUs can be fragmented and reassembled within 
	 * a single PDU.
	 */
	public static final boolean DIFFragmentation = false;
	
	/**
	 * Value of a flag that indicates a SDU fragment that is neither the first nor the last
	 */
	public static final byte fragmentFlag = 0x00;
	
	/**
	 * Value of a flag that indicates a SDU fragment that is the first fragment of an SDU
	 */
	public static final byte firstFragmentFlag = 0x01;
	
	/**
	 * Value of a flag that indicates a SDU fragment that is the last fragment of an SDU
	 */
	public static final byte lastFragmentFlag = 0x02;
	
	/**
	 * Value of a flag that indicates a PDU carrying a complete SDU
	 */
	public static final byte completeFlag = 0x03;
	
	/**
	 * Value of a flag that indicates a PDU carrying multiple SDUs
	 */
	public static final byte multipleFlag = 0x07;
	
	/**
	 * The SDU gap timer delay in ms.
	 */
	public static final long SDUGapTimerDelay = 2*1000;
}
