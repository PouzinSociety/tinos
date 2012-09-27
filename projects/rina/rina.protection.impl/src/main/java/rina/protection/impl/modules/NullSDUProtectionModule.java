package rina.protection.impl.modules;

import rina.efcp.api.PDU;
import rina.ipcservice.api.IPCException;
import rina.protection.api.SDUProtectionModule;
import rina.protection.api.SDUProtectionModuleRepository;

/**
 * A protection module that provides no protection. It just takes the 
 * encoded PCI and user data and creates a single SDU (and viceversa)
 * @author eduardgrasa
 *
 */
public class NullSDUProtectionModule implements SDUProtectionModule{

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
		byte[] sdu = new byte[pdu.getEncodedPCI().length + pdu.getUserData().length];
		System.arraycopy(pdu.getEncodedPCI(), 0, sdu, 0, pdu.getEncodedPCI().length);
		System.arraycopy(pdu.getUserData(), 0, sdu, pdu.getEncodedPCI().length, pdu.getUserData().length);
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
		return pdu;
	}
}
