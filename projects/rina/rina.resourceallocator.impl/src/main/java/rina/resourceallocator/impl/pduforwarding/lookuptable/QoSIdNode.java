package rina.resourceallocator.impl.pduforwarding.lookuptable;

public class QoSIdNode {
	
	private int[] portIds = null;
	
	public QoSIdNode(int[] portIds){
		this.portIds = portIds;
	}
	
	public int[] getPortIds(){
		return this.portIds;
	}
}
