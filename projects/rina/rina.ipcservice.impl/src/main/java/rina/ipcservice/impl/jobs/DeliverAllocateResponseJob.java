package rina.ipcservice.impl.jobs;

import rina.ipcservice.api.APService;
import rina.ipcservice.api.ApplicationProcessNamingInfo;

/**
 * Delivers an allocate response to the application 
 * @author eduardgrasa
 *
 */
public class DeliverAllocateResponseJob implements Runnable{
	
	private APService applicationProcess = null;
	private ApplicationProcessNamingInfo apNamingInfo = null;
	private int portId = 0;
	private int errorCode = 0;
	private String errorReason = null;
	
	public DeliverAllocateResponseJob(APService applicationProcess, ApplicationProcessNamingInfo apNamingInfo, int portId, int errorCode, String errorReason){
		this.applicationProcess = applicationProcess;
		this.apNamingInfo = apNamingInfo;
		this.portId = portId;
		this.errorCode = errorCode;
		this.errorReason = errorReason;
	}
	
	public void run(){
		applicationProcess.deliverAllocateResponse(apNamingInfo, portId, errorCode, errorReason);
	}

}
