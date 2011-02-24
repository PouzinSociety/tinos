package rina.flowallocator.api.message;

import rina.ipcservice.api.ApplicationProcessNamingInfo;

/**
 * An entry in the directory
 * @author eduardgrasa
 *
 */
public class DirectoryEntry {
	
	/**
	 * The age when this directory entry will expire
	 */
	private long age = 0;
	
	/**
	 * The address of the IPC process from where the applicationProcesName can be reached, or of the 
	 * IPC Process that may know where the application process is
	 */
	private byte[] address = null;
	
	/**
	 * The naming information of the application (at least it will contain the application process name)
	 */
	private ApplicationProcessNamingInfo apNamingInfo = null;

	public long getAge() {
		return age;
	}

	public void setAge(long age) {
		this.age = age;
	}

	public byte[] getAddress() {
		return address;
	}

	public void setAddress(byte[] address) {
		this.address = address;
	}

	public ApplicationProcessNamingInfo getApNamingInfo() {
		return apNamingInfo;
	}

	public void setApNamingInfo(ApplicationProcessNamingInfo apNamingInfo) {
		this.apNamingInfo = apNamingInfo;
	}
}