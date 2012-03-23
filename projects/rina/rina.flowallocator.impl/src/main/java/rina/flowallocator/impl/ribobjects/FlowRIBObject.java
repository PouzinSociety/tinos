package rina.flowallocator.impl.ribobjects;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.flowallocator.api.FlowAllocatorInstance;
import rina.flowallocator.api.message.Flow;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.SimpleSetMemberRIBObject;

public class FlowRIBObject extends SimpleSetMemberRIBObject{
	
	private FlowAllocatorInstance flowAllocatorInstance = null;
	
	public FlowRIBObject(IPCProcess ipcProcess, String objectName, FlowAllocatorInstance flowAllocatorInstance){
		super(ipcProcess, Flow.FLOW_RIB_OBJECT_CLASS, objectName, flowAllocatorInstance.getFlow());
		this.flowAllocatorInstance = flowAllocatorInstance;
	}
	
	@Override
	public void delete(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException{
		flowAllocatorInstance.deleteFlowRequestMessageReceived(cdapMessage, cdapSessionDescriptor.getPortId());
	}
	
	@Override
	public Object getObjectValue(){
		return flowAllocatorInstance.getFlow();
	}
}
