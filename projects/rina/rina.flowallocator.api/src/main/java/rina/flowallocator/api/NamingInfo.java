package rina.flowallocator.api;

/**
 * Captures an application process and/or entity naming information
 * @author eduardgrasa
 *
 */
public class NamingInfo {
	private String applicationProcessName = null;
	private String applicationProcessInstanceId = null;
	private String applicationEntityName = null;
	private String applicationEntityInstanceId = null;
	
	public NamingInfo(String applicationProcessName){
		this.applicationProcessName = applicationProcessName;
	}
	
	public String getApplicationProcessName() {
		return applicationProcessName;
	}
	public void setApplicationProcessName(String applicationProcessName) {
		this.applicationProcessName = applicationProcessName;
	}
	public String getApplicationProcessInstanceId() {
		return applicationProcessInstanceId;
	}
	public void setApplicationProcessInstanceId(String applicationProcessInstanceId) {
		this.applicationProcessInstanceId = applicationProcessInstanceId;
	}
	public String getApplicationEntityName() {
		return applicationEntityName;
	}
	public void setApplicationEntityName(String applicationEntityName) {
		this.applicationEntityName = applicationEntityName;
	}
	public String getApplicationEntityInstanceId() {
		return applicationEntityInstanceId;
	}
	public void setApplicationEntityInstanceId(String applicationEntityInstanceId) {
		this.applicationEntityInstanceId = applicationEntityInstanceId;
	}
}
