package rina.shimipcprocess.ip;

import java.net.DatagramSocket;
import java.util.TimerTask;

public class CloseUDPSocketTimerTask extends TimerTask{

	private DatagramSocket datagramSocket = null;
	
	public CloseUDPSocketTimerTask(DatagramSocket datagramSocket){
		this.datagramSocket = datagramSocket;
	}
	
	@Override
	public void run() {
		this.datagramSocket.close();
	}
	

}
