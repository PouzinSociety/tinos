package rina.shimipcprocess.ip;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Timer;

import rina.ipcservice.api.APService;
import rina.shimipcprocess.ip.flowallocator.FlowAllocatorImpl;

public class UDPSocketReader implements Runnable{
	
	public static final int TIMER_PERIOD_IN_MILISECONDS = 24*3600*1000;

	private APService applicationCallback = null;
	private int portId = -1;
	private FlowAllocatorImpl flowAllocator = null;
	private DatagramSocket datagramSocket = null;
	private Timer timer = null;
	
	public UDPSocketReader(DatagramSocket datagramSocket, APService applicationCallback, int portId, FlowAllocatorImpl flowAllocator) {
		this.datagramSocket = datagramSocket;
		this.applicationCallback = applicationCallback;
		this.portId = portId;
		this.flowAllocator = flowAllocator;
		this.timer = new Timer();
	}

	public void run(){
		byte[] receiveData = new byte[8192];
		DatagramPacket datagramPacket = null;
		byte[] sdu = null;
		CloseUDPSocketTimerTask timerTask = new CloseUDPSocketTimerTask(datagramSocket);
		this.timer.schedule(timerTask, TIMER_PERIOD_IN_MILISECONDS);
		
		while(datagramSocket.isConnected()){
			try{
				datagramPacket = new DatagramPacket(receiveData, receiveData.length);
				datagramSocket.receive(datagramPacket);
				if (datagramPacket.getLength() > 0){
					sdu = new byte[datagramPacket.getLength()];
					System.arraycopy(datagramPacket.getData(), 
							datagramPacket.getOffset(), sdu, 0, datagramPacket.getLength());
					this.applicationCallback.deliverTransfer(portId, sdu);
				}
				
				timerTask.cancel();
				timerTask = new CloseUDPSocketTimerTask(datagramSocket);
				this.timer.schedule(timerTask, TIMER_PERIOD_IN_MILISECONDS);
			}catch(IOException ex){
				//TODO what to do?
			}
		}
		
		flowAllocator.socketClosed(this.portId);
	}

}
