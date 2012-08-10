package rina.resourceallocator.impl.ribobjects;

import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.FlowService;
import rina.resourceallocator.api.NMinus1FlowManager;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.ObjectInstanceGenerator;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;

public class NMinus1FlowSetRIBObject extends BaseRIBObject{
	
	public static final String N_MINUS_ONE_FLOW_SET_RIB_OBJECT_CLASS = "nminusone flow set";
	public static final String N_MINUS_ONE_FLOW_SET_RIB_OBJECT_NAME = RIBObjectNames.SEPARATOR + RIBObjectNames.DIF + 
		RIBObjectNames.SEPARATOR + RIBObjectNames.RESOURCE_ALLOCATION + RIBObjectNames.SEPARATOR + 
		RIBObjectNames.NMINUSONEFLOWMANAGER + RIBObjectNames.SEPARATOR + RIBObjectNames.NMINUSEONEFLOWS;;
	
	private NMinus1FlowManager nMinus1FlowManager = null;
	
	public NMinus1FlowSetRIBObject(NMinus1FlowManager nMinus1FlowManager, IPCProcess ipcProcess){
		super(ipcProcess, N_MINUS_ONE_FLOW_SET_RIB_OBJECT_CLASS, ObjectInstanceGenerator.getObjectInstance(), 
				N_MINUS_ONE_FLOW_SET_RIB_OBJECT_NAME);
		this.nMinus1FlowManager = nMinus1FlowManager;
	}
	
	/**
	 * A new N-1 flow has to be allocated
	 */
	@Override
	public void create(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException{
		//TODO implement this (call nMinus1FlowManager)
	}
	
	@Override
	public void create(String objectClass, long objectInstance, String objectName, Object object) throws RIBDaemonException{
		if (!(object instanceof FlowService)){
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
					"Object class ("+object.getClass().getName()+") does not match object name "+objectName);
		}
		
		NMinus1FlowRIBObject ribObject = new NMinus1FlowRIBObject(this.getIPCProcess(), objectName, 
				(FlowService) object, this.nMinus1FlowManager);
		this.addChild(ribObject);
		getRIBDaemon().addRIBObject(ribObject);
	}
	
	@Override
	public RIBObject read() throws RIBDaemonException{
		return this;
	}
	
	@Override
	public Object getObjectValue(){
		FlowService[] result = new FlowService[this.getChildren().size()];
		for(int i=0; i<result.length; i++){
			result[i] = (FlowService) this.getChildren().get(i).getObjectValue();
		}
		
		return result;
	}
}
