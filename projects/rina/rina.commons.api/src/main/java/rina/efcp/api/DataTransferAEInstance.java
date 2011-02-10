package rina.efcp.api;

import java.util.List;

import rina.flowallocator.api.Connection;
import rina.ipcprocess.api.IPCProcessComponent;

/**
 * Interface to communicate with the DataTrasferAEInstance
 * @author eduardgrasa
 *
 */
public interface DataTransferAEInstance extends IPCProcessComponent {

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
	 * It indicates that the gap in sequencing on this flow from undelivered
	 * SDUs is greater than MaxGapAllowed. The action is a policy 
	 * (SDU Gap Timer Policy)
	 */
	public void sduGapTimerFired();
	
	/**
	 * Return the connection associated to this Data Transfer AE Instance
	 * @return
	 */
	public Connection getConnection();
}
