package rina.applibrary.api;

/**
 * Classes implementing this interface are called every time an external application 
 * attempts to establish a flow to this application. FlowAcceptors have to decide wether 
 * they accept the new flow request or not.
 * @author eduardgrasa
 *
 */
public interface FlowAcceptor {
	
	/**
	 * Decides if the flow request by sourceApplication to destinationApplication is accepted. 
	 * @param sourceApplication
	 * @param destinationApplication
	 * @return null if the flow is accepted, a string explaining the reasons 
	 * why the flow is rejected in case it is rejected
	 */
	public String acceptFlow(ApplicationProcess sourceApplication, ApplicationProcess destinationApplication);

}
