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
	
	public ApplicationProcessNamingInfo getRequestedAPinfo() {
		return requestedAPinfo;
	}

	public void setRequestedAPinfo(ApplicationProcessNamingInfo requestedAPinfo) {
		this.requestedAPinfo = requestedAPinfo;
	}

	public int getPort_id() {
		return port_id;
	}

	public void setPort_id(int portId) {
		port_id = portId;
	}

	public QoSCube getCube() {
		return cube;
	}

	public void setCube(QoSCube cube) {
		this.cube = cube;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public boolean isAcceptRequest() {
		return acceptRequest;
	}

	public void setAcceptRequest(boolean acceptRequest) {
		this.acceptRequest = acceptRequest;
	}

	public boolean isValidFormat(){
		//TODO: Add the format check
		return validFormat;
	}

}
