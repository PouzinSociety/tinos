package rina.configuration;

import java.util.List;

import rina.enrollment.api.Neighbor;

/**
 * Contains enough information to create an IPC Process
 * @author eduardgrasa
 *
 */
public class IPCProcessToCreate {
	
	private String applicationProcessName = null;
	private String applicationProcessInstance = null;
	private String difName = null;
	private List<Neighbor> neighbors = null;
	
	public String getApplicationProcessName() {
		return applicationProcessName;
	}
	public void setApplicationProcessName(String applicationProcessName) {
		this.applicationProcessName = applicationProcessName;
	}
	public String getApplicationProcessInstance() {
		return applicationProcessInstance;
	}
	public void setApplicationProcessInstance(String applicationProcessInstance) {
		this.applicationProcessInstance = applicationProcessInstance;
	}
	public String getDifName() {
		return difName;
	}
	public void setDifName(String difName) {
		this.difName = difName;
	}
	public List<Neighbor> getNeighbors() {
		return neighbors;
	}
	public void setNeighbors(List<Neighbor> neighbors) {
		this.neighbors = neighbors;
	}
}
