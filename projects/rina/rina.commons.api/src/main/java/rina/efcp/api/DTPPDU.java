package rina.efcp.api;

/**
 * Represents a DTP PDU
 * @author eduardgrasa
 *
 */
public class DTPPDU extends PDU{
	
	/**
	 * This field indicates conditions that affect the handling of the PDU. Flags should only indicate 
	 * conditions that can change from one PDU to the next. Conditions that are invariant over the life 
	 * of the connection should be established during allocation or by the action of management. The 
	 * interpretation of the flags depends on the PDU Type.
	 */
	private int flags = 0;
	
	public DTPPDU(){
		this.setPduType(PDU.DTP_PDU_TYPE);
	}
	
	public DTPPDU(PDU pdu){
		this.setDestinationAddress(pdu.getDestinationAddress());
		this.setSourceAddress(pdu.getSourceAddress());
		this.setConnectionId(pdu.getConnectionId());
		this.setSequenceNumber(pdu.getSequenceNumber());
		this.setPduType(pdu.getPduType());
		this.setEncodedPCI(pdu.getEncodedPCI());
		this.setErrorCheckCode(pdu.getErrorCheckCode());
		this.setRawPDU(pdu.getRawPDU());
		this.setUserData(pdu.getUserData());
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}
	
	@Override
	public String toString(){
		String result = "";
		result = result + "Destination @: " +this.getDestinationAddress() + " CEPid: " + this.getConnectionId().getDestinationCEPId() + 
			" Source @: " +this.getSourceAddress() + " CEPid: " +this.getConnectionId().getSourceCEPId() + "\n" + 
			" QoSid: " +this.getConnectionId().getQosId() + " PDU type: " +this.getPduType() + 
			" Flags: " +this.getFlags() + " Sequence number: " +this.getSequenceNumber();
		
		return result;
	}
}
