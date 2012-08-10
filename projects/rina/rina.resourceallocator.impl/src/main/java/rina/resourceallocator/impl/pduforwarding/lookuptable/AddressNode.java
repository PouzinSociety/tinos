package rina.resourceallocator.impl.pduforwarding.lookuptable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AddressNode {
	
	private int[] portIds = null;
	
	private Map<Integer, QoSIdNode> qosIdNodes = null;
	
	public AddressNode(){
		qosIdNodes = new ConcurrentHashMap<Integer, QoSIdNode>();
	}
	
	public void addQoSIdNode(int qosId, int[] portIds){
		QoSIdNode node = new QoSIdNode(portIds);
		this.qosIdNodes.put(new Integer(qosId), node);
	}
	
	public Map<Integer, QoSIdNode> getQoSIdNodes(){
		return this.qosIdNodes;
	}
	
	public QoSIdNode getQoSIdNode(int qosId){
		return this.qosIdNodes.get(new Integer(qosId));
	}
	
	public void removeQoSIdNode(int qosId){
		this.qosIdNodes.remove(new Integer(qosId));
	}
	
	public void setPortIds(int[] portIds){
		this.portIds = portIds;
	}
	
	public int[] getPortIds(){
		return this.portIds;
	}

}
