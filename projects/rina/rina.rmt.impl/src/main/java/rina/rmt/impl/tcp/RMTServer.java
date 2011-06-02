package rina.rmt.impl.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TCP server that listens for incoming connections.
 * @author eduardgrasa
 *
 */
public class RMTServer implements Runnable{

	private static final Log log = LogFactory.getLog(RMTServer.class);
	
	public static final int DEFAULT_PORT = 32769;
	
	/**
	 * A relaying and multiplexing task that operates on top of a TCP/IP network
	 */
	private TCPRMTImpl tcpRmtImpl = null;
	
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
	
	public RMTServer(TCPRMTImpl tcpRmtImpl){
		this(tcpRmtImpl, DEFAULT_PORT);
	}
	
	public RMTServer(TCPRMTImpl tcpRmtImpl, int port){
		this.tcpRmtImpl = tcpRmtImpl;
		this.port = port;
	}
	
	public void setEnd(boolean end){
		this.end = end;
	}

	public void run() {
		try{
			serverSocket = new ServerSocket(port);
			log.info("Waiting for incoming TCP connections on port "+port);
			while (!end){
				Socket socket = serverSocket.accept();
				log.info("Got a new request from "+socket.getInetAddress().getHostAddress());
				tcpRmtImpl.newConnectionAccepted(socket);
			}
		}catch(IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e.getMessage());
		}
	}
}
