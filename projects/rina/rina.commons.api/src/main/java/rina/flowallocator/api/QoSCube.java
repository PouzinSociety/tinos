package rina.flowallocator.api;

import java.util.List;
import java.util.Map;

/**
 * Encapsulates the Quality of Service cube data.
 * @author eduardgrasa
 *
 */
public class QoSCube {
	
	/**
	 * Identifies the type of QoS-cube
	 */
	private byte[] qosId = null;
	
	/**
	 * in bytes/s, a value of 0 indicates 'don't care'
	 */
	private long averageBandwidth = 0;
	
	/**
	 * in bytes/s, a value of 0 indicates 'don't care'
	 */
	private long averageSDUBandwidth = 0;
	
	/**
	 * in ms, a value of 0 indicates 'don't care'
	 */
	private int peakBandwidthDuration = 0;
	
	/**
	 * in ms, a value of 0 indicates 'don't care'
	 */
	private int peakSDUBandwidthDuration = 0;
	
	/**
	 * a value of 0 indicates 'don`t care'
	 */
	private double undetectedBitErrorRate = 0;
	
	/**
	 * True if partial SDUs can be delivered to the application. This was intended to provide a 
	 * "stream"-like connection. However, that can be done purely at the API level: any undelimited
	 * connection should behave like a stream, The sender accepts SDUs of any lentgh which the
	 * receiver receives and reassembles, as usual. The receiver passes the data up to the AP in 
	 * chunks of whatever size, splitting the data across multiple buffers, if required. An alternate 
	 * use of this - specifying that a received SDU that is too big for the AP's buffer to be 
	 * truncated, rather than raising an exception of some sort, is also at the API level, rather 
	 * than the QoS-cube level. Therefore, this parameter should be REMOVED from the QoS-cube.
	 */
	private boolean partialDelivery = false;
	
	/**
	 * Indicates if SDUs have to be delivered in order
	 */
	private boolean order = false;
	
	/**
	 * Indicates the maximum gap allowed in SDUs, a gap of N SDUs is considered the same as all SDUs delivered. 
	 * A value of -1 indicates 'Any'
	 */
	private int maxAllowableGapSdu = 0;
	
	/**
	 * In milliseconds, indicates the maximum delay allowed in this flow. A value of 0 indicates don't care
	 */
	private int delay = 0;
	
	/**
	 * In milliseconds, indicates indicates the maximum jitter allowed in this flow. A value of 0 indicates don't care
	 */
	private int jitter = 0;
	
	/**
	 * It is expected that within a DIF, selecting a particular QoS-cube implies that a 
	 * specific set of policies are necessary to achieve QoS. Therefore, the QoS-cube
	 * contains a list of policies.
	 */
	private List<String> policies = null;
	
	/**
	 * Policies may contain parameters that control their behavior. The QoS cube should contain 
	 * the default values for such parameters. A connection should contain the actual values 
	 * for the policy parameters.
	 */
	private Map<String, String> policyDefaultParameters = null;

	public byte[] getQosId() {
		return qosId;
	}

	public void setQosId(byte[] qosId) {
		this.qosId = qosId;
	}

	public long getAverageBandwidth() {
		return averageBandwidth;
	}

	public void setAverageBandwidth(long averageBandwidth) {
		this.averageBandwidth = averageBandwidth;
	}

	public long getAverageSDUBandwidth() {
		return averageSDUBandwidth;
	}

	public void setAverageSDUBandwidth(long averageSDUBandwidth) {
		this.averageSDUBandwidth = averageSDUBandwidth;
	}

	public int getPeakBandwidthDuration() {
		return peakBandwidthDuration;
	}

	public void setPeakBandwidthDuration(int peakBandwidthDuration) {
		this.peakBandwidthDuration = peakBandwidthDuration;
	}

	public int getPeakSDUBandwidthDuration() {
		return peakSDUBandwidthDuration;
	}

	public void setPeakSDUBandwidthDuration(int peakSDUBandwidthDuration) {
		this.peakSDUBandwidthDuration = peakSDUBandwidthDuration;
	}

	public double getUndetectedBitErrorRate() {
		return undetectedBitErrorRate;
	}

	public void setUndetectedBitErrorRate(double undetectedBitErrorRate) {
		this.undetectedBitErrorRate = undetectedBitErrorRate;
	}

	public boolean isPartialDelivery() {
		return partialDelivery;
	}

	public void setPartialDelivery(boolean partialDelivery) {
		this.partialDelivery = partialDelivery;
	}

	public boolean isOrder() {
		return order;
	}

	public void setOrder(boolean order) {
		this.order = order;
	}

	public int getMaxAllowableGapSdu() {
		return maxAllowableGapSdu;
	}

	public void setMaxAllowableGapSdu(int maxAllowableGapSdu) {
		this.maxAllowableGapSdu = maxAllowableGapSdu;
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public int getJitter() {
		return jitter;
	}

	public void setJitter(int jitter) {
		this.jitter = jitter;
	}

	public List<String> getPolicies() {
		return policies;
	}

	public void setPolicies(List<String> policies) {
		this.policies = policies;
	}

	public Map<String, String> getPolicyDefaultParameters() {
		return policyDefaultParameters;
	}

	public void setPolicyDefaultParameters(
			Map<String, String> policyDefaultParameters) {
		this.policyDefaultParameters = policyDefaultParameters;
	}
	
	@Override
	public String toString(){
		String result = "";
		result = result + "QoS id: " + this.getQosId() +  "\n";
		result = result + "Average bandwidth: " + this.getAverageBandwidth() +  "\n";
		result = result + "Average SDU bandwidth: " + this.getAverageSDUBandwidth() +  "\n";
		result = result + "Peak bandwidth duration: " + this.getPeakBandwidthDuration() +  "\n";
		result = result + "Peak SDU bandwidth duration: " + this.getPeakSDUBandwidthDuration() +  "\n";
		result = result + "Undetected bit error rate: " + this.getUndetectedBitErrorRate()+  "\n";
		result = result + "Partial Delivery: " + this.isPartialDelivery() +  "\n";
		result = result + "Order: " + this.isOrder() +  "\n";
		result = result + "Max allowable gap SDU: " + this.getMaxAllowableGapSdu() +  "\n";
		result = result + "Delay: " + this.getDelay() +  "\n";
		result = result + "Jitter: " + this.getJitter() +  "\n";
		return result;
	}
}