package rina.shimipcprocess.ip.flowallocator;

import java.util.TimerTask;

import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.APService;
import rina.shimipcprocess.ip.TCPSocketReader;
import rina.shimipcprocess.ip.UDPSocketReader;

public class AllocateResponseTimerTask extends TimerTask{
	
	private APService applicationCallback = null;
	private IPCProcess ipcProcess = null;
	private TCPSocketReader tcpSocketReader = null;
	private UDPSocketReader udpSocketReader = null;
	private int portId = -1;
	
	public AllocateResponseTimerTask(APService applicationCallback, IPCProcess ipcProcess, 
			TCPSocketReader tcpSocketReader, UDPSocketReader udpSocketReader, int portId){
		this.applicationCallback = applicationCallback;
		this.ipcProcess = ipcProcess;
		this.tcpSocketReader = tcpSocketReader;
		this.udpSocketReader = udpSocketReader;
		this.portId = portId;
	}

	@Override
	public void run() {
		this.applicationCallback.deliverAllocateResponse(portId, 0, null);
		if (this.tcpSocketReader != null){
			this.ipcProcess.execute(tcpSocketReader);
		}else if (this.udpSocketReader != null){
			this.ipcProcess.execute(udpSocketReader);
		}
	}

}
