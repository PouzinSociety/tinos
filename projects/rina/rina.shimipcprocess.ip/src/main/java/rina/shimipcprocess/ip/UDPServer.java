package rina.shimipcprocess.ip;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.shimipcprocess.ip.flowallocator.FlowAllocatorImpl;

public class UDPServer implements Runnable{
	private static final Log log = LogFactory.getLog(UDPServer.class);
	
	/**
	 * Controls when the server will finish the execution
	 */
	private boolean end = false;
	
	/**
	 * Controls whether the server is listening
	 */
	private boolean listening = false;
	
	/**
	 * The UDP port to listen for incoming connections
	 */
	private int port = 0;
	
	/**
	 * The hostname or IP address of the interface the 
	 * Shim IPC Process is bounded to 
	 */
	private String hostname = null;
	
	/**
	 * The flow allocator
	 */
	private FlowAllocatorImpl flowAllocator = null;
	
	/**
	 * The server socket that listens for incoming connections
	 */
	private DatagramSocket serverSocket = null;
	
	public UDPServer(String hostname, int port, FlowAllocatorImpl flowAllocator){
		this.hostname = hostname;
		this.port = port;
		this.flowAllocator = flowAllocator;
	}
	
	public void setEnd(boolean end){
		this.end = end;
		if (end){
			try{
				this.serverSocket.close();
			}catch(Exception ex){
				log.error(ex.getMessage());
			}finally{
				listening = false;
			}
		}
	}
	
	public DatagramSocket getSocket(){
		return this.serverSocket;
	}
	
	public boolean isListening(){
		return listening;
	}
	
	public void run() {
		try{
			InetAddress inetAddress = InetAddress.getByName(hostname);
			serverSocket = new DatagramSocket(port, inetAddress);
			byte[] receiveData = new byte[8192];
			DatagramPacket receivedPacket = null;
			
			log.info("Waiting for incoming UDP datagrams on interface "+inetAddress+ ", port "+port);
			while(!end){
				receivedPacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivedPacket);
				log.debug("Received a UDP packet!");
				log.debug("Source address: "+receivedPacket.getAddress().getHostName());
				log.debug("Source port: "+receivedPacket.getPort());
				log.debug("Length: "+receivedPacket.getLength());
			}
		}catch(Exception  e){
			log.error(e.getMessage());
		}finally{
			listening = false;
		}
	}
}