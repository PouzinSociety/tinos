package rina.shimipcprocess.ip.flowallocator;

import java.util.TimerTask;

import rina.ipcservice.api.APService;

public class AllocateResponseTimerTask extends TimerTask{
	
	private APService applicationCallback = null;
	private int portId = -1;
	
	public AllocateResponseTimerTask(APService applicationCallback, int portId){
		this.applicationCallback = applicationCallback;
		this.portId = portId;
	}

	@Override
	public void run() {
		this.applicationCallback.deliverAllocateResponse(portId, 0, null);
	}

}
