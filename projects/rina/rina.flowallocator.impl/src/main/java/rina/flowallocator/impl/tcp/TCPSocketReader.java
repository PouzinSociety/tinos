package rina.flowallocator.impl.tcp;

import java.io.IOException;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.flowallocator.api.FlowAllocatorInstance;

/**
 * Continuously reads data from a socket. When an amount of data has been received
 * (either delimited or just an undelimited amount of bytes, depending on the 
 * operation mode) it delivers them to TBD
 * @author eduardgrasa
 *
 */
public class TCPSocketReader implements Runnable{
	
	private static final Log log = LogFactory.getLog(TCPSocketReader.class);
	
	/**
	 * The socket to read the data from
	 */
	private Socket socket = null;
	
	/**
	 * A reference to the Flow Allocator instance that owns this flow
	 */
	private FlowAllocatorInstance flowAllocatorInstance = null;
	
	/**
	 * Controls the end of the execution of this Runnable
	 */
	private boolean end = false;
	
	public TCPSocketReader(Socket socket){
		this.socket = socket;
	}
	
	public synchronized void setEnd(){
		this.end = true;
	}

	public void run() {
		log.debug("Started socket reader. Remote IP address: "+socket.getInetAddress().getHostAddress()+
				". Remote port: "+socket.getPort()+"; local port: "+socket.getLocalPort());
		
		int value = 0;
		
		while(!end){
			try{
				value = socket.getInputStream().read();
				if (value == -1){
					break;
				}
			}catch(IOException ex){
				ex.printStackTrace();
				end = true;
			}
		}
		
		try{
			socket.close();
		}catch(IOException ex){
			ex.printStackTrace();
		}
		
		log.debug("The remote endpoint of flow "+socket.getPort()+" has disconnected. Notifying the Flow Allocator instance");
		flowAllocatorInstance.socketClosed();
	}

}
