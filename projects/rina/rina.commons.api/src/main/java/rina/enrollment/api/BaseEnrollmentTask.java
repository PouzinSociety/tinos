package rina.enrollment.api;

import rina.ipcprocess.api.BaseIPCProcessComponent;

/**
 * Provides the component name for the Enrollment Task
 * @author eduardgrasa
 *
 */
public abstract class BaseEnrollmentTask extends BaseIPCProcessComponent implements EnrollmentTask{
	
	public static final String ENROLLMENT_TASK_TIMEOUT_PROPERTY = "rina.enrollment.timeout";
	public static final long DEFAULT_TIMEOUT = 5000;
	
	public static final String getComponentName(){
		return EnrollmentTask.class.getName();
	}

	public String getName(){
		return BaseEnrollmentTask.getComponentName();
	}
}
