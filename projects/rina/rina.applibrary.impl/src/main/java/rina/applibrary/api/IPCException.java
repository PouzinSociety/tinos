package rina.applibrary.api;

/**
 * An exception thrown 
 * @author eduardgrasa
 *
 */
public class IPCException extends Exception{
	
	public static final int SOURCE_APPLICATION_NOT_SPECIFIED_CODE = 1;
	public static final int DESTINATION_APPLICATION_NOT_SPECIFIED_CODE = 2;
	public static final int SDU_LISTENER_NOT_SPECIFIED_CODE = 3;
	public static final int APPLICATION_PROCESS_NAME_NOT_SPECIFIED_CODE = 4;
	public static final int ERROR_ALLOCATING_THE_FLOW_CODE = 5;
	
	public static final String SOURCE_APPLICATION_NOT_SPECIFIED = "Source application name not specified";
	public static final String DESTINATION_APPLICATION_NOT_SPECIFIED = "Destination application name not specified";
	public static final String SDU_LISTENER_NOT_SPECIFIED = "SDU Listener not specified";
	public static final String APPLICATION_PROCESS_NAME_NOT_SPECIFIED = "Application process name not specified";
	public static final String ERROR_ALLOCATING_THE_FLOW_= "Error allocating the flow";

	/**
	 * 
	 */
	private static final long serialVersionUID = -7158207374258170051L;
	
	private int errorCode = 0;
	
	public IPCException(){
		super();
	}
	
	public IPCException(String message){
		super(message);
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}
}
