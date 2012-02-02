package rina.applibrary.api;

/**
 * Classes implementing this interface will be notified when a new flow is accepted
 * @author eduardgrasa
 *
 */
public interface FlowListener {

	/**
	 * Called when a new flow is accepted. This operation is responsible for
	 * attaching an SDU Listener to the flow, if not the information sent to 
	 * this application will be lost
	 * @param flow
	 */
	public void flowAccepted(Flow flow);
}
