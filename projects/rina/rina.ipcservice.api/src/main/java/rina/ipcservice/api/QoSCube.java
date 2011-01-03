package rina.ipcservice.api;

import java.util.*;

/**
 * Definition of QoS cube
 *
 */
public class QoSCube {

	private Map<String, Object> cube = new HashMap<String, Object>();

	public boolean validFormat = false;
	
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

	public Map<String, Object> getCube() {
		return cube;
	}

	public void setCube(Map<String, Object> cube) {
		this.cube = cube;
	}
	
	public boolean isValidFormat(){
		//TODO: Add the format check
		return validFormat;
		
	}
}
