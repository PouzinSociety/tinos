package rina.ipcmanager.impl.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.ipcmanager.impl.IPCManagerImpl;


/**
 * Listens to local connections from applications that want to use the RINA services.
 * In reality the calls will come from the API libraries (Native RINA or faux sockets).
 * @author eduardgrasa
 *
 */
public class IPCManagerTCPServer implements Runnable{
	
private static final Log log = LogFactory.getLog(IPCManagerTCPServer.class);
	
	public static final int DEFAULT_PORT = 32771;
	
	/**
	 * The IPC Manager
	 */
	private IPCManagerImpl ipcManager = null;
	
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
	
	public IPCManagerTCPServer(IPCManagerImpl ipcManager){
		this(ipcManager, DEFAULT_PORT);
	}
	
	public IPCManagerTCPServer(IPCManagerImpl ipcManager, int port){
		this.ipcManager = ipcManager;
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
			log.info("IPC Manager waiting for incoming TCP connections from applications on port "+port);
			while (!end){
				Socket socket = serverSocket.accept();
				String address = socket.getInetAddress().getHostAddress();
				String hostname = socket.getInetAddress().getHostName();
				
				//Accept only local connections
				if (!address.equals("127.0.0.1") && !hostname.equals("localhost")){
					log.info("Connection attempt from "+address+" blocked");
					socket.close();
					continue;
				}
				
				log.info("Got a new request from "+socket.getInetAddress().getHostAddress() + 
						". Local port: "+socket.getLocalPort()+"; Remote port: "+socket.getPort());
				
				//Call the IPC Manager
				ipcManager.newConnectionAccepted(socket);
			}
		}catch(IOException e){
			log.error(e.getMessage());
		}
	}

}
