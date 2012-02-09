package rina.applibrary.api;

/**
 * Implementers of this class are notified when a flow receives 
 * an SDU (Service Data Unit), the array of bytes sent by the application(s) 
 * on the other side of the flow
 * @author eduardgrasa
 *
 */
public interface SDUListener {
	
	/**
	 * When an SDU is received by the flow, the SDU Listener will be notified
	 * @param sdu
	 */
	public void sduDelivered(byte[] sdu);

}
