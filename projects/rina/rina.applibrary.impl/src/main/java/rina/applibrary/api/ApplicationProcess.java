package rina.applibrary.api;

/**
 * A container class for the naming information of the application process
 * @author eduardgrasa
 *
 */
public class ApplicationProcess {
	
	private String applicationProcessName = null;
	
	private String applicationProcessInstance = null;
	
	private String applicationEntityName = null;
	
	private String applicationEntityInstance = null;

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
	
	public void validate() throws IPCException{
		if (applicationProcessName == null){
			IPCException flowException = new IPCException(IPCException.APPLICATION_PROCESS_NAME_NOT_SPECIFIED);
			flowException.setErrorCode(IPCException.APPLICATION_PROCESS_NAME_NOT_SPECIFIED_CODE);
		}
	}
	
	public boolean equals(Object object){
		if (object == null){
			return false;
		}
		
		if (!(object instanceof ApplicationProcess)){
			return false;
		}
		
		ApplicationProcess candidate = (ApplicationProcess) object;
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
	
	public String toString(){
		String result = "";
		result = result + "AP Name: " + this.applicationProcessName;
		if (this.applicationProcessInstance != null){
			result = result + "\nAP Instance: " + this.getApplicationProcessInstance();
		}
		if (this.applicationEntityName != null){
			result = result + "\nAE Name: " + this.getApplicationEntityName();
		}
		if (this.applicationEntityInstance != null){
			result = result + "\nAE Instance: " + this.getApplicationEntityInstance();
		}
		
		return result;
	}
}
