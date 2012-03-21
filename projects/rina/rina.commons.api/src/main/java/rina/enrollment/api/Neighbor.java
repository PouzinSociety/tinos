package rina.enrollment.api;

import rina.ribdaemon.api.RIBObjectNames;

/**
 * A synonim for an application process name
 * @author eduardgrasa
 *
 */
public class Neighbor {
	
	public static final String NEIGHBOR_SET_RIB_OBJECT_NAME = RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + 
		RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + RIBObjectNames.SEPARATOR + RIBObjectNames.NEIGHBORS;
	
	public static final String NEIGHBOR_SET_RIB_OBJECT_CLASS = "neighbor set";
	
	public static final String NEIGHBOR_RIB_OBJECT_CLASS = "neighbor";

	/**
	 * The application process name that is synonym refers to
	 */
	private String applicationProcessName = null;
	
	/**
	 * The application process instance that this synonym refers to
	 */
	private String applicationProcessInstance = null;
	
	/**
	 * The address
	 */
	private long address = 0;

	public String getApplicationProcessName() {
		return applicationProcessName;
	}

	public void setApplicationProcessName(String applicationProcessName) {
		this.applicationProcessName = applicationProcessName;
	}

	public String getApplicationProcessInstance() {
		return applicationProcessInstance;
	}

	public void setApplicationProcessInstance(String applicationProcessInstance) {
		this.applicationProcessInstance = applicationProcessInstance;
	}

	public long getAddress() {
		return address;
	}

	public void setAddress(long address) {
		this.address = address;
	}
	
	@Override
	public String toString(){
		String result = "Application Process Name: " + this.applicationProcessName + "\n";
		result = result + "Application Process Instance: " + this.applicationProcessInstance + "\n";
		result = result + "Address: " + this.address;
		
		return result;
	}
}
