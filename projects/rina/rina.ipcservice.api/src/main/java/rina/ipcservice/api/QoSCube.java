package rina.ipcservice.api;

import java.util.*;

/**
 * Definition of QoS cube
 *
 */
public class QoSCube {

	Map<String, Object> cube = new HashMap<String, Object>();

	public boolean validFormat = false;
	
	//Average bandwidth (measured at the application in bits/sec) 
	public static final String avgBW = "average bandwidth"; 
	//Average SDU bandwidth (measured in SDUs/sec)
	public static final String avgSDUBW = "average SDU bandwidth";
	//Peak bandwidth-duration (measured in bits/sec)
	public static final String peakBWduration = "peak bandwidth duration";
	//Peak SDU bandwidth-duration (measured in SDUs/sec)
	public static final String peakSDUBWduration = "peak SDU bandwidth duration";
	//Burst period measured in seconds 
	public static final String burstPeriod = "burst period";
	//Burst duration, measured in fraction of Burst Period
	public static final String burstDuration = "burst duration";
	//Undetected bit error rate measured as a probability
	public static final String undetectedBitErrorRate = "undetected bit error rate";
	//Partial Delivery Ð Can partial SDUs be delivered?
	public static final String partialDeliveryAllowed = "partial delivery";
	//Order Ð Must SDUs be delivered in order? 
	public static final String orderRequired = "SDUs delivered in order";
	//Max allowable gap in SDUs
	public static final String maxGapinSDUs = "maximum allowable gap in SDUs";
	//Delay in secs
	public static final String delay = "delay";
	//Jitter in secs2
	public static final String jitter = "jitter";



	
	public QoSCube() {
		cube.put(avgBW, null); 
		cube.put(avgSDUBW, null); 
		cube.put(peakBWduration, null); 
		cube.put(peakSDUBWduration, null); 
		cube.put(burstPeriod, null); 
		cube.put(burstDuration, null);  
		cube.put(undetectedBitErrorRate, null);
		cube.put(partialDeliveryAllowed, null); 
		cube.put(orderRequired, null); 
		cube.put(maxGapinSDUs, null); 
		cube.put(delay, null); 
		cube.put(jitter, null); 
	}

	
	public boolean isValidFormat(){
		//TODO: Add the format check
		return validFormat;
		
	}

}
