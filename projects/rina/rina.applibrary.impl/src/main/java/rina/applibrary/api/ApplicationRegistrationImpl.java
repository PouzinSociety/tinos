package rina.applibrary.api;

import java.util.List;

import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.IPCException;

/**
 * Classes implementing this interface provide the behavior of the ApplicationRegistration class
 * @author eduardgrasa
 *
 */
public interface ApplicationRegistrationImpl {

	public enum State {REGISTERED, UNREGISTERED};
	
	/**
	 * Registers the application to the specified list of DIFs. If the list is null, the application is 
	 * registered in all the DIFs that currently exist in this system.
	 * @param applicationProcess The naming information of the application process that is registering
	 * @param difNames The list of difNames to which the application process is registering. If the list is null it will 
	 * register to all the DIFs available in the system
	 * @param flowAcceptor Decides what flows will be accepted and what flows will be rejected. If it is null, all the 
	 * incoming flows are accepted
	 * @param flowListener If provided, every time a new flow is accepted the flowListener will be called (non-blocking
	 * mode). In non-blocking mode calls to "accept" will throw an Exception. If it is null, users of this class will have 
	 * to call the "accept" blocking operation in order to get the accepted flows.
	 * @throws IPCException
	 */
	public void register(ApplicationProcessNamingInfo applicationProcess, List<String> difNames, FlowAcceptor flowAcceptor, FlowListener flowListener) throws IPCException;
	
	/**
	 * This operation will block until a new incoming flow is accepted.
	 * @return the accepted Flow
	 */
	public FlowImpl accept() throws IPCException;
	
	/**
	 * Cancel the registration
	 * @throws IPCException
	 */
	public void unregister() throws IPCException;
	
	/**
	 * Returns the registration state
	 * @return
	 */
	public State getState();
	
	/**
	 * Sets the registration state
	 * @param current state
	 * @param state
	 */
	public void setState(State state);
}
