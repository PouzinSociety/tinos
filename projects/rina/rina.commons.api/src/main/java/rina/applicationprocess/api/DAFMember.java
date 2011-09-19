package rina.applicationprocess.api;

/**
 * A synonim for an application process name
 * @author eduardgrasa
 *
 */
public class DAFMember {

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
