package rina.ipcservice.api;


public class AllocateRequest {

	private ApplicationProcessNamingInfo requestedAPinfo = null;
	private int port_id = 0;
	private QoSParameters cube = null;
	private boolean result = false;
	
	public AllocateRequest(){
	}

	public AllocateRequest(ApplicationProcessNamingInfo requestedAPinfo, int port_id, QoSParameters cube, boolean result) {
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

	public QoSParameters getCube() {
		return cube;
	}

	public void setCube(QoSParameters cube) {
		this.cube = cube;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}
	
	public String toString(){
		String result = "";
		result = result + "Requested application process naming info: " + this.getRequestedAPinfo() + "\n";
		return result;
	}


}
