package rina.policies.impl;

import java.util.*;

import rina.ipcservice.api.QoSCube;

/**
 * The unreliable QoS cube that maps to UDP
 * @author elenitrouva
 *
 */

public class unreliableQoSCube {
	
	QoSCube cube = new QoSCube();

	private Map<String, Object> unreliablecube = new HashMap<String, Object>();
	
	//Average bandwidth (measured at the application in bits/sec) 
	public String avgBW = "average bandwidth"; 
	//Average SDU bandwidth (measured in SDUs/sec)
	public String avgSDUBW = "average SDU bandwidth";
	//Peak bandwidth-duration (measured in bits/sec)
	public String peakBWduration = "peak bandwidth duration";
	//Peak SDU bandwidth-duration (measured in SDUs/sec)
	public String peakSDUBWduration = "peak SDU bandwidth duration";
	//Burst period measured in seconds 
	public String burstPeriod = "burst period";
	//Burst duration, measured in fraction of Burst Period
	public String burstDuration = "burst duration";
	//Undetected bit error rate measured as a probability
	public String undetectedBitErrorRate = "undetected bit error rate";
	//Partial Delivery Ð Can partial SDUs be delivered?
	public String partialDeliveryAllowed = "partial delivery";
	//Order Ð Must SDUs be delivered in order? 
	public String orderRequired = "SDUs delivered in order";
	//Max allowable gap in SDUs
	public String maxGapinSDUs = "maximum allowable gap in SDUs";
		//Delay in secs
	public String delay = "delay";
	//Jitter in secs2
	public String jitter = "jitter";
	
	
	public unreliableQoSCube(){
		unreliablecube.put(avgBW, null); 
		unreliablecube.put(avgSDUBW, null); 
		unreliablecube.put(peakBWduration, null); 
		unreliablecube.put(peakSDUBWduration, null); 
		unreliablecube.put(burstPeriod, null); 
		unreliablecube.put(burstDuration, null);  
		unreliablecube.put(undetectedBitErrorRate,  java.lang.Math.pow(10, -9) );
		unreliablecube.put(partialDeliveryAllowed, true); 
		unreliablecube.put(orderRequired, false); 
		unreliablecube.put(maxGapinSDUs, null); 
		unreliablecube.put(delay, null); 
		unreliablecube.put(jitter, null); 
				
		cube.setCube(unreliablecube);
	}
	
	
}
