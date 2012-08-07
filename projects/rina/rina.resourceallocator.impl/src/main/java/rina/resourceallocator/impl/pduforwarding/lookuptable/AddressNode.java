package rina.resourceallocator.impl.pduforwarding.lookuptable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AddressNode {
	
	private int portId = -1;
	
	private Map<Integer, QoSIdNode> qosIdNodes = null;
	
	public AddressNode(){
		qosIdNodes = new ConcurrentHashMap<Integer, QoSIdNode>();
	}
	
	public void addQoSIdNode(int qosId, int portId){
		QoSIdNode node = new QoSIdNode();
		if (portId != -1){
			node.setPortId(portId);
		}
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
	
	public void setPortId(int portId){
		this.portId = portId;
	}
	
	public int getPortId(){
		return this.portId;
	}

}
