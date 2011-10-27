package rina.ipcservice.api;

import java.util.*;

/**
 * A QoS cube
 *
 */
public class QoSParameters {

	private Map<String, Object> cube = null;
	
	
	public QoSParameters() {
		cube = new HashMap<String, Object>();
	}


	public Map<String, Object> getCube() {
		return cube;
	}

	public void setCube(Map<String, Object> cube) {
		this.cube = cube;
	}	
		
}
