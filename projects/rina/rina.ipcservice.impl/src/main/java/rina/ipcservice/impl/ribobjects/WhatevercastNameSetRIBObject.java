package rina.ipcservice.impl.ribobjects;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.WhatevercastName;
import rina.ipcservice.impl.IPCProcessImpl;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * Handles the operations related to the "daf.management.naming.whatevercastnames" objects
 * @author eduardgrasa
 *
 */
public class WhatevercastNameSetRIBObject extends BaseRIBObject{
	
	private static final Log log = LogFactory.getLog(WhatevercastNameSetRIBObject.class);
	
	public WhatevercastNameSetRIBObject(IPCProcessImpl ipcProcess){
		super(ipcProcess, RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.WHATEVERCAST_NAMES, 
				null, Calendar.getInstance().getTimeInMillis());
	}

	@Override
	public void create(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException{
		if (!(object instanceof WhatevercastName)){
			throw new RIBDaemonException(RIBDaemonException.OBJECTCLASS_DOES_NOT_MATCH_OBJECTNAME, 
					"Object class ("+object.getClass().getName()+") does not match object name "+objectName);
		}
		
		WhatevercastNameRIBObject ribObject = new WhatevercastNameRIBObject(this.getIPCProcess(), objectName, (WhatevercastName) object);
		this.addChild(ribObject);
		getRIBDaemon().addRIBObject(ribObject);
	}

	@Override
	public void delete(String objectClass, String objectName, long objectInstance, Object object) throws RIBDaemonException {
		String childName = null;
		List<String> childrenNames = new ArrayList<String>();
		
		for(int i=0; i<this.getChildren().size(); i++){
			childName = this.getChildren().get(i).getObjectName();
			childrenNames.add(childName);
			getRIBDaemon().delete(null, childName, 0, null);
		}
		
		for(int i=0; i<childrenNames.size(); i++){
			this.removeChild(childrenNames.get(i));
		}
	}

	@Override
	public Object read(String objectClass, String objectName, long objectInstance) throws RIBDaemonException{
		List<WhatevercastName> whatevercastNames = new ArrayList<WhatevercastName>();
		
		for(int i=0; i<this.getChildren().size(); i++){
			RIBObject ribObject = this.getChildren().get(i);
			WhatevercastName whatevercastName = (WhatevercastName) ribObject.read(ribObject.getObjectClass(), ribObject.getObjectName(), ribObject.getObjectInstance());
			whatevercastNames.add(whatevercastName);
		}
		
		return whatevercastNames;
	}
}