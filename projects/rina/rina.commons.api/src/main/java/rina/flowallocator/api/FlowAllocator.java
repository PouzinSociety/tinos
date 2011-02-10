package rina.flowallocator.api;

import rina.ipcprocess.api.IPCProcessComponent;
import rina.ipcservice.api.APService;
import rina.ipcservice.api.AllocateRequest;

/**
 * This interface must be implemented by the class that implements the flow allocator
 * @author eduardgrasa
 */
public interface FlowAllocator extends IPCProcessComponent{

	/**
	 * Process the new allocate request, submitted by the application process APService
	 * @param allocateRequest the characteristics of the flow to be allocated.
	 * @param applicationProcess the requesting application process
	 * @param portId the port id that will be allocated to this flow
	 */
	public void submitAllocateRequest(AllocateRequest allocateRequest, APService applicationProcess, int portId);
}
