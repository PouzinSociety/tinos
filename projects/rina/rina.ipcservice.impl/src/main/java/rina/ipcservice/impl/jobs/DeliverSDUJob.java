package rina.ipcservice.impl.jobs;

import java.util.List;

import rina.ipcservice.api.APService;

/**
 * Delivers a list of SDUs to an application process
 * @author eduardgrasa
 *
 */
public class DeliverSDUJob implements Runnable{
	
	private APService applicationProcess = null;
	private List<byte[]> sdus = null;
	private int portId = 0;
	
	public DeliverSDUJob(APService applicationProcess, List<byte[]> sdus, int portId){
		this.applicationProcess = applicationProcess;
		this.sdus = sdus;
		this.portId = portId;
	}
	
	public void run(){
		for (int i=0; i<sdus.size(); i++){
			applicationProcess.deliverTransfer(portId, sdus.get(i));
		}
	}

}
