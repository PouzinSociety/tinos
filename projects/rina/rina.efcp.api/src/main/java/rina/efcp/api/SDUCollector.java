package rina.efcp.api;

import java.util.List;

/**
 * Classes implementing this interface are called 
 * when one or more SDUs are ready to be delivered to a certain port Id.
 * It is the responsibility of the SDU collector to call the application 
 * and pass it the complete SDUs
 * @author eduardgrasa
 *
 */
public interface SDUCollector {
	
	/**
	 * Deliver a set of sdus to the application process bound to portId
	 * @param sdus
	 * @param portId
	 */
	public void deliverSDUsToApplicationProcess(List<byte[]> sdus, int portId);
}
