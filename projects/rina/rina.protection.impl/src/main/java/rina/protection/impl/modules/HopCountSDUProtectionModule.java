package rina.protection.impl.modules;

import rina.efcp.api.PDU;
import rina.ipcservice.api.IPCException;
import rina.protection.api.SDUProtectionModule;
import rina.protection.api.SDUProtectionModuleRepository;

/**
 * A protection module that just provides a 1 byte hop count. After that it takes the 
 * encoded PCI and user data and creates a single SDU (and viceversa). The resulting SDU has
 * the following syntax: Hopcount (1 byte) + PCI + user data
 * @author eduardgrasa
 *
 */
public class HopCountSDUProtectionModule implements SDUProtectionModule{
	
	private byte initialHopCountValue = 0;

	public HopCountSDUProtectionModule(){
		//TODO, make this a configurable value
		this.initialHopCountValue = 4;
	}
	
	/**
	 * Return the type of the SDU Protection Module
	 * @return
	 */
	public String getType() {
		return SDUProtectionModuleRepository.NULL;
	}

	/**
	 * Protects a PDU before being sent through an N-1 flow
	 * @param pdu
	 * @return the protected PDU
	 * @throws IPCException if there is an issue protecting the PDU
	 */
	public byte[] protectPDU(PDU pdu) throws IPCException {
		//If the PDU has never been encoded (i.e. originates from a flow that has
		//this IPC Process as source or destination)
		if (pdu.getRawPDU() == null){
			byte[] sdu = new byte[1 + pdu.getEncodedPCI().length + pdu.getUserData().length];
			sdu[0] = this.initialHopCountValue;
			System.arraycopy(pdu.getEncodedPCI(), 0, sdu, 1, pdu.getEncodedPCI().length);
			System.arraycopy(pdu.getUserData(), 0, sdu, 1+ pdu.getEncodedPCI().length, pdu.getUserData().length);
			return sdu;
		}
		
		//If the PDU has been encoded before (i.e. this IPC Process is relaying it), 
		//just update hopcount and check that is not 0
		byte[] sdu = pdu.getRawPDU();
		sdu[0] = (byte) (sdu[0] - 1);
		if (sdu[0] <= 0){
			throw new IPCException(IPCException.ERROR_CODE, "Hopcount reached 0, dropping PDU");
		}
		return sdu;
	}

	/**
	 * Unprotects a PDU after receiving it from an N-1 flow. When this 
	 * call returns the PDU.rawPDU attribute must contain a byte array 
	 * that is the concatenation of the encoded PCI + the user data.
	 * @param sdu
	 * @return the unprotected PDU
	 * @throws IPCException if there is an issue unprotecting the SDU
	 */
	public PDU unprotectPDU(byte[] sdu) throws IPCException {
		PDU pdu = new PDU();
		pdu.setRawPDU(sdu);
		pdu.setPciStartIndex((byte)1);
		return pdu;
	}
}
