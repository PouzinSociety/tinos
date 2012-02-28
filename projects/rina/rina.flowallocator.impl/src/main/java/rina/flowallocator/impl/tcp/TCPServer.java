package rina.flowallocator.impl.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.flowallocator.api.BaseFlowAllocator;
import rina.flowallocator.api.FlowAllocator;

/**
 * TCP server that listens for incoming connections.
 * @author eduardgrasa
 *
 */
public class TCPServer implements Runnable{

	private static final Log log = LogFactory.getLog(TCPServer.class);
	
	/**
	 * The Flow Allocator
	 */
	private FlowAllocator flowAllocator = null;
	
	/**
	 * Controls when the server will finish the execution
	 */
	private boolean end = false;
	
	/**
	 * The TCP port to listen for incoming connections
	 */
	private int port = 0;
	
	/**
	 * The server socket that listens for incoming connections
	 */
	private ServerSocket serverSocket = null;
	
	public TCPServer(FlowAllocator flowAllocator){
		this.flowAllocator = flowAllocator;
		try{
			this.port = Integer.parseInt(System.getProperty(BaseFlowAllocator.FLOW_ALLOCATOR_PORT_PROPERTY));
		}catch(Exception ex){
			this.port = BaseFlowAllocator.DEFAULT_PORT;
			log.info("Property "+BaseFlowAllocator.FLOW_ALLOCATOR_PORT_PROPERTY+" not found or invalid, " +
					"using default port ("+BaseFlowAllocator.DEFAULT_PORT+")");
		}
	}
	
	public int getPort(){
		return this.port;
	}
	
	public void setEnd(boolean end){
		this.end = end;
		if (end){
			try{
				this.serverSocket.close();
			}catch(IOException ex){
				log.error(ex.getMessage());
			}
		}
	}

	public void run() {
		try{
			serverSocket = new ServerSocket(port);
			log.info("Flow Allocator waiting for incoming TCP connections on port "+port);
			while (!end){
				Socket socket = serverSocket.accept();
				log.info("Got a new request from "+socket.getInetAddress().getHostAddress() + 
						". Local port: "+socket.getLocalPort()+"; Remote port: "+socket.getPort());
				flowAllocator.newConnectionAccepted(socket);
			}
		}catch(IOException e){
			log.error(e.getMessage());
		}
	}
}
