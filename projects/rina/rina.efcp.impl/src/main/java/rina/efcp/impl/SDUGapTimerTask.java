package rina.efcp.impl;

import java.util.TimerTask;

import rina.efcp.api.DataTransferAEInstance;

/**
 * Task that executes when the SDUGapTimer fires
 * @author eduardgrasa
 */
public class SDUGapTimerTask extends TimerTask{
	
	/**
	 * The data transfer AE Instance associated to this timer task
	 */
	private DataTransferAEInstance dataTransferAEInstance = null;
	
	public SDUGapTimerTask(DataTransferAEInstance dataTransferAEInstance){
		this.dataTransferAEInstance = dataTransferAEInstance;
	}

	public DataTransferAEInstance getDataTransferAEInstance() {
		return dataTransferAEInstance;
	}

	public void setDataTransferAEInstance(DataTransferAEInstance dataTransferAEInstance) {
		this.dataTransferAEInstance = dataTransferAEInstance;
	}

	@Override
	public void run() {
		dataTransferAEInstance.sduGapTimerFired();
	}

}
