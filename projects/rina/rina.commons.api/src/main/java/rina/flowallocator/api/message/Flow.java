package rina.flowallocator.api.message;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import rina.flowallocator.api.ConnectionId;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.QualityOfServiceSpecification;

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
	private long sourcePortId = 0;
	
	/**
	 * The port-id returned to the destination Application process. This port-id is used for 
	 * the life of the flow.
	 */
	private long destinationPortId = 0;
	
	/**
	 * The address of the IPC process that is the source of this flow
	 */
	private long sourceAddress = 0;
	
	/**
	 * The address of the IPC process that is the destination of this flow
	 */
	private long destinationAddress = 0;
	
	/**
	 * All the possible flowIds of this flow
	 */
	private List<ConnectionId> flowIds = null;
	
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
	private QualityOfServiceSpecification qosParameters = null;
	
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
	private byte[] accessControl = null;
	
	/**
	 * Maximum number of retries to create the flow before giving up.
	 */
	private int maxCreateFlowRetries = 0;
	
	/**
	 * The current number of retries
	 */
	private int createFlowRetries = 0;
	
	/** 
	 * While the search rules that generate the forwarding table should allow for a 
	 * natural termination condition, it seems wise to have the means to enforce termination.
	 */  
	private int hopCount = 0;
	
	 /**
	  * The port number that has to match the one sent over the TCP connection
	  */
	private int tcpRendezvousId = 0;
	
	public Flow(){
		flowIds = new ArrayList<ConnectionId>();
		policies = new ArrayList<String>();
		policyParameters = new Hashtable<String, String>();
	}

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

	public long getSourcePortId() {
		return sourcePortId;
	}

	public void setSourcePortId(long sourcePortId) {
		this.sourcePortId = sourcePortId;
	}

	public long getDestinationPortId() {
		return destinationPortId;
	}

	public void setDestinationPortId(long destinationPortId) {
		this.destinationPortId = destinationPortId;
	}

	public long getSourceAddress() {
		return sourceAddress;
	}

	public void setSourceAddress(long sourceAddress) {
		this.sourceAddress = sourceAddress;
	}

	public long getDestinationAddress() {
		return destinationAddress;
	}

	public void setDestinationAddress(long destinationAddress) {
		this.destinationAddress = destinationAddress;
	}

	public List<ConnectionId> getFlowIds() {
		return flowIds;
	}

	public void setFlowIds(List<ConnectionId> flowIds) {
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

	public QualityOfServiceSpecification getQosParameters() {
		return qosParameters;
	}

	public void setQosParameters(QualityOfServiceSpecification qosParameters) {
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

	public byte[] getAccessControl() {
		return accessControl;
	}

	public void setAccessControl(byte[] accessControl) {
		this.accessControl = accessControl;
	}

	public int getMaxCreateFlowRetries() {
		return maxCreateFlowRetries;
	}

	public void setMaxCreateFlowRetries(int maxCreateFlowRetries) {
		this.maxCreateFlowRetries = maxCreateFlowRetries;
	}

	public int getCreateFlowRetries() {
		return createFlowRetries;
	}

	public void setCreateFlowRetries(int createFlowRetries) {
		this.createFlowRetries = createFlowRetries;
	}

	public int getHopCount() {
		return hopCount;
	}

	public void setHopCount(int hopCount) {
		this.hopCount = hopCount;
	}
	
	public int getTcpRendezvousId() {
		return tcpRendezvousId;
	}

	public void setTcpRendezvousId(int tcpRendezvousId) {
		this.tcpRendezvousId = tcpRendezvousId;
	}

	public String toString(){
		String result = "";
		result = result + "Max create flow retries: " + this.getMaxCreateFlowRetries() + "\n";
		result = result + "Hop count: " + this.getHopCount() + "\n";
		result = result + "Source AP Naming Info: "+this.sourceNamingInfo;
		result = result + "Source address: " + this.getSourceAddress() + "\n";
		result = result + "Source port id: " + this.getSourcePortId() + "\n";
		result = result + "Destination AP Naming Info: "+ this.getDestinationNamingInfo();
		result = result + "Destination addres: " + this.getDestinationAddress() + "\n";
		result = result + "Destination port id: "+ this.getDestinationPortId() + "\n";
		result = result + "TCP Rendez-vous id: " + this.getTcpRendezvousId() + "\n";
		return result;
	}
}
