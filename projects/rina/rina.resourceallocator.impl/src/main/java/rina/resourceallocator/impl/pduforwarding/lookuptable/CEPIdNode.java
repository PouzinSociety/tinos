package rina.resourceallocator.impl.pduforwarding.lookuptable;

public class CEPIdNode {
	
	private int portId = 0;
	
	public CEPIdNode(int portId){
		this.portId = portId;
	}
	
	public int getPortId(){
		return this.portId;
	}
}
