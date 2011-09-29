package rina.efcp.impl.ribobjects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.efcp.api.DataTransferConstants;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.ObjectInstanceGenerator;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * Handles the operations related to the "daf.management.operationalStatus" objects
 * @author eduardgrasa
 *
 */
public class DataTransferConstantsRIBObject extends BaseRIBObject{
	
	private static final Log log = LogFactory.getLog(DataTransferConstantsRIBObject.class);

	private DataTransferConstants dataTransferConstants = null;
	
	public DataTransferConstantsRIBObject(IPCProcess ipcProcess){
		super(ipcProcess, RIBObjectNames.SEPARATOR + RIBObjectNames.DIF + RIBObjectNames.SEPARATOR + RIBObjectNames.IPC + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.DATA_TRANSFER+ RIBObjectNames.SEPARATOR + RIBObjectNames.CONSTANTS, 
				"datatransfercons", ObjectInstanceGenerator.getObjectInstance());
	}

	@Override
	public RIBObject read(String objectClass, String objectName, long objectInstance) throws RIBDaemonException{
		return this;
	}

	@Override
	public void write(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		if (!(object instanceof DataTransferConstants)){
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
					"Object class ("+object.getClass().getName()+") does not match object name "+objectName);
		}
		
		this.dataTransferConstants = (DataTransferConstants) object;
	}
	
	@Override
	public Object getObjectValue(){
		return dataTransferConstants;
	}


}
