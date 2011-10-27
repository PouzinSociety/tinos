package rina.ipcservice.api;

/**
 * Root class of exceptions
 * @author eduardgrasa
 *
 */
public class IPCException extends Exception{

	private static final long serialVersionUID = -4765335465449082765L;
	
	/** Error codes **/
	public static final int MALFORMED_ALLOCATE_REQUEST = 1;
	public static final int PORTID_NOT_IN_ALLOCATION_PENDING_STATE = 2;
	public static final int PORTID_NOT_IN_TRANSFER_STATE = 3;
	public static final int PORTID_NOT_IN_DEALLOCATION_PENDING_STATE = 4;
	
	private int errorCode = 0;
	
	public IPCException(int errorCode){
		super();
		this.errorCode = errorCode;
	}
	
	public IPCException(int errorCode, String message){
		super(message);
		this.errorCode = errorCode;
	
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
}
