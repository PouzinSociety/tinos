package rina.flowallocator.impl;

import java.util.Map;

import rina.cdap.api.message.CDAPMessage;
import rina.ipcservice.api.AllocateRequest;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.IPCService;
import rina.ipcservice.api.QoSCube;
import java.util.Enumeration;


/** 
 * Implements the Flow Allocator
 */

public class FlowAllocator implements IPCService {
	
	private ApplicationProcessNamingInfo requestedAPinfo = null;
	private int portId = 0;
	private QoSCube cube = null;
	private boolean result = false;
	private int FAid = 0;
	
	private boolean validAllocateRequest = false;
	private boolean validApplicationProcessNamingInfo = false;
	private boolean AllocateNotifyPolicy = false;



	public FlowAllocator() {

	}

	public void submitAllocateRequest(AllocateRequest request) {
		try {
			if(validateRequest(request))
			{
				request.setPort_id(assignPortId());
				FlowAllocatorInstance FAI = new FlowAllocatorInstance();
				//TODO subscribeToMessages(MessageSubscription messageSubscription, MessageSubscriber messageSubscriber);
				
//			if(AllocateNotifyPolicy)
//				deliverAllocateResponse(request.getRequestedAPinfo(), request.getPort_id(), true, "");
}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		else
//			deliverAllocateResponse(request.getRequestedAPinfo(), request.getPort_id(), false, "The format of request was not valid");
	}

	/**
	 * 
	 * Assigns a port-id for the specific AllocateRequest
	 */
	private int assignPortId() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void submitAllocateResponse(int portId, boolean result) {
		// TODO Auto-generated method stub
		
	}

	public void submitDeallocate(int portId) {
		// TODO Auto-generated method stub
		
	}

	
	public void submitStatus(int portId) {
		// TODO Auto-generated method stub
		
	}

	public void submitTransfer(int portId, byte[] sdu, boolean result) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Validates an AllocateRequest: 
	 * 		- ApplicationProcessNamingInfo requestedAPinfo
	 * 		- int port_id
	 * 		- QoSCube cube
	 * 		- boolean result
	 * @param request
	 * @return yes if valid, no otherwise
	 * @throws Exception 
	 */
	public boolean validateRequest(AllocateRequest request) throws Exception{
		if (validateApplicationProcessNamingInfo(request.getRequestedAPinfo())
				&& validateQoScube(request.getCube()))
			validAllocateRequest = true; 
		return validAllocateRequest;
	}


	public boolean submitTransfer(int portId, byte[] sdu) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public void register(
			ApplicationProcessNamingInfo applicationProcessNamingInfo) {
		// TODO Auto-generated method stub
		
	}
	
	public void unregister(
			ApplicationProcessNamingInfo applicationProcessNamingInfo) {
		// TODO Auto-generated method stub
		
	}

	
	/** 
	 * Validates the AP naming info (applicationProcessName, applicationProcessInstance, applicationEntityName and applicationEntityInstance)
	 * @param APnamingInfo
	 * @throws Exception
	 */
	public boolean validateApplicationProcessNamingInfo(ApplicationProcessNamingInfo APnamingInfo) throws Exception{
		if (validateApplicationProcessName(APnamingInfo.getApplicationProcessName()) &&
		validateApplicationProcessInstance(APnamingInfo.getApplicationProcessInstance()) &&
		validateApplicationEntityName(APnamingInfo.getApplicationEntityName()) &&
		validateApplicationEntityInstance(APnamingInfo.getApplicationEntityInstance()))
			validApplicationProcessNamingInfo = true;
		return validApplicationProcessNamingInfo;	
	}
	
	/**
	 * Validates the applicationProcessName
	 * @param applicationProcessName
	 * @throws Exception
	 */
	private boolean validateApplicationProcessName(String applicationProcessName) throws Exception
	{
		if (applicationProcessName!=null)
			return true;
		else
			throw new Exception("Application process name is empty");
	}
	
	
	/**
	 * Validates the applicationProcessInstance
	 * @param applicationProcessInstance
	 * @throws Exception
	 */
	private boolean validateApplicationProcessInstance(String applicationProcessInstance) throws Exception
	{
		if (applicationProcessInstance!=null)
			return true;
		else
			throw new Exception("Application process instance is empty");	
	}
	
	
	
	/**
	 * Validates the applicationEntityName
	 * @param ApplicationEntityName
	 * @throws Exception
	 */
	private boolean validateApplicationEntityName(String ApplicationEntityName) throws Exception
	{
		if (ApplicationEntityName!=null)
			return true;
		else
			throw new Exception("Application entity name is empty");
	}
	
	
	
	/**
	 * Validates the applicationEntityInstance
	 * @param applicationEntityInstance
	 * @throws Exception
	 */
	private boolean validateApplicationEntityInstance(String applicationEntityInstance) throws Exception
	{
		if (applicationEntityInstance!=null)
			return true;
		else
			throw new Exception("Application entity instance is empty");
	}
	
	
	public boolean validateQoScube(QoSCube cube) {
		Map<String, Object> qos_cube = cube.getCube();
		//TODO add check
		return true;
	}

	

}
