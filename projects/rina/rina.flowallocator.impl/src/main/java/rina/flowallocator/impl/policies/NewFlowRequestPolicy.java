package rina.flowallocator.impl.policies;

import rina.flowallocator.api.Flow;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCException;

/**
 * This policy is used to convert an Allocate Request is into a create_flow request.  
 * Its primary task is to translate the request into the proper QoS-class-set, flow set, 
 * and access control capabilities. 
 * @author eduardgrasa
 *
 */
public interface NewFlowRequestPolicy {
	
	/**
	 * Converts an allocate request into a Flow object
	 * @param allocateRequest the allocate request
	 * @param portId the local port id associated to this flow
	 * @return flow the object with all the required data to create a connection that supports this flow
	 * @throws IPCException if the request cannot be satisfied
	 */
	public Flow generateFlowObject(FlowService flowService) throws IPCException; 
}
