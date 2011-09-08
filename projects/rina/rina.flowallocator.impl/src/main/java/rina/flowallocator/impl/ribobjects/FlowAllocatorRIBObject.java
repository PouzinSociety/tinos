package rina.flowallocator.impl.ribobjects;

import java.util.Calendar;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.flowallocator.api.FlowAllocator;
import rina.flowallocator.impl.FlowAllocatorImpl;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObjectNames;

public class FlowAllocatorRIBObject extends BaseRIBObject{
	
	private FlowAllocatorImpl flowAllocator = null;
	
	public FlowAllocatorRIBObject(FlowAllocatorImpl flowAllocator, IPCProcess ipcProcess){
		super(ipcProcess, RIBObjectNames.DIF + RIBObjectNames.SEPARATOR + RIBObjectNames.RESOURCE_ALLOCATION + RIBObjectNames.SEPARATOR +
				RIBObjectNames.FLOW_ALLOCATOR, null, Calendar.getInstance().getTimeInMillis());
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
	

}
