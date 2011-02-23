package rina.flowallocator.api;

import rina.ipcservice.api.AllocateRequest;
import rina.ipcservice.api.IPCException;

/**
 * The interface between the FA and the FAI
 * @author elenitrouva
 *
 */
public interface FlowAllocatorInstance{

	/**
	 * Called by the FA to forward an Allocate request to a FAI
	 * @param request
	 */
	public void submitAllocateRequest(AllocateRequest request, int portId) throws IPCException;

}