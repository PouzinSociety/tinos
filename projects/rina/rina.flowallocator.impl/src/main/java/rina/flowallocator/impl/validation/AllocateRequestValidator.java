package rina.flowallocator.impl.validation;

import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCException;

/**
 * Contains the logic to validate an allocate request
 * @author eduardgrasa
 *
 */
public class AllocateRequestValidator {
	
	public void validateAllocateRequest(FlowService flowService) throws IPCException{
		validateApplicationProcessNamingInfo(flowService.getSourceAPNamingInfo());
		validateApplicationProcessNamingInfo(flowService.getDestinationAPNamingInfo());
	}
	
	/** 
	 * Validates the AP naming info. The current validation just requires application 
	 * process name not to be null
	 * @param APnamingInfo
	 * @throws Exception
	 */
	private void validateApplicationProcessNamingInfo(ApplicationProcessNamingInfo apNamingInfo) throws IPCException{
		if (apNamingInfo == null){
			throw new IPCException(IPCException.MALFORMED_ALLOCATE_REQUEST, ErrorDescriptions.NULL_APPLICATION_NAMING_INFO);
		}
		
		if (apNamingInfo.getApplicationProcessName() == null){
			throw new IPCException(IPCException.MALFORMED_ALLOCATE_REQUEST, ErrorDescriptions.NULL_APPLICATION_PROCESS_NAME);
		}
	}

}
