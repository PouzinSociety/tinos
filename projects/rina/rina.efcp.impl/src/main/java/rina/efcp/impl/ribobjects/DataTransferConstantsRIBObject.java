package rina.efcp.impl.ribobjects;

import rina.cdap.api.BaseCDAPSessionManager;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.ObjectValue;
import rina.efcp.api.DataTransferConstants;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.ObjectInstanceGenerator;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;

public class DataTransferConstantsRIBObject extends BaseRIBObject{

	private DataTransferConstants objectValue = null;
	
	public DataTransferConstantsRIBObject(IPCProcess ipcProcess, DataTransferConstants value) {
		super(ipcProcess, DataTransferConstants.DATA_TRANSFER_CONSTANTS_RIB_OBJECT_CLASS, 
				ObjectInstanceGenerator.getObjectInstance(), 
				DataTransferConstants.DATA_TRANSFER_CONSTANTS_RIB_OBJECT_NAME);
		this.objectValue = value;
	}
	
	@Override
	public RIBObject read() throws RIBDaemonException{
		return this;
	}
	
	@Override
	public void read(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException{
		try{
			ObjectValue objectValue = new ObjectValue();
			objectValue.setByteval(this.getEncoder().encode(this.objectValue));
			CDAPSessionManager cdapSessionManager = (CDAPSessionManager)
				this.getIPCProcess().getIPCProcessComponent(BaseCDAPSessionManager.getComponentName());
			CDAPMessage responseMessage = cdapSessionManager.getReadObjectResponseMessage(cdapSessionDescriptor.getPortId(), 
					null, this.getObjectClass(), this.getObjectInstance(), this.getObjectName(), objectValue, 0, null, 
					cdapMessage.getInvokeID());
			this.getRIBDaemon().sendMessage(responseMessage, cdapSessionDescriptor.getPortId(), null);
		}catch(Exception ex){
			throw new RIBDaemonException(RIBDaemonException.PROBLEMS_SENDING_CDAP_MESSAGE, ex.getMessage());
		}
	}
	
	@Override
	public void create(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException{
		if (cdapMessage.getObjValue() == null){
			throw new RIBDaemonException(RIBDaemonException.OBJECT_VALUE_IS_NULL, "Object value is null");
		}
		
		try{
			DataTransferConstants value = (DataTransferConstants) this.getEncoder().
				decode(cdapMessage.getObjValue().getByteval(), DataTransferConstants.class);
			this.getRIBDaemon().create(cdapMessage.getObjClass(), cdapMessage.getObjInst(), 
					cdapMessage.getObjName(), value, null);
		}catch(Exception ex){
			throw new RIBDaemonException(RIBDaemonException.PROBLEMS_DECODING_OBJECT, ex.getMessage());
		}
	}
	
	/**
	 * In this case create has the semantics of update 
	 */
	@Override
	public void create(String objectClass, long objectInstance, String objectName, Object value) throws RIBDaemonException{
		this.write(value);
	}
	
	@Override
	public void write(Object value) throws RIBDaemonException {
		DataTransferConstants candidate = null;
		
		if (!(value instanceof DataTransferConstants)){
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
					"Value is not an instance of "+DataTransferConstants.class.getName());
		}
		
		candidate = (DataTransferConstants) value;
		objectValue.setAddressLength(candidate.getAddressLength());
		objectValue.setCepIdLength(candidate.getCepIdLength());
		objectValue.setCompleteFlag(candidate.getCompleteFlag());
		objectValue.setDIFConcatenation(candidate.isDIFConcatenation());
		objectValue.setDIFFragmentation(candidate.isDIFFragmentation());
		objectValue.setDIFIntegrity(candidate.isDIFIntegrity());
		objectValue.setFirstFragmentFlag(candidate.getFirstFragmentFlag());
		objectValue.setFragmentFlag(candidate.getFragmentFlag());
		objectValue.setLastFragmentFlag(candidate.getLastFragmentFlag());
		objectValue.setLengthLength(candidate.getLengthLength());
		objectValue.setMaxPDULifetime(candidate.getMaxPDULifetime());
		objectValue.setMaxPDUSize(candidate.getMaxPDUSize());
		objectValue.setMultipleFlag(candidate.getMultipleFlag());
		objectValue.setPortIdLength(candidate.getPortIdLength());
		objectValue.setQosIdLength(candidate.getQosIdLength());
		objectValue.setSDUGapTimerDelay(candidate.getSDUGapTimerDelay());
		objectValue.setSequenceNumberLength(candidate.getSequenceNumberLength());	
	}

	@Override
	public Object getObjectValue() {
		return objectValue;
	}
}
