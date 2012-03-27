package rina.configuration;

/**
 * Configuration of the local RINA Software instantiation
 * @author eduardgrasa
 *
 */
public class LocalConfiguration {

	/**
	 * The port where the IPC Manager is listening for incoming local TCP connections from administrators
	 */
	private int consolePort = 32766;
	
	/**
	 * The port where the Flow Allocator is listening for incoming TCP connections from remote Flow Allocators
	 */
	private int flowAllocatorPort = 32770;
	
	/**
	 * The port where the RMT is listening for incoming TCP connections from remote RMTs
	 */
	private int rmtPort = 32769;
	
	/**
	 * The maximum time the CDAP state machine of a session will wait for connect or release responses (in ms)
	 */
	private int cdapTimeoutInMs = 10000;
	
	/**
	 * The maximum time to wait between steps of the enrollment sequence (in ms)
	 */
	private int enrollmentTimeoutInMs = 10000;

	public int getConsolePort() {
		return consolePort;
	}

	public void setConsolePort(int consolePort) {
		this.consolePort = consolePort;
	}

	public int getFlowAllocatorPort() {
		return flowAllocatorPort;
	}

	public void setFlowAllocatorPort(int flowAllocatorPort) {
		this.flowAllocatorPort = flowAllocatorPort;
	}

	public int getRmtPort() {
		return rmtPort;
	}

	public void setRmtPort(int rmtPort) {
		this.rmtPort = rmtPort;
	}

	public int getCdapTimeoutInMs() {
		return cdapTimeoutInMs;
	}

	public void setCdapTimeoutInMs(int cdapTimeoutInMs) {
		this.cdapTimeoutInMs = cdapTimeoutInMs;
	}

	public int getEnrollmentTimeoutInMs() {
		return enrollmentTimeoutInMs;
	}

	public void setEnrollmentTimeoutInMs(int enrollmentTimeoutInMs) {
		this.enrollmentTimeoutInMs = enrollmentTimeoutInMs;
	}
}
