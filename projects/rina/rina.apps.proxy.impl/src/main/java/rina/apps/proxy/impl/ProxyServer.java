package rina.apps.proxy.impl;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A proxy server that listens for incoming application requests from a TCP/IP network, 
 * extracts the application name from it, and allocates a flow to the application through 
 * an underlying DIF. If the flow is successfull, the proxy relays application data back 
 * and forth between the TCP/IP network and the DIF.
 * @author eduardgrasa
 *
 */
public class ProxyServer implements Runnable{
	
	public static final String APPLICATION_PROCESS_NAME = "rina.apps.proxy";
	
	public static final int LISTEN_PORT = 8000;
	
	private static final Log log = LogFactory.getLog(ProxyServer.class);
	
	private ExecutorService executorService = null;
	
	private ServerSocket serverSocket = null;
	
	public ProxyServer(ExecutorService executorService){
		this.executorService = executorService;
	}

	public void run() {
		Socket socket = null;
		ProxyWorker proxyWorker = null;
		
		try{
			serverSocket = new ServerSocket(LISTEN_PORT);
			log.info("Proxy server started, listening at port "+LISTEN_PORT+" for incoming requests.");
			
			while(true){
				try{
					socket = serverSocket.accept();
					log.info("Got a new request!" + "\n" + "" +
							"Hostname: "+socket.getInetAddress().getHostName() + "\n" + 
							"Local port: "+socket.getLocalPort() + "\n" + 
							"Remote port: "+socket.getPort());
					
					proxyWorker = new ProxyWorker(socket);
					executorService.execute(proxyWorker);
				}catch(Exception ex){
					ex.printStackTrace();
					log.error(ex);
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
			log.error(ex);
		}finally{
			if (!serverSocket.isClosed()){
				try{
					serverSocket.close();
				}catch(Exception ex){
				}
			}
		}
		
	}
}
