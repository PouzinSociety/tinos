package rina.flowallocator.impl.ribobjects;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.flowallocator.impl.FlowAllocatorImpl;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.ObjectInstanceGenerator;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;

public class FlowSetRIBObject extends BaseRIBObject{
	
	private FlowAllocatorImpl flowAllocator = null;
	
	public FlowSetRIBObject(FlowAllocatorImpl flowAllocator, IPCProcess ipcProcess){
		super(ipcProcess, RIBObjectNames.SEPARATOR + RIBObjectNames.DIF + RIBObjectNames.SEPARATOR + RIBObjectNames.RESOURCE_ALLOCATION + RIBObjectNames.SEPARATOR +
				RIBObjectNames.FLOW_ALLOCATOR + RIBObjectNames.SEPARATOR + RIBObjectNames.FLOWS, "flow set", ObjectInstanceGenerator.getObjectInstance());
		this.flowAllocator = flowAllocator;
	}
	
	/**
	 * A new Flow has to be created
	 */
	@Override
	public void create(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException{
		flowAllocator.createFlowRequestMessageReceived(cdapMessage, cdapSessionDescriptor.getPortId());
	}
	
	@Override
	public Object getObjectValue(){
		return null;
	}
	
	@Override
	public RIBObject read(String objectClass, String objectName, long objectInstance) throws RIBDaemonException{
		return this;
	}
}
