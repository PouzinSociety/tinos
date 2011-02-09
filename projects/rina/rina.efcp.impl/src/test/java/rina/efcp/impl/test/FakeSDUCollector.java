package rina.efcp.impl.test;

import java.util.List;

import rina.efcp.api.SDUCollector;

/**
 * Fake SDU collector for testing, assumes all the SDUs are strings
 * @author eduardgrasa
 */
public class FakeSDUCollector implements SDUCollector{

	public void deliverSDUsToApplicationProcess(List<byte[]> sdus, int portId) {
		System.out.println("Delivering sdus to port " + portId);
		
		for(int i=0; i<sdus.size(); i++){
			System.out.println(new String(sdus.get(i)));
		}
	}

}
