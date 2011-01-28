package rina.efcp.api;

import java.util.List;

import rina.flowallocator.api.Connection;

/**
 * Interface to communicate with the DataTrasferAEInstance
 * @author eduardgrasa
 *
 */
public interface DataTransferAEInstance {

	/**
	 * A new SDU has been delivered from (N)-port x. This DTP 
	 * instance will generate the adequate PDUs and post them 
	 * to the RMT.
	 * @param sdu
	 */
	public void sdusDelivered(List<byte[]> sdus);
	
	/**
	 * A PDU has been delivered from the RMT. This DTP instance 
	 * will process it.
	 * @param pdu
	 */
	public void pduDelivered(byte[] pdu);
	
	/**
	 * Return the connection associated to this Data Transfer AE Instance
	 * @return
	 */
	public Connection getConnection();
}
