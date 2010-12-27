package rina.ipcservice.api;

/**
 * All the elements needed to name an application process.
 *
 */
public class ApplicationProcessNamingInfo {
	
	private String applicationProcessName = null;
	private int applicationProcessInstance_id = 0;
	private String applicationEntity_id = null;
	private int applicationEntityInstance_id = 0;
	private boolean validFormat = false;
	
	public ApplicationProcessNamingInfo( String applicationProcessName, int applicationProcessInstance_id, String flowAllocator_id, int flowAllocatorInstance_id){
		this.applicationProcessName = applicationProcessName;
		this.applicationProcessInstance_id = applicationProcessInstance_id;
		this.applicationEntity_id = flowAllocator_id;
		this.applicationEntityInstance_id = flowAllocatorInstance_id;
	}
	
	public String getApplicationProcessName() {
		return applicationProcessName;
	}

	public void setApplicationProcessName(String applicationProcessName) {
		this.applicationProcessName = applicationProcessName;
	}

	public int getApplicationProcessInstance_id() {
		return applicationProcessInstance_id;
	}

	public void setApplicationProcessInstance_id(int applicationProcessInstanceId) {
		applicationProcessInstance_id = applicationProcessInstanceId;
	}

	public String getApplicationEntity_id() {
		return applicationEntity_id;
	}

	public void setApplicationEntity_id(String applicationEntityId) {
		applicationEntity_id = applicationEntityId;
	}

	public int getApplicationEntityInstance_id() {
		return applicationEntityInstance_id;
	}

	public void setApplicationEntityInstance_id(int applicationEntityInstanceId) {
		applicationEntityInstance_id = applicationEntityInstanceId;
	}

	public boolean isValidFormat(){
		//TODO: Add the format check
		return validFormat;
		
	}
}