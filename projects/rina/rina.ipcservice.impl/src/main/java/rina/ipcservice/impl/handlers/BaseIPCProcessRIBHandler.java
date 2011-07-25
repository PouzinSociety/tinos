package rina.ipcservice.impl.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.ObjectValue;
import rina.encoding.api.BaseEncoder;
import rina.encoding.api.Encoder;
import rina.ipcservice.impl.IPCProcessImpl;
import rina.ipcservice.impl.ObjectNametoClassMapper;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBHandler;

public abstract class BaseIPCProcessRIBHandler implements RIBHandler{
	
	private static final Log log = LogFactory.getLog(BaseIPCProcessRIBHandler.class);
	protected IPCProcessImpl ipcProcess = null;
	private Encoder encoder = null;
	private RIBDaemon ribDaemon = null;
	
	public BaseIPCProcessRIBHandler(IPCProcessImpl ipcProcess){
		this.ipcProcess = ipcProcess;
	}
	
	public Encoder getEncoder(){
		if (this.encoder == null){
			this.encoder = (Encoder) ipcProcess.getIPCProcessComponent(BaseEncoder.getComponentName());
		}
		
		return this.encoder;
	}
	
	public RIBDaemon getRIBDaemon(){
		if (this.ribDaemon == null){
			this.ribDaemon = (RIBDaemon) ipcProcess.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		}
		
		return this.ribDaemon;
	}
	
	public void create(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException {
		CDAPMessage responseMessage = null;
		
		try{
			Object object = getEncoder().decode(cdapMessage.getObjValue().getByteval(), ObjectNametoClassMapper.getObjectClass(cdapMessage.getObjName()));
			this.create(cdapMessage.getObjClass(), cdapMessage.getObjName(), cdapMessage.getObjInst(), object);
			if (cdapMessage.getInvokeID() != 0){
				responseMessage = CDAPMessage.getCreateObjectResponseMessage(null, cdapMessage.getInvokeID(), 
						cdapMessage.getObjClass(), 0, cdapMessage.getObjName(), cdapMessage.getObjValue(), 0, null);
				getRIBDaemon().sendMessage(responseMessage, cdapSessionDescriptor.getPortId(), null);
			}
		}catch(Exception ex){
			try{
				responseMessage = CDAPMessage.getCreateObjectResponseMessage(null, cdapMessage.getInvokeID(), 
						cdapMessage.getObjClass(), 0, cdapMessage.getObjName(), cdapMessage.getObjValue(), 1, ex.getMessage());
				getRIBDaemon().sendMessage(responseMessage, cdapSessionDescriptor.getPortId(), null);
			}catch(CDAPException cdapEx){
				log.error(ex);
			}
		}
	}

	public void delete(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException {
		CDAPMessage responseMessage = null;
		
		try{
			this.delete(cdapMessage.getObjClass(), cdapMessage.getObjName(), cdapMessage.getObjInst(), null);
			if (cdapMessage.getInvokeID() != 0){
				responseMessage = CDAPMessage.getDeleteObjectResponseMessage(null, cdapMessage.getInvokeID(), 
						cdapMessage.getObjClass(), 0, cdapMessage.getObjName(), 0, null);
				getRIBDaemon().sendMessage(responseMessage, cdapSessionDescriptor.getPortId(), null);
			}
		}catch(Exception ex){
			try{
				responseMessage = CDAPMessage.getDeleteObjectResponseMessage(null, cdapMessage.getInvokeID(), 
						cdapMessage.getObjClass(), 0, cdapMessage.getObjName(), 1, ex.getMessage());
				getRIBDaemon().sendMessage(responseMessage, cdapSessionDescriptor.getPortId(), null);
			}catch(CDAPException cdapEx){
				log.error(ex);
			}
		}
	}

	public void read(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException {
		CDAPMessage responseMessage = null;
		
		try{
			Object object = this.read(cdapMessage.getObjClass(), cdapMessage.getObjName(), cdapMessage.getObjInst());
			ObjectValue objectValue = new ObjectValue();
			objectValue.setByteval(getEncoder().encode(object));
			responseMessage = CDAPMessage.getReadObjectResponseMessage(null, cdapMessage.getInvokeID(), cdapMessage.getObjClass(), 
					cdapMessage.getObjInst(), cdapMessage.getObjName(), objectValue, 0, null);
			getRIBDaemon().sendMessage(responseMessage, cdapSessionDescriptor.getPortId(), null);
		}catch(RIBDaemonException ex){
			try{
				responseMessage = CDAPMessage.getReadObjectResponseMessage(null, cdapMessage.getInvokeID(), cdapMessage.getObjClass(), 
						cdapMessage.getObjInst(), cdapMessage.getObjName(), null, 1, ex.getMessage());
				getRIBDaemon().sendMessage(responseMessage, cdapSessionDescriptor.getPortId(), null);
			}catch(CDAPException cdapEx){
				log.error(cdapEx);
			}
		}catch(Exception ex){
			log.error(ex);
		}
	}

	public void write(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException {
		CDAPMessage responseMessage = null;
		
		try{
			Object object = getEncoder().decode(cdapMessage.getObjValue().getByteval(), ObjectNametoClassMapper.getObjectClass(cdapMessage.getObjName()));
			this.write(cdapMessage.getObjClass(), cdapMessage.getObjName(), cdapMessage.getObjInst(), object);
			if (cdapMessage.getInvokeID() != 0){
				responseMessage = CDAPMessage.getWriteObjectResponseMessage(null, cdapMessage.getInvokeID(), 0, null);
				getRIBDaemon().sendMessage(responseMessage, cdapSessionDescriptor.getPortId(), null);
			}
		}catch(Exception ex){
			try{
				responseMessage = CDAPMessage.getWriteObjectResponseMessage(null, cdapMessage.getInvokeID(), 1, ex.getMessage());
				getRIBDaemon().sendMessage(responseMessage, cdapSessionDescriptor.getPortId(), null);
			}catch(CDAPException cdapEx){
				log.error(ex);
			}
		}
	}
	
	public void cancelRead(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException {
		//TODO
	}
	
	public void start(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException {
		this.start(cdapMessage.getObjClass(), cdapMessage.getObjName(), cdapMessage.getObjInst(), cdapMessage.getObjValue());
	}

	public void start(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		throw new RIBDaemonException(RIBDaemonException.OPERATION_NOT_ALLOWED_AT_THIS_OBJECT, 
				"Operation START not allowed for objectName "+objectName);
	}

	public void stop(CDAPMessage cdapMessage, CDAPSessionDescriptor cdapSessionDescriptor) throws RIBDaemonException {
		this.stop(cdapMessage.getObjClass(), cdapMessage.getObjName(), cdapMessage.getObjInst(), cdapMessage.getObjValue());
	}

	public void stop(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		throw new RIBDaemonException(RIBDaemonException.OPERATION_NOT_ALLOWED_AT_THIS_OBJECT, 
				"Operation STOP not allowed for objectName "+objectName);
	}
}
