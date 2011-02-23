package rina.ipcservice.api;

/**
 * Root class of exceptions
 * @author eduardgrasa
 *
 */
public class IPCException extends Exception{

	private static final long serialVersionUID = -4765335465449082765L;
	
	private int errorCode = 0;

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
}
