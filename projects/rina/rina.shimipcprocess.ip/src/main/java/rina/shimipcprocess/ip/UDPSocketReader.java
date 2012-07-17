package rina.shimipcprocess.ip;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import rina.ipcservice.api.APService;
import rina.shimipcprocess.ip.flowallocator.FlowAllocatorImpl;

public class UDPSocketReader implements Runnable{

	private APService applicationCallback = null;
	private int portId = -1;
	private FlowAllocatorImpl flowAllocator = null;
	private DatagramSocket datagramSocket = null;
	
	public UDPSocketReader(DatagramSocket datagramSocket, APService applicationCallback, int portId, FlowAllocatorImpl flowAllocator) {
		this.datagramSocket = datagramSocket;
		this.applicationCallback = applicationCallback;
		this.portId = portId;
		this.flowAllocator = flowAllocator;
	}

	public void run(){
		byte[] receiveData = new byte[8192];
		DatagramPacket datagramPacket = null;
		byte[] sdu = null;
		
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
			}catch(IOException ex){
				//TODO what to do?
			}
		}
		
		flowAllocator.socketClosed(this.portId);
	}

}
