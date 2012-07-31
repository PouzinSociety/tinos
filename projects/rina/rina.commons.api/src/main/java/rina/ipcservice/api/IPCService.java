package rina.ipcservice.api;

import java.util.concurrent.BlockingQueue;

import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.aux.BlockingQueueSet;

/** 
 * Implements the IPC API for the part of the requested application
 */

public interface IPCService {
	
	public enum FlowState {BLOCKED, AVAILABLE};
	
	/**
	 * This primitive is invoked by an Application Process to request the allocation of 
	 * IPC resources with the destination application.
	 * @param request the characteristics of the requested flow
	 * @param application the callback to invoke the application for allocateResponse and any other calls
	 * @return int the portId for the flow allocation
	 * @throw IPCException if the allocate request is not well formed or there are 
	 * no resources to honour the request
	 */
	public int submitAllocateRequest(FlowService request, APService application) throws IPCException;
	
	/**
	 * This primitive is invoked by the Application Process in any state to deallocate the 
	 * resources allocated to this instance. 
	 * @param port_id
	 * @throws IPCException
	 */
	public void submitDeallocate(int portId) throws IPCException;
	
	/**
	 * Write an SDU to the flow identified by portId. This 
	 * operation will block if the flow is not available for transmitting
	 * (because the queue is full).
	 * @param portId
	 * @param sdu
	 * @throws IPCException if the flow doesn't exist
	 */
	public void submitTransfer(int portId, byte[] sdu) throws IPCException;
	
	/**
	 * Returns true if an SDU can be posted to the Flow identified by portId, 
	 * false if the flow is blocked (because the queue is full).
	 * @param portId
	 * @return
	 * @throws IPCException if no flow identified by portId exists
	 */
	public boolean isFlowAvailableForTransfer(int portId) throws IPCException;
	
	/**
	 * Returns the incoming flow queue related to the portId
	 * @param portId
	 * @return
	 * @throws IPCException
	 */
	public BlockingQueue<byte[]> getIncomingFlowQueue(int portId) throws IPCException;
	
	/**
	 * Returns the incoming flow queues as a blocking queue set
	 * @return
	 */
	public BlockingQueueSet getIncomingFlowQueues();
	
	
	/**
	 * Returns the outgoing flow queues as a blocking queue set
	 * @return
	 */
	public BlockingQueueSet getOutgoingFlowQueues();
	
	/**
	 * This primitive is invoked by the requested Application Process to respond to an allocation 
	 * request from IPC. 
	 * @param port_id
	 * @param result
	 * @param reason
	 * @param application the callback to invoke the application for any call
	 * @throws IPCException
	 */
	public void submitAllocateResponse(int portId, boolean result, String reason, APService applicationCallback) throws IPCException;
	
	/**
	 * Used by an application process to specify that it is available through this IPC process
	 * @param applicationProcessNamingInfo
	 * @param application the callback to invoke the application for deliverAllocateRequest and any other calls
	 */
	public void register(ApplicationProcessNamingInfo applicationProcessNamingInfo, APService apService) throws IPCException;
	
	/**
	 * Used by an application process to specify that it is no longer available through this IPC process
	 * @param applicationProcessNamingInfo
	 */
	public void unregister(ApplicationProcessNamingInfo applicationProcessNamingInfo) throws IPCException;
}