package rina.applicationprocess.api;

import rina.ribdaemon.api.RIBObjectNames;

/**
 * A synonim for an application process name
 * @author eduardgrasa
 *
 */
public class DAFMember {
	
	public static final String DAF_MEMBER_SET_RIB_OBJECT_NAME = RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + 
		RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + RIBObjectNames.SEPARATOR + RIBObjectNames.ENROLLMENT + 
		RIBObjectNames.SEPARATOR + RIBObjectNames.MEMBERS;
	
	public static final String DAF_MEMBER_SET_RIB_OBJECT_CLASS = "dafmember set";
	
	public static final String DAF_MEMBER_RIB_OBJECT_CLASS = "dafmember";

	/**
	 * The application process name that is synonym refers to
	 */
	private String applicationProcessName = null;
	
	/**
	 * The application process instance that this synonym refers to
	 */
	private String applicationProcessInstance = null;
	
	/**
	 * The synonym
	 */
	private long synonym = 0;

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

	public long getSynonym() {
		return synonym;
	}

	public void setSynonym(long synonym) {
		this.synonym = synonym;
	}
	
	@Override
	public String toString(){
		String result = "Application Process Name: " + this.applicationProcessName + "\n";
		result = result + "Application Process Instance: " + this.applicationProcessInstance + "\n";
		result = result + "Synonym: " + this.synonym;
		
		return result;
	}
}
