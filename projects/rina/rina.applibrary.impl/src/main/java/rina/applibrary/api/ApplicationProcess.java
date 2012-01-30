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
}
