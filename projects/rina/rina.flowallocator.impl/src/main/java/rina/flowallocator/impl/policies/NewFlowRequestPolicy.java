package rina.flowallocator.impl.policies;

import rina.flowallocator.api.message.Flow;
import rina.ipcservice.api.AllocateRequest;
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
	 * @param allocateRequest
	 * @return the current return type is just a placeholder, a Flow object must be returned
	 * @throws IPCException if the request cannot be satisfied
	 */
	public Flow generateFlowObject(AllocateRequest allocateRequest) throws IPCException; 
}
