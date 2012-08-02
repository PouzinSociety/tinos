package rina.shimipcprocess.ip;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.ipcmanager.api.IPCManager;
import rina.shimipcprocess.ip.flowallocator.FlowAllocatorImpl;

public class UDPSocketReader implements Runnable{

	private static final Log log = LogFactory.getLog(UDPSocketReader.class);

	private IPCManager ipcManager = null;
	private int portId = -1;
	private FlowAllocatorImpl flowAllocator = null;
	private DatagramSocket datagramSocket = null;
	
	public UDPSocketReader(DatagramSocket datagramSocket, IPCManager ipcManager, int portId, FlowAllocatorImpl flowAllocator) {
		this.datagramSocket = datagramSocket;
		this.ipcManager = ipcManager;
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
					this.ipcManager.getOutgoingFlowQueue(this.portId).writeDataToQueue(sdu);
				}
			}catch(Exception ex){
				log.error(ex);
				if (datagramSocket.isClosed()){
					break;
				}
			}
		}
		
		flowAllocator.socketClosed(this.portId);
	}

}
