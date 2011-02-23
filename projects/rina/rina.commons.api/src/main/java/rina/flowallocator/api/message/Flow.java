package rina.flowallocator.api.message;

import java.util.List;
import java.util.Map;

import rina.flowallocator.api.ConnectionId;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.QoSParameters;
import rina.utils.types.Unsigned;

/**
 * Encapsulates all the information required to manage a Flow
 * @author eduardgrasa
 *
 */
public class Flow {

	/**
	 * The application that requested the flow
	 */
	private ApplicationProcessNamingInfo sourceNamingInfo = null;
	
	/**
	 * The destination application of the flow
	 */
	private ApplicationProcessNamingInfo destinationNamingInfo = null;
	
	/**
	 * The port-id returned to the Application process that requested the flow. This port-id is used for 
	 * the life of the flow.
	 */
	private Unsigned sourcePortId = null;
	
	/**
	 * The port-id returned to the destination Application process. This port-id is used for 
	 * the life of the flow.
	 */
	private Unsigned destinationPortId = null;
	
	/**
	 * The address of the IPC process that is the source of this flow
	 */
	private byte[] sourceAddress = null;
	
	/**
	 * The address of the IPC process that is the destination of this flow
	 */
	private byte[] destinationAddress = null;
	
	/**
	 * All the possible flowIds of this flow
	 */
	private ConnectionId[] flowIds = null;
	
	/**
	 * The index of the current flowId
	 */
	private int currentFlowId = 0;
	
	/**
	 * The status of this flow
	 */
	private byte status = 0x00;
	
	/**
	 * The list of parameters from the AllocateRequest that generated this flow
	 */
	private QoSParameters qosParameters = null;
	
	/**
	 * The list of policies that are used to control this flow. NOTE: Does this provide 
	 * anything beyond the list used within the QoS-cube? Can we override or specialize those, 
	 * somehow?
	 */
	private List<String> policies = null;
	
	/**
	 * The merged list of parameters from QoS.policy-Default-Parameters and QoS-Params.
	 */
	private Map<String, String> policyParameters = null;
	
	/**
	 * TODO this is just a placeHolder for this piece of data
	 */
	private String accessControl = null;
	
	/**
	 * Maximum number of retries to create the flow before giving up.
	 */
	private Unsigned maxCreateFlowRetries = null;
	
	/**
	 * The current number of retries
	 */
	private Unsigned createFlowRetries = null;
	
	/** 
	 * While the search rules that generate the forwarding table should allow for a 
	 * natural termination condition, it seems wise to have the means to enforce termination.
	 */  
	private Unsigned hopCount = null;

	public ApplicationProcessNamingInfo getSourceNamingInfo() {
		return sourceNamingInfo;
	}

	public void setSourceNamingInfo(ApplicationProcessNamingInfo sourceNamingInfo) {
		this.sourceNamingInfo = sourceNamingInfo;
	}

	public ApplicationProcessNamingInfo getDestinationNamingInfo() {
		return destinationNamingInfo;
	}

	public void setDestinationNamingInfo(
			ApplicationProcessNamingInfo destinationNamingInfo) {
		this.destinationNamingInfo = destinationNamingInfo;
	}

	public Unsigned getSourcePortId() {
		return sourcePortId;
	}

	public void setSourcePortId(Unsigned sourcePortId) {
		this.sourcePortId = sourcePortId;
	}

	public Unsigned getDestinationPortId() {
		return destinationPortId;
	}

	public void setDestinationPortId(Unsigned destinationPortId) {
		this.destinationPortId = destinationPortId;
	}

	public byte[] getSourceAddress() {
		return sourceAddress;
	}

	public void setSourceAddress(byte[] sourceAddress) {
		this.sourceAddress = sourceAddress;
	}

	public byte[] getDestinationAddress() {
		return destinationAddress;
	}

	public void setDestinationAddress(byte[] destinationAddress) {
		this.destinationAddress = destinationAddress;
	}

	public ConnectionId[] getFlowIds() {
		return flowIds;
	}

	public void setFlowIds(ConnectionId[] flowIds) {
		this.flowIds = flowIds;
	}

	public int getCurrentFlowId() {
		return currentFlowId;
	}

	public void setCurrentFlowId(int currentFlowId) {
		this.currentFlowId = currentFlowId;
	}

	public byte getStatus() {
		return status;
	}

	public void setStatus(byte status) {
		this.status = status;
	}

	public QoSParameters getQosParameters() {
		return qosParameters;
	}

	public void setQosParameters(QoSParameters qosParameters) {
		this.qosParameters = qosParameters;
	}

	public List<String> getPolicies() {
		return policies;
	}

	public void setPolicies(List<String> policies) {
		this.policies = policies;
	}

	public Map<String, String> getPolicyParameters() {
		return policyParameters;
	}

	public void setPolicyParameters(Map<String, String> policyParameters) {
		this.policyParameters = policyParameters;
	}

	public String getAccessControl() {
		return accessControl;
	}

	public void setAccessControl(String accessControl) {
		this.accessControl = accessControl;
	}

	public Unsigned getMaxCreateFlowRetries() {
		return maxCreateFlowRetries;
	}

	public void setMaxCreateFlowRetries(Unsigned maxCreateFlowRetries) {
		this.maxCreateFlowRetries = maxCreateFlowRetries;
	}

	public Unsigned getCreateFlowRetries() {
		return createFlowRetries;
	}

	public void setCreateFlowRetries(Unsigned createFlowRetries) {
		this.createFlowRetries = createFlowRetries;
	}

	public Unsigned getHopCount() {
		return hopCount;
	}

	public void setHopCount(Unsigned hopCount) {
		this.hopCount = hopCount;
	}
}
