package rina.ipcservice.impl.test;

import rina.enrollment.api.EnrollmentTask;
import rina.enrollment.api.EnrollmentTaskFactory;
import rina.ipcservice.api.ApplicationProcessNamingInfo;

public class MockEnrollmentTaskFactory implements EnrollmentTaskFactory {
	
	private EnrollmentTask mockEnrollmentTask = null;

	public EnrollmentTask createEnrollmentTask(ApplicationProcessNamingInfo arg0) {
		mockEnrollmentTask = new MockEnrollmentTask();
		return mockEnrollmentTask;
	}

	public void destroyEnrollmentTask(ApplicationProcessNamingInfo arg0) {
		mockEnrollmentTask = null;
	}

	public EnrollmentTask getEnrollmentTask(ApplicationProcessNamingInfo arg0) {
		return mockEnrollmentTask;
	}

}
