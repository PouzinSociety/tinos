package rina.flowallocator.api;

import rina.ipcservice.api.AllocateRequest;
import rina.ipcservice.api.ApplicationProcessNamingInfo;

public interface FlowAllocatorInstanceService {

	/**
	 * Call by the FA in order to forward a request to a FAI 
	 * @param request
	 */
	public void forwardAllocateRequest(AllocateRequest request) ;
	

	public void forwardDeallocate(int port_id) ;


	
	

}
