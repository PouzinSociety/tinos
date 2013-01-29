package rina.resourceallocator.impl.ribobjects;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.ipcprocess.api.IPCProcess;
import rina.resourceallocator.api.NMinus1FlowDescriptor;
import rina.resourceallocator.api.NMinus1FlowManager;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.SimpleSetMemberRIBObject;

public class NMinus1FlowRIBObject extends SimpleSetMemberRIBObject{
	
	public static final String N_MINUS_ONE_FLOW_RIB_OBJECT_CLASS = "nminusone flow";
	
	private NMinus1FlowDescriptor nMinus1FlowDescriptor = null;
	private NMinus1FlowManager nMinus1FlowManager = null;
	
	public NMinus1FlowRIBObject(IPCProcess ipcProcess, String objectName, NMinus1FlowDescriptor nMinus1FlowDescriptor, 
			NMinus1FlowManager nMinus1FlowManager){
		super(ipcProcess, N_MINUS_ONE_FLOW_RIB_OBJECT_CLASS, objectName, nMinus1FlowDescriptor);
		this.nMinus1FlowDescriptor = nMinus1FlowDescriptor;
		this.nMinus1FlowManager = nMinus1FlowManager;
	}
	
	@Override
	public void delete(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException{
		//TODO implement this by calling the N Minus 1 Flow Manager
	}
	
	@Override
	public Object getObjectValue(){
		return nMinus1FlowDescriptor;
	}
}
