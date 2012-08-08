package rina.resourceallocator.impl.flowmanager;

import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCService;

/**
 * Contains the information about the state of a flow service request
 * @author eduardgrasa
 *
 */
public class FlowServiceState {
	
	public enum Status {NULL, ALLOCATION_REQUESTED, ALLOCATED, DEALLOCATION_REQUESTED};
	
	/**
	 * The parameters of the requested flow service (source app, destination app, 
	 * QoS parameters, portId)
	 */
	private FlowService flowService = null;
	
	private Status status = Status.NULL;
	
	/**
	 * The IPC Process that is handling our request
	 */
	private IPCService ipcService = null;
	
	/**
	 * True if this N-1 flow is used for layer management, 
	 * false otherwise
	 */
	private boolean management = false;

	public FlowService getFlowService() {
		return flowService;
	}

	public void setFlowService(FlowService flowService) {
		this.flowService = flowService;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setIpcService(IPCService ipcService) {
		this.ipcService = ipcService;
	}

	public IPCService getIpcService() {
		return ipcService;
	}
	
	public boolean isManagement() {
		return management;
	}

	public void setManagement(boolean management) {
		this.management = management;
	}

	@Override
	public boolean equals(Object object){
		if (object == null){
			return false;
		}
		
		if (!(object instanceof FlowServiceState)){
			return false;
		}
		
		FlowServiceState candidate = (FlowServiceState) object;
		
		if (candidate.getFlowService() == null){
			return false;
		}
		
		return (candidate.getFlowService().getPortId() == this.getFlowService().getPortId());
	}
	
}
