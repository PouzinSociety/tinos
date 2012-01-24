package rina.ipcmanager.impl.apservice;

import java.net.Socket;

import rina.ipcservice.api.FlowService;

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
	
	/**
	 * The socket to communicate with the application that is using the flow service
	 */
	private Socket socket = null;
	
	private Status status = Status.NULL;

	public FlowService getFlowService() {
		return flowService;
	}

	public void setFlowService(FlowService flowService) {
		this.flowService = flowService;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
}
