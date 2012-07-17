package rina.shimipcprocess.ip;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.APService;
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
	
	/**
	 * The application to call back when a new connection is accepted
	 */
	private APService applicationCallback = null;
	
	private ApplicationProcessNamingInfo apNamingInfo= null;
	
	public UDPServer(String hostname, int port, FlowAllocatorImpl flowAllocator, 
			APService applicationCallback, ApplicationProcessNamingInfo apNamingInfo){
		this.hostname = hostname;
		this.port = port;
		this.flowAllocator = flowAllocator;
		this.applicationCallback = applicationCallback;
		this.apNamingInfo = apNamingInfo;
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
			log.info("Starting a new UDP server socket");
			InetAddress inetAddress = InetAddress.getByName(hostname);
			serverSocket = new DatagramSocket(port, inetAddress);
			byte[] receiveData = new byte[8192];
			DatagramPacket receivedPacket = null;
			
			log.info("Waiting for incoming UDP datagrams on interface "+inetAddress+ ", port "+port);
			while(!end){
				receivedPacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivedPacket);
				this.flowAllocator.datagramReceived(receivedPacket, port, applicationCallback, 
						apNamingInfo, serverSocket);
			}
		}catch(Exception  e){
			log.error(e.getMessage());
		}finally{
			listening = false;
		}
	}
}
