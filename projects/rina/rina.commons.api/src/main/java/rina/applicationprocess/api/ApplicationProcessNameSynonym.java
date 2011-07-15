package rina.applicationprocess.api;

/**
 * A synonim for an application process name
 * @author eduardgrasa
 *
 */
public class ApplicationProcessNameSynonym {

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
	private byte[] synonym = null;

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

	public byte[] getSynonym() {
		return synonym;
	}

	public void setSynonym(byte[] synonym) {
		this.synonym = synonym;
	}
}
