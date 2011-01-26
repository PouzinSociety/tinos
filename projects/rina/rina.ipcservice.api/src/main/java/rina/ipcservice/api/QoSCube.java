package rina.ipcservice.api;

import java.util.*;

/**
 * A QoS cube
 *
 */
public class QoSCube {

	private Map<String, Object> cube = null;
	
	
	public QoSCube() {
		cube = new HashMap<String, Object>();
	}


	public Map<String, Object> getCube() {
		return cube;
	}

	public void setCube(Map<String, Object> cube) {
		this.cube = cube;
	}	
		
}
