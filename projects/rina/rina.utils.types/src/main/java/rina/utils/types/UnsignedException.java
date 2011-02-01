package rina.utils.types;

/**
 * Exception for
 * @author eduardgrasa
 *
 */
public class UnsignedException extends RuntimeException{
	
	private static final long serialVersionUID = -4802623184750614587L;

	public enum ErrorCodes {UnsupportedNumberOfBytes};
	
	private ErrorCodes errorCode = null;
	
	public UnsignedException(ErrorCodes errorCode){
		this.errorCode = errorCode;
	}
	
	public ErrorCodes getErrorCode(){
		return errorCode;
	}

}
