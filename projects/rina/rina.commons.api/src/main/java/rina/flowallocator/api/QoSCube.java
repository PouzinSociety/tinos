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
	private int[] qosId = null;
	
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
	 * True if SDUs must be delivered in order
	 */
	private boolean qosOrder = false;
	
	/**
	 * Maximum allowable gap in SDUs
	 */
	private int qosMaxGap = 0;

	public int[] getQosId() {
		return qosId;
	}

	public void setQosId(int[] qosId) {
		this.qosId = qosId;
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

	public boolean isPartialDelivery() {
		return partialDelivery;
	}

	public void setPartialDelivery(boolean partialDelivery) {
		this.partialDelivery = partialDelivery;
	}

	public boolean isQosOrder() {
		return qosOrder;
	}

	public void setQosOrder(boolean qosOrder) {
		this.qosOrder = qosOrder;
	}

	public int getQosMaxGap() {
		return qosMaxGap;
	}

	public void setQosMaxGap(int qosMaxGap) {
		this.qosMaxGap = qosMaxGap;
	}
}