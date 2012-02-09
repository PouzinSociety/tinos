package rina.ipcmanager.impl.apservice;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import rina.ipcservice.api.ApplicationRegistration;

/**
 * Contains information about a certain application registration state
 * @author eduardgrasa
 *
 */
public class ApplicationRegistrationState {
	
	/**
	 * The application registration information
	 */
	private ApplicationRegistration applicationRegistration = null;
	
	/**
	 * The state of the flows that have been established to the registered 
	 * application
	 */
	private List<FlowServiceState> flowServices = null;
	
	/**
	 * The socket used to manage with the application registration
	 */
	private Socket socket = null;
	
	public ApplicationRegistrationState(ApplicationRegistration applicationRegistration){
		this.setApplicationRegistration(applicationRegistration);
		setFlowServices(new ArrayList<FlowServiceState>());
	}

	private void setFlowServices(List<FlowServiceState> flowServices) {
		this.flowServices = flowServices;
	}

	public List<FlowServiceState> getFlowServices() {
		return flowServices;
	}

	private void setApplicationRegistration(ApplicationRegistration applicationRegistration) {
		this.applicationRegistration = applicationRegistration;
	}

	public ApplicationRegistration getApplicationRegistration() {
		return applicationRegistration;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public Socket getSocket() {
		return socket;
	}
}
