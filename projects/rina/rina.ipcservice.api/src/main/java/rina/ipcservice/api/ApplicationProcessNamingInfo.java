package rina.ipcservice.api;

import java.util.ArrayList;
import java.util.List;

/**
 * All the elements needed to name an application process.
 */
public class ApplicationProcessNamingInfo {
	
	private String applicationProcessName = null;
	private String applicationProcessInstance = null;
	private List<ApplicationEntityNamingInfo> applicationEntities = null;
	
	
	public ApplicationProcessNamingInfo(){
		applicationEntities = new ArrayList<ApplicationEntityNamingInfo>();
	}
	
	public ApplicationProcessNamingInfo(String applicationProcessName, String applicationProcessInstance){
		this();
		this.applicationProcessName = applicationProcessName;
		this.applicationProcessInstance = applicationProcessInstance;
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
	
	public List<ApplicationEntityNamingInfo> getApplicationEntities() {
		return applicationEntities;
	}

	public void setApplicationEntities(List<ApplicationEntityNamingInfo> applicationEntities) {
		this.applicationEntities = applicationEntities;
	}

	public String getProcessKey(){
		return this.applicationProcessName + "-" + this.getApplicationProcessInstance();
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
		
		return true;
	}
	
	@Override
	public String toString(){
		String result = "Application Process Name: " + this.applicationProcessName + "\n";
		result = result + "Application Process Instance: " + this.getApplicationProcessInstance() + "\n";
		result = result + "Application Entities: + \n";
		for(int i=0; i<applicationEntities.size(); i++){
			result = result + "Application Entity name: " + applicationEntities.get(i).getApplicationEntityName() + " ";
			result = result + "Application Entity instance: " + applicationEntities.get(i).getApplicationEntityInstance() + "\n";
		}
		return result;
	}
	
}