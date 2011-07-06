package rina.ribdaemon.api;

/**
 * Exceptions thrown by the RIB Daemon
 * TODO finish this class
 * @author eduardgrasa
 *
 */
public class RIBDaemonException extends Exception{

	private static final long serialVersionUID = 2358998897959323817L;
	
	/** Error codes **/
	public static final int UNKNOWN_OBJECT_CLASS = 1;
	public static final int MALFORMED_MESSAGE_SUBSCRIPTION_REQUEST = 2;
	public static final int MALFORMED_MESSAGE_UNSUBSCRIPTION_REQUEST = 3;
	public static final int SUBSCRIBER_WAS_NOT_SUBSCRIBED = 4;
	public static final int OBJECTCLASS_AND_OBJECT_NAME_OR_OBJECT_INSTANCE_NOT_SPECIFIED = 5;
	public static final int OBJECTNAME_NOT_PRESENT_IN_THE_RIB = 6;
	public static final int RESPONSE_REQUIRED_BUT_MESSAGE_HANDLER_IS_NULL = 7;
	public static final int PROBLEMS_SENDING_CDAP_MESSAGE = 8;
	
	private int errorCode = 0;
	
	public RIBDaemonException(int errorCode){
		super();
		this.errorCode = errorCode;
	}
	
	public RIBDaemonException(int errorCode, String message){
		super(message);
		this.errorCode = errorCode;
	
	}
	
	public RIBDaemonException(int errorCode, Exception ex){
		super(ex);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
}
