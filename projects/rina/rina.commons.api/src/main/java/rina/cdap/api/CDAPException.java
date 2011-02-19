package rina.cdap.api;

public class CDAPException extends Exception{
	private static final long serialVersionUID = 1L;

	/**
	 * Name of the operation that failed
	 */
	private String operation;
	
	/**
	 * Operation result code
	 */
	private String result;
	
	/**
	 * Result reason
	 */
	private String resultReason;
	
	public CDAPException(Exception ex){
		super(ex);
	}
	
	public CDAPException(String operation, String result, String resultReason){
		super(resultReason);
		this.operation = operation;
		this.result = result;
		this.resultReason = resultReason;
	}
	
	public CDAPException(String result, String resultReason){
		this (null, result, resultReason);
	}
	
	public CDAPException(String resultReason){
		this(null, null, resultReason);
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getResultReason() {
		return resultReason;
	}

	public void setResultReason(String resultReason) {
		this.resultReason = resultReason;
	}
}
