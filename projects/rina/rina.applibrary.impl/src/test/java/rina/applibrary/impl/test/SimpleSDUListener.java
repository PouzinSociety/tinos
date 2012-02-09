package rina.applibrary.impl.test;

import rina.applibrary.api.SDUListener;

/**
 * Prints an SDU when it receives it, and stores it as the last SDU received.
 * Assumes all received SDUs are Strings.
 * @author eduardgrasa
 *
 */
public class SimpleSDUListener implements SDUListener{

	private String lastSdu = null;
	
	public String getLastSDU(){
		return lastSdu;
	}
	
	public void sduDelivered(byte[] sdu) {
		lastSdu = new String(sdu);
		System.out.println("Simple SDU Listener: Received SDU: "+sdu);
	}

}
