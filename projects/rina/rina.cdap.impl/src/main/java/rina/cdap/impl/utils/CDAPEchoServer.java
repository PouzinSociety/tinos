package rina.cdap.impl.utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPSession;
import rina.cdap.api.CDAPSessionFactory;

/**
 * CDAP echo target.  This is a very basic CDAP/GPB test process listening on an Internet port for a TCP connection.  
 * It will accept the connection and reply to any M_CONNECT request, then go into an echo mode, where operations such as 
 * starting, writing, and reading objects will simply result in an echoed version of the data in the request being written 
 * back as a formatted string (using a shared printf format string so that the exact characters from different implementations 
 * should match) and a re-encoded version of the same message, encapsulated as CDAP M_WRITEÕs of particular objects.  This can 
 * be used to test delimiting, GPB implementations, number coding and boundary conditions, message parsing, and other basic 
 * functions for compatibility among implementations.  We will experiment with using the same port numbers we intend to reserve 
 * in the future in order to get experience with firewall issues.
 * Assumes each new incoming connection is a new CDAP session. When the TCP connection breaks, the CDAP session over it is 
 * discarded.
 * @author eduardgrasa
 *
 */
public class CDAPEchoServer {
	
	private static final Log log = LogFactory.getLog(CDAPEchoServer.class);
	
	/**
	 * The maximum number of worker threads in the CDAP Echo Server thread pool
	 */
	private static int MAXWORKERTHREADS = 5;

	/**
	 * The factory to create/remove CDAP sessions
	 */
	private CDAPSessionFactory cdapSessionFactory = null;
	
	/**
	 * The TCP port to listen for incoming connections
	 */
	private int port = 0;
	
	/**
	 * The server socket that listens for incoming connections
	 */
	private ServerSocket serverSocket = null;
	
	/**
	 * The thread pool implementation
	 */
	private ExecutorService executorService = null;
	
	public CDAPEchoServer(CDAPSessionFactory cdapSessionFactory, int port){
		this.executorService = Executors.newFixedThreadPool(MAXWORKERTHREADS);
		this.cdapSessionFactory = cdapSessionFactory;
		this.port = port;
	}

	public CDAPSessionFactory getCdapSessionFactory() {
		return cdapSessionFactory;
	}

	public void setCdapSessionFactory(CDAPSessionFactory cdapSessionFactory) {
		this.cdapSessionFactory = cdapSessionFactory;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public void run(){
		try {
			serverSocket = new ServerSocket(port);
			log.info("Waiting for incoming TCP connections on port "+port);
			while(true){
				Socket socket = serverSocket.accept();
				log.info("Got a new request");
				CDAPSession cdapSession = cdapSessionFactory.createCDAPSession();
				CDAPEchoWorker cdapEchoWorker = new CDAPEchoWorker(socket, cdapSession);
				executorService.execute(cdapEchoWorker);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e.getMessage());
		}
	}
}
