package rina.applibrary.api;

/**
 *
 * Classes implementing FlowImpl perform the actual job of 
 * implementing the operations provided by the Flow class
 * @author eduardgrasa
 *
 */
public interface FlowImpl {
	
	/**
	 * Will try to allocate a flow to the destination application process with the level of 
	 * service specified by qosSpec. SDUs received from the flow will be passed to the 
	 * sduListener class.
	 * @param sourceApplication
	 * @param destinationAppliation
	 * @param qosSpec
	 * @param sduListener
	 * @throws FlowException if something goes wrong. The explanation of what went wrong and why 
	 * will be in the "message" and "errorCode" fields of the class.
	 */
	public void allocate(ApplicationProcess sourceApplication, ApplicationProcess destinationApplication, 
			QualityOfServiceSpecification qosSpec, SDUListener sduListener) throws IPCException;
	
	/**
	 * Sends an SDU to the flow
	 * @param sdu
	 */
	public void write(byte[] sdu) throws IPCException;
	
	/**
	 * Causes the flow to be terminated. All the resources associated to it will be deallocated 
	 * @throws IPCException
	 */
	public void deallocate() throws IPCException;
}
