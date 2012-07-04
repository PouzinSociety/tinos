package rina.shimipcprocess.ip;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.APService;
import rina.shimipcprocess.ip.flowallocator.FlowAllocatorImpl;

/**
 * TCP server that listens for incoming connections on behalf of a certain application
 * @author eduardgrasa
 *
 */
public class TCPServer implements Runnable{

	private static final Log log = LogFactory.getLog(TCPServer.class);
	
	/**
	 * Controls when the server will finish the execution
	 */
	private boolean end = false;
	
	/**
	 * Controls whether the server is listening
	 */
	private boolean listening = false;
	
	/**
	 * The TCP port to listen for incoming connections
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
	private ServerSocket serverSocket = null;
	
	/**
	 * The application to call back when a new connection is accepted
	 */
	private APService applicationCallback = null;
	
	
	private ApplicationProcessNamingInfo apNamingInfo= null;
	
	public TCPServer(String hostname, int port, APService applicationCallback, 
			ApplicationProcessNamingInfo apNamingInfo, FlowAllocatorImpl flowAllocator){
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
			}catch(IOException ex){
				log.error(ex.getMessage());
			}finally{
				listening = false;
			}
		}
	}
	
	public boolean isListening(){
		return listening;
	}
	
	public void run() {
		try{
			log.debug("Starting a new TCP server socket");
			InetAddress inetAddress = InetAddress.getByName(hostname);
			serverSocket = new ServerSocket(port, 0, inetAddress);
			listening = true;
			log.info("Waiting for incoming TCP connections on interface "+inetAddress + ", port "+port);
			while (!end){
				Socket socket = serverSocket.accept();
				log.debug("Got a new request!");
				log.debug("Source IP address: "+socket.getInetAddress().getHostAddress());
				log.debug("Source port: "+socket.getPort());
				log.debug("Destination IP address: "+socket.getLocalAddress().getHostAddress());
				log.debug("Destination port: "+socket.getLocalPort());
				this.flowAllocator.newConnectionAccepted(socket, this.applicationCallback, this.apNamingInfo);
			}
		}catch(IOException e){
			log.error(e.getMessage());
		}finally{
			listening = false;
		}
	}

	
}
