package rina.ipcservice.impl.jobs;

import rina.ipcservice.api.APService;

/**
 * Delivers a list of SDUs to an application process
 * @author eduardgrasa
 *
 */
public class DeliverDeallocateJob implements Runnable{
	
	private APService applicationProcess = null;
	private int portId = 0;
	
	public DeliverDeallocateJob(APService applicationProcess, int portId){
		this.applicationProcess = applicationProcess;
		this.portId = portId;
	}
	
	public void run(){
		applicationProcess.deliverDeallocateResponse(portId, 0, null);
	}

}
