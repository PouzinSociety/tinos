package rina.ipcservice.api;

public class FlowService {

	public static final String OBJECT_NAME="/flowservice";
	public static final String OBJECT_CLASS="flow service object";
	
	private ApplicationProcessNamingInfo sourceAPNamingInfo = null;
	private ApplicationProcessNamingInfo destinationAPNamingInfo = null;
	private int portId = 0;
	private QoSParameters qosParameters = null;
	private boolean result = false;
	
	public FlowService(){
	}

	public FlowService(ApplicationProcessNamingInfo sourceAPNamingInfo, ApplicationProcessNamingInfo destinationAPNamingInfo, QoSParameters qosParameters) {
		this.sourceAPNamingInfo = sourceAPNamingInfo;
		this.destinationAPNamingInfo = destinationAPNamingInfo;
		this.qosParameters = qosParameters;
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
		return qosParameters;
	}

	public void setQoSParameters(QoSParameters qosParameters) {
		this.qosParameters = qosParameters;
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
