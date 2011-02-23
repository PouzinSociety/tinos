package rina.flowallocator.impl;

import rina.flowallocator.api.FlowAllocatorInstance;
import rina.flowallocator.api.message.Flow;
import rina.flowallocator.impl.policies.NewFlowRequestPolicy;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.AllocateRequest;
import rina.ipcservice.api.IPCException;

/**
 * A flow allocator instance implementation. Its task is to manage the 
 * lifecycle of an EFCP connection
 * @author eduardgrasa
 *
 */
public class FlowAllocatorInstanceImpl implements FlowAllocatorInstance{
	/**
	 * The allocate request that this flow allocator has to try to fulfill
	 */
	private AllocateRequest allocateRequest = null;
	
	/**
	 * A reference to the wrapping IPC Process
	 */
	private IPCProcess ipcProcess = null;
	
	/**
	 * The new flow request policy, will translate the allocate request into 
	 * a flow object
	 */
	private NewFlowRequestPolicy newFlowRequestPolicy = null;
	
	public FlowAllocatorInstanceImpl(IPCProcess ipcProcess){
		this.ipcProcess = ipcProcess;
		//TODO initialize the newFlowRequestPolicy
	}

	/**
	 * Generate the flow object, create the local DTP and optionally DTCP instances, generate a CDAP 
	 * M_CREATE request with the flow object and send it to the appropriate IPC process (search the 
	 * directory and the directory forwarding table if needed)
	 */
	public void submitAllocateRequest(AllocateRequest allocateRequest, int portId) throws IPCException {
		Flow flow = newFlowRequestPolicy.generateFlowObject(allocateRequest);
		
		//TODO create the local instances of DTP and optionally DTCP
		
		//TODO check directory to see to what IPC process the CDAP M_CREATE request has to be delivered
		//TODO if the directory doesn't contain a mapping for the destination application process, 
		//TODO check the directory forwarding table
		
		//TODO once the destination IPC process is known, create the CDAP message
		//TODO Now create the EFCP PDU and handle it to the appropriated EFCP instance
	}

}
