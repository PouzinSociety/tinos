package rina.ipcservice.api;


public class AllocateRequest {

	private ApplicationProcessNamingInfo requestedAPinfo = null;
	private int port_id = 0;
	private QoSCube cube = null;
	private boolean result = false;

	private boolean validFormat = false;
	private boolean acceptRequest = false;



	public AllocateRequest(ApplicationProcessNamingInfo requestedAPinfo, int port_id, QoSCube cube, boolean result) {
		this.requestedAPinfo = requestedAPinfo;
		this.port_id = port_id;
		this.cube = cube;
		this.result = result;
	}
	
	public boolean isValidFormat(){
		//TODO: Add the format check
		return validFormat;
	}
	
	public boolean translateRequestinPolicies(){
		//TODO: invoke NewFlowRequestPolicy and get the result
		return acceptRequest;
	}

}
