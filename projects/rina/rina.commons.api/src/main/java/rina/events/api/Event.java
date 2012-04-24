package rina.events.api;

/**
 * An event
 * @author eduardgrasa
 *
 */
public interface Event {
	
	public static final String N_MINUS_1_FLOW_DEALLOCATED = "N minus 1 Flow Deallocated";
	public static final String CONNECTIVITY_TO_NEIGHBOR_LOST = "Connectivity to Neighbor Lost";

	/**
	 * The id of the event
	 * @return
	 */
	public String getId();
}
