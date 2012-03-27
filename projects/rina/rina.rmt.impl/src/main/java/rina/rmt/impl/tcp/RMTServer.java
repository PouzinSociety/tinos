package rina.rmt.impl.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.configuration.RINAConfiguration;

/**
 * TCP server that listens for incoming connections.
 * @author eduardgrasa
 *
 */
public class RMTServer implements Runnable{

	private static final Log log = LogFactory.getLog(RMTServer.class);
	
	/**
	 * A relaying and multiplexing task that operates on top of a TCP/IP network
	 */
	private TCPRMTImpl tcpRmtImpl = null;
	
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
	 * The server socket that listens for incoming connections
	 */
	private ServerSocket serverSocket = null;

	public RMTServer(TCPRMTImpl tcpRmtImpl){
		this.tcpRmtImpl = tcpRmtImpl;
		this.port = RINAConfiguration.getInstance().getRMTPortNumber(tcpRmtImpl.getIPCProcess().getApplicationProcessName());
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
			serverSocket = new ServerSocket(port);
			listening = true;
			log.info("Waiting for incoming TCP connections on port "+port);
			while (!end){
				Socket socket = serverSocket.accept();
				log.info("Got a new request from "+socket.getInetAddress().getHostAddress());
				tcpRmtImpl.newConnectionAccepted(socket);
			}
		}catch(IOException e){
			log.error(e.getMessage());
		}finally{
			listening = false;
		}
	}
}
