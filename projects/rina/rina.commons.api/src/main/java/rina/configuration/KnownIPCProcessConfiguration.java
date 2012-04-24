package rina.configuration;

/**
 * The configuration of a known IPC Process
 * @author eduardgrasa
 *
 */
public class KnownIPCProcessConfiguration {

	/**
	 * The application process name of the remote IPC Process
	 */
	private String apName = null;
	
	/**
	 * The address of the remote IPC Process
	 */
	private long address = 0;
	
	/**
	 * The hostName or IP address of the interface where the IPC process
	 * is listening
	 */
	private String hostName = null;
	
	/**
	 * The port number where the remote IPC process RMT is listening
	 */
	private int rmtPortNumber = 32769;
	
	/**
	 * The port number where the remote IPC process Flow Allocator 
	 * is listening
	 */
	private int flowAllocatorPortNumber = 32770;
	
	public String getApName() {
		return apName;
	}

	public void setApName(String apName) {
		this.apName = apName;
	}

	public long getAddress() {
		return address;
	}

	public void setAddress(long address) {
		this.address = address;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public int getRmtPortNumber() {
		return rmtPortNumber;
	}

	public void setRmtPortNumber(int rmtPortNumber) {
		this.rmtPortNumber = rmtPortNumber;
	}

	public int getFlowAllocatorPortNumber() {
		return flowAllocatorPortNumber;
	}

	public void setFlowAllocatorPortNumber(int flowAllocatorPortNumber) {
		this.flowAllocatorPortNumber = flowAllocatorPortNumber;
	}
}
