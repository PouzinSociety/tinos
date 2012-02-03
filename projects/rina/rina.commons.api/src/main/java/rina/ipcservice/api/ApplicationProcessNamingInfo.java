package rina.ipcservice.api;

/**
 * All the elements needed to name an application process.
 */
public class ApplicationProcessNamingInfo {
	
	private String applicationProcessName = null;
	private String applicationProcessInstance = null;
	private String applicationEntityName = null;
	private String applicationEntityInstance = null;
	
	public ApplicationProcessNamingInfo(){
	}
	
	public ApplicationProcessNamingInfo(String applicationProcessName, String applicationProcessInstance){
		this();
		this.applicationProcessName = applicationProcessName;
		this.applicationProcessInstance = applicationProcessInstance;
	}
	
	public ApplicationProcessNamingInfo(String applicationProcessName, String applicationProcessInstance, String applicationEntityName, String applicationEntityInstance){
		this();
		this.applicationProcessName = applicationProcessName;
		this.applicationProcessInstance = applicationProcessInstance;
		this.applicationEntityName = applicationEntityName;
		this.applicationEntityInstance = applicationEntityInstance;
	}
	
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

	public String getProcessKey(){
		return this.applicationProcessName + "-" + this.getApplicationProcessInstance() + "-" 
		+ this.applicationEntityName + "-" + this.applicationEntityInstance;
	}
	
	public String getApplicationEntityName() {
		return applicationEntityName;
	}

	public void setApplicationEntityName(String applicationEntityName) {
		this.applicationEntityName = applicationEntityName;
	}

	public String getApplicationEntityInstance() {
		return applicationEntityInstance;
	}

	public void setApplicationEntityInstance(String applicationEntityInstance) {
		this.applicationEntityInstance = applicationEntityInstance;
	}

	@Override
	public boolean equals(Object object){
		if (object == null){
			return false;
		}
		
		if (!(object instanceof ApplicationProcessNamingInfo)){
			return false;
		}
		
		ApplicationProcessNamingInfo candidate = (ApplicationProcessNamingInfo) object;
		
		if (!this.applicationProcessName.equals(candidate.getApplicationProcessName())){
			return false;
		}
		
		if (this.applicationProcessInstance != null && !this.applicationProcessInstance.equals(candidate.getApplicationProcessInstance())){
			return false;
		}
		
		if (this.applicationEntityName != null && !this.applicationEntityName.equals(candidate.getApplicationEntityName())){
			return false;
		}
		
		if (this.applicationEntityInstance != null && !this.applicationEntityInstance.equals(candidate.getApplicationEntityInstance())){
			return false;
		}
		
		return true;
	}
	
	@Override
	public String toString(){
		String result = "Application Process Name: " + this.applicationProcessName + "\n";
		result = result + "Application Process Instance: " + this.getApplicationProcessInstance() + "\n";
		result = result + "Application Entity name: " + this.getApplicationEntityName() + " ";
		result = result + "Application Entity instance: " + this.getApplicationEntityInstance() + "\n";
		
		return result;
	}
	
}