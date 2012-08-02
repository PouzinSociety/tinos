package rina.resourceallocator.api;

import java.util.List;

import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCException;

/**
 * Manages the allocation and lifetime of N-1 Flows for 
 * an IPC Process 
 * @author eduardgrasa
 *
 */
public interface NMinus1FlowManager {

	/**
	 * Allocate an N-1 Flow with the requested QoS to the destination 
	 * IPC Process 
	 * @param flowService contains the destination IPC Process and requested QoS information
	 */
	public void allocateNMinus1Flow(FlowService flowService);
	
	/**
	 * Deallocate the N-1 Flow identified by portId
	 * @param portId
	 * @throws IPCException if no N-1 Flow identified by portId exists
	 */
	public void deallocateNMinus1Flow(int portId) throws IPCException;
	
	/**
	 * Register the IPC Process to one or more N-1 DIFs
	 * @param difNames The list of N-1 DIFs where this IPC Process will register
	 * @throws IPCException
	 */
	public void registerIPCProcess(List<String> difNames) throws IPCException;
}
