package rina.ipcservice.api;

import java.util.*;

/**
 * A group of QoS parameters
 *
 */
public class QoSParameters {

	private Map<String, Object> parameters = null;
	
	
	public QoSParameters() {
		parameters = new HashMap<String, Object>();
	}


	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}	
		
}
