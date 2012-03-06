package rina.flowallocator.impl.ribobjects;

import java.util.Calendar;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.flowallocator.api.FlowAllocatorInstance;
import rina.flowallocator.api.message.Flow;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;

public class FlowRIBObject extends BaseRIBObject{
	
	private FlowAllocatorInstance flowAllocatorInstance = null;
	
	public FlowRIBObject(IPCProcess ipcProcess, String objectName, FlowAllocatorInstance flowAllocatorInstance){
		super(ipcProcess, objectName, Flow.FLOW_RIB_OBJECT_CLASS, Calendar.getInstance().getTimeInMillis());
		this.flowAllocatorInstance = flowAllocatorInstance;
	}
	
	@Override
	public void delete(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException{
		flowAllocatorInstance.deleteFlowRequestMessageReceived(cdapMessage, cdapSessionDescriptor.getPortId());
	}
	
	@Override
	public void delete(String objectClass, String objectName, long objectInstance) throws RIBDaemonException {
		this.getParent().removeChild(objectName);
	}
	
	@Override
	public RIBObject read(String objectClass, String objectName, long objectInstance) throws RIBDaemonException{
		return this;
	}
	
	@Override
	public Object getObjectValue(){
		return flowAllocatorInstance.getFlow();
	}
}
