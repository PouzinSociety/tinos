package rina.flowallocator.impl.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.flowallocator.api.FlowAllocator;

/**
 * TCP server that listens for incoming connections.
 * @author eduardgrasa
 *
 */
public class TCPServer implements Runnable{

	private static final Log log = LogFactory.getLog(TCPServer.class);
	
	public static final int DEFAULT_PORT = 32770;
	
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
		this(flowAllocator, DEFAULT_PORT);
	}
	
	public TCPServer(FlowAllocator flowAllocator, int port){
		this.flowAllocator = flowAllocator;
		this.port = port;
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
