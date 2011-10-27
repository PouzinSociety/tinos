package rina.ipcservice.api;

public class AllocateRequest {

	private ApplicationProcessNamingInfo sourceAPNamingInfo = null;
	private ApplicationProcessNamingInfo destinationAPNamingInfo = null;
	private int portId = 0;
	private QoSParameters cube = null;
	private boolean result = false;
	
	public AllocateRequest(){
	}

	public AllocateRequest(ApplicationProcessNamingInfo sourceAPNamingInfo, ApplicationProcessNamingInfo destinationAPNamingInfo, QoSParameters cube) {
		this.sourceAPNamingInfo = sourceAPNamingInfo;
		this.destinationAPNamingInfo = destinationAPNamingInfo;
		this.cube = cube;
	}
	
	public ApplicationProcessNamingInfo getSourceAPNamingInfo() {
		return sourceAPNamingInfo;
	}

	public void setSourceAPNamingInfo(ApplicationProcessNamingInfo sourceAPNamingInfo) {
		this.sourceAPNamingInfo = sourceAPNamingInfo;
	}

	public ApplicationProcessNamingInfo getDestinationAPNamingInfo() {
		return destinationAPNamingInfo;
	}

	public void setDestinationAPNamingInfo(ApplicationProcessNamingInfo destinationAPNamingInfo) {
		this.destinationAPNamingInfo = destinationAPNamingInfo;
	}

	public int getPortId() {
		return portId;
	}

	public void setPortId(int portId) {
		this.portId = portId;
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
		result = result + "Source application process naming info: " + this.getSourceAPNamingInfo() + "\n";
		result = result + "Destination application process naming info: " + this.getDestinationAPNamingInfo() + "\n";
		return result;
	}


}
