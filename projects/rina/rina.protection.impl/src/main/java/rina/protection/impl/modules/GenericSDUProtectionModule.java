package rina.protection.impl.modules;

import rina.efcp.api.PDU;
import rina.ipcservice.api.IPCException;
import rina.protection.api.SDUProtectionModule;

/**
 * This module assumes that all functions can be performed and provides a
 * checklist. This module is generally a property of the DIF, but in some cases
 * there might be a different SDU Protection module for each (N-1)-port. The
 * module provides for the functions to be applied to both the PCI (header) and
 * the whole SDU. This module may add PCI elements to the PDU to support
 * the functions. These fields are defined by the policy that defines them, e.g.
 * specifying CRC-16 would prepend a 16 bit PCI field. When converted to
 * code, unused fields are not included. The order of operations and the fields
 * are as follows (inbound, outbound is clearly the opposite):
 * 
 * (Decryption, Decompression, Error Check Code, Time-to-Live)

 * @author eduardgrasa
 *
 */
public abstract class GenericSDUProtectionModule implements SDUProtectionModule{

	public byte[] protectPDU(PDU pdu) throws IPCException {
		pdu = this.applyTTL(pdu);
		pdu = this.appendErrorCheckCode(pdu);
		/*byte[] buffer = PDUParser.encodePDU(pdu);
		/*buffer = this.compressSDU(buffer);
		buffer = this.encryptSDU(buffer);*/
		
		return null;
	}

	public PDU unprotectPDU(byte[] sdu) throws IPCException {
		sdu = this.decryptSDU(sdu);
		sdu = this.uncompressSDU(sdu);
		//PDU pdu = PDUParser.parsePDU(sdu);
		return null;
	}
	
	/**
	 * IF TTL present invoke TTL mechanism to initialize or modify the TTL field
	 * @param pdu
	 * @return
	 * @throws IPCException if TTL is 0 throw Exception (the PDU will be discarded)
	 */
	public abstract PDU applyTTL(PDU pdu) throws IPCException;
	
	/**
	 * If this option is present, apply the error code to the full SDU and append it
	 * @param pdu
	 * @return
	 * @throws IPCException if there are problems applying the error code (the PDU will be discarded)
	 */
	public abstract PDU appendErrorCheckCode(PDU pdu) throws IPCException;
	
	/**
	 * If this option is present, compress the SDU
	 * @param sdu to be compressed
	 * @return compressed SDU
	 * @throws IPCException if there are any problems compressing the SDU (it will be discarded)
	 */
	public abstract byte[] compressSDU(byte[] sdu) throws IPCException;
	
	/**
	 * If this option is present, encrypt the SDU
	 * @param sdu to be encrypted
	 * @return encrypted SDU
	 * @throws IPCException if there are any problems encrypting the SDU (it will be discarded)
	 */
	public abstract byte[] encryptSDU(byte[] sdu) throws IPCException;
	
	/**
	 * Compute the CRC with the 
	 * @param pdu
	 * @return
	 * @throws IPCException
	 */
	public abstract PDU analyzeErrorCheckCode(PDU pdu) throws IPCException;
	
	/**
	 * If this option is present, uncompress the SDU
	 * @param sdu to be uncompressed
	 * @return uncompressed SDU
	 * @throws IPCException
	 */
	public abstract byte[] uncompressSDU(byte[] sdu) throws IPCException;
	
	/**
	 * If this option is present, decrypt the SDU
	 * @param sdu to be decrypted
	 * @return decrypted SDU
	 * @throws IPCException if there are any problems decrypting the SDU (it will be discarded)
	 */
	public abstract byte[] decryptSDU(byte[] sdu) throws IPCException;
}
