package rina.ipcservice.impl;

import java.util.List;

import rina.ipcservice.api.APService;

/**
 * Delivers a list of SDUs to an application process
 * @author eduardgrasa
 *
 */
public class DeliverSDUThread extends Thread{
	
	private APService applicationProcess = null;
	private List<byte[]> sdus = null;
	private int portId = 0;
	
	public DeliverSDUThread(APService applicationProcess, List<byte[]> sdus, int portId){
		this.applicationProcess = applicationProcess;
		this.sdus = sdus;
		this.portId = portId;
	}
	
	@Override
	public void run(){
		for (int i=0; i<sdus.size(); i++){
			applicationProcess.deliverTransfer(portId, sdus.get(i), true);
		}
	}

}
