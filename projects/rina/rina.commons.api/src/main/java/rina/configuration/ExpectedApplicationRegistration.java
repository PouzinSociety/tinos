package rina.configuration;

public class ExpectedApplicationRegistration {
	
	private String applicationProcessName = null;
	private String applicationProcessInstance = null;
	private int socketPortNumber = -1;
	
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
	public int getSocketPortNumber() {
		return socketPortNumber;
	}
	public void setSocketPortNumber(int socketPortNumber) {
		this.socketPortNumber = socketPortNumber;
	}
	
}
