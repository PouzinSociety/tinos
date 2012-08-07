package rina.resourceallocator.impl.pduforwarding.lookuptable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QoSIdNode {
	
	private int portId = -1;
	
	private Map<Long, CEPIdNode> cepIdNodes = null;
	
	public QoSIdNode(){
		cepIdNodes = new ConcurrentHashMap<Long, CEPIdNode>();
	}
	
	public void addCEPIdNode(long cepId, int portId){
		CEPIdNode node = new CEPIdNode(portId);
		this.cepIdNodes.put(new Long(cepId), node);
	}
	
	public Map<Long, CEPIdNode> getCEPIdNodes(){
		return this.cepIdNodes;
	}
	
	public CEPIdNode getCEPIdNode(long cepId){
		return this.cepIdNodes.get(new Long(cepId));
	}
	
	public void removeCEPIdNode(long cepId){
		this.cepIdNodes.remove(new Long(cepId));
	}
	
	public void setPortId(int portId){
		this.portId = portId;
	}
	
	public int getPortId(){
		return this.portId;
	}
}
