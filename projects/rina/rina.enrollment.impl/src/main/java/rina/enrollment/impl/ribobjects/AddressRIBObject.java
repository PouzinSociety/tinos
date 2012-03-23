package rina.enrollment.impl.ribobjects;

import rina.enrollment.impl.EnrollmentTaskImpl;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.ObjectInstanceGenerator;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * Handles the operations related to the "daf.management.naming.currentsynonym" objects
 * @author eduardgrasa
 *
 */
public class AddressRIBObject extends BaseRIBObject{

	private Long synonym = null;
	
	public AddressRIBObject(IPCProcess ipcProcess, EnrollmentTaskImpl enrollmentTask){
		super(ipcProcess, RIBObjectNames.ADDRESS_RIB_OBJECT_CLASS, 
				ObjectInstanceGenerator.getObjectInstance(), RIBObjectNames.ADDRESS_RIB_OBJECT_NAME);
	}
	
	@Override
	public RIBObject read() throws RIBDaemonException{
		return this;
	}

	@Override
	public void write(Object object) throws RIBDaemonException {
		if (!(object instanceof Long)){
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
					"Object class ("+object.getClass().getName()+") does not match object name "+this.getObjectName());
		}
		
		this.synonym = (Long) object;
	}
	
	@Override
	public Object getObjectValue(){
		return synonym;
	}

}
