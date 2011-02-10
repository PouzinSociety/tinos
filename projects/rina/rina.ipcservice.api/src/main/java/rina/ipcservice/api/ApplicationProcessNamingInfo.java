package rina.ipcservice.api;

/**
 * All the elements needed to name an application process.
 */
public class ApplicationProcessNamingInfo {
	
	private String applicationProcessName = null;
	private String applicationProcessInstance = null;
	private String applicationEntityName = null;
	private String applicationEntityInstance = null;
	
	
	public ApplicationProcessNamingInfo(String applicationProcessName, String applicationProcessInstance, String applicationEntityName, String applicationEntityInstance){
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
	public boolean equals(Object candidate){
		if (candidate == null){
			return false;
		}
		
		if (!(candidate instanceof ApplicationProcessNamingInfo)){
			return false;
		}
		
		ApplicationProcessNamingInfo namingInfo = (ApplicationProcessNamingInfo) candidate;
		
		if (!(this.getApplicationProcessName().equals(namingInfo.getApplicationProcessName()))){
			return false;
		}
		
		if (this.getApplicationProcessInstance() != null){
			if (!(this.getApplicationProcessInstance().equals(namingInfo.getApplicationProcessInstance()))){
				return false;
			}
		}
		
		if (this.getApplicationEntityName() != null){
			if (!(this.getApplicationEntityName().equals(namingInfo.getApplicationEntityName()))){
				return false;
			}
		}
		
		if (this.getApplicationEntityInstance() != null){
			if (!(this.getApplicationEntityInstance().equals(namingInfo.getApplicationEntityInstance()))){
				return false;
			}
		}
		
		return true;
	}
	
}