package rina.flowallocator.impl.ribobjects;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.flowallocator.api.FlowAllocator;
import rina.flowallocator.api.FlowAllocatorInstance;
import rina.flowallocator.api.message.Flow;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.ObjectInstanceGenerator;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;

public class FlowSetRIBObject extends BaseRIBObject{
	
	private FlowAllocator flowAllocator = null;
	
	public FlowSetRIBObject(FlowAllocator flowAllocator, IPCProcess ipcProcess){
		super(ipcProcess, Flow.FLOW_SET_RIB_OBJECT_NAME, Flow.FLOW_SET_RIB_OBJECT_CLASS, ObjectInstanceGenerator.getObjectInstance());
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
	public void create(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException{
		if (!(object instanceof FlowAllocatorInstance)){
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
					"Object class ("+object.getClass().getName()+") does not match object name "+objectName);
		}
		
		FlowRIBObject ribObject = new FlowRIBObject(this.getIPCProcess(), objectName, (FlowAllocatorInstance) object);
		this.addChild(ribObject);
		getRIBDaemon().addRIBObject(ribObject);
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