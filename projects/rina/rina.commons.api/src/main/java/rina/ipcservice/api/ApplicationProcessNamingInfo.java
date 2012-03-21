package rina.ipcservice.api;

import rina.ribdaemon.api.RIBObjectNames;

/**
 * All the elements needed to name an application process.
 */
public class ApplicationProcessNamingInfo {
	
	public static final String APPLICATION_PROCESS_NAMING_INFO_RIB_OBJECT_NAME = RIBObjectNames.SEPARATOR + RIBObjectNames.DAF + 
		RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + 
		RIBObjectNames.SEPARATOR + RIBObjectNames.APNAME;

	public static final String APPLICATION_PROCESS_NAMING_INFO_RIB_OBJECT_CLASS = "apnaminginfo";
	
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
		String key = this.applicationProcessName + ".";
		
		if (this.applicationProcessInstance != null){
			key = key + this.applicationProcessInstance;
		}else{
			key = key + "*";
		}
		
		key = key + ".";
		
		if (this.applicationEntityName != null){
			key = key + this.applicationEntityName;
		}else{
			key = key + "*";
		}
		
		key = key + ".";
		
		if (this.applicationEntityInstance != null){
			key = key + this.applicationEntityInstance;
		}else{
			key = key + "*";
		}
		
		return key;
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
		
		return this.getProcessKey().equals(candidate.getProcessKey());
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