package rina.enrollment.impl.ribobjects;

import java.util.Calendar;

import rina.enrollment.impl.EnrollmentTaskImpl;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBObject;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * Handles the operations related to the "daf.management.operationalStatus" objects
 * @author eduardgrasa
 *
 */
public class OperationalStatusRIBObject extends BaseRIBObject{

	private EnrollmentTaskImpl enrollmentTask = null;

	public OperationalStatusRIBObject(EnrollmentTaskImpl enrollmentTaskImpl, IPCProcess ipcProcess){
		super(ipcProcess, RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.OPERATIONAL_STATUS, null, Calendar.getInstance().getTimeInMillis());
		this.enrollmentTask = enrollmentTaskImpl;
	}

}
