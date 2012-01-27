package rina.ipcmanager.impl.apservice;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Listens to local connections from applications that want to use the RINA services.
 * In reality the calls will come from the API libraries (Native RINA or faux sockets).
 * @author eduardgrasa
 *
 */
public class APServiceTCPServer implements Runnable{
	
private static final Log log = LogFactory.getLog(APServiceTCPServer.class);
	
	public static final int DEFAULT_PORT = 32771;
	
	/**
	 * The AP Service
	 */
	private APServiceImpl apService = null;
	
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
	
	public APServiceTCPServer(APServiceImpl apService){
		this(apService, DEFAULT_PORT);
	}
	
	public APServiceTCPServer(APServiceImpl apService, int port){
		this.apService = apService;
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
				
				//Call the AP Service
				apService.newConnectionAccepted(socket);
			}
		}catch(IOException e){
			log.error(e.getMessage());
		}
	}

}
