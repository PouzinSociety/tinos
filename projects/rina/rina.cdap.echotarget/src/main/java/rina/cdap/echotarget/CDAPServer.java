package rina.cdap.echotarget;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPSession;
import rina.cdap.api.CDAPSessionFactory;
import rina.cdap.impl.CDAPSessionFactoryImpl;
import rina.cdap.impl.WireMessageProviderFactory;
import rina.cdap.impl.googleprotobuf.GoogleProtocolBufWireMessageProviderFactory;
import rina.delimiting.api.Delimiter;
import rina.delimiting.api.DelimiterFactory;
import rina.delimiting.impl.DelimiterFactoryImpl;
import rina.serialization.api.SerializationFactory;
import rina.serialization.api.Serializer;
import rina.utils.serialization.googleprotobuf.GPBSerializationFactory;

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
public class CDAPServer {
	
	private static final Log log = LogFactory.getLog(CDAPServer.class);
	
	public static final int DEFAULT_ECHO_PORT = 32767;
	public static final int DEFAULT_ENROLLMENT_PORT = 32768;
	
	/**
	 * The maximum number of worker threads in the CDAP Echo Server thread pool
	 */
	private static int MAXWORKERTHREADS = 5;

	/**
	 * The factory to create/remove CDAP sessions
	 */
	private CDAPSessionFactory cdapSessionFactory = null;
	
	/**
	 * The delimiter factory for the sessions
	 */
	private DelimiterFactory delimiterFactory = null;
	
	/**
	 * The serialization factory user for sessions
	 */
	private SerializationFactory serializationFactory = null;
	
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
	
	/**
	 * The type of CDAP server (echo, enrollment, ...)
	 */
	private String type = null;
	
	public CDAPServer(CDAPSessionFactory cdapSessionFactory, DelimiterFactory delimiterFactory, SerializationFactory serializationFactory, int port, String type){
		this.executorService = Executors.newFixedThreadPool(MAXWORKERTHREADS);
		this.cdapSessionFactory = cdapSessionFactory;
		this.delimiterFactory = delimiterFactory;
		this.serializationFactory = serializationFactory;
		this.port = port;
		this.type = type;
	}

	public CDAPSessionFactory getCdapSessionFactory() {
		return cdapSessionFactory;
	}

	public void setCdapSessionFactory(CDAPSessionFactory cdapSessionFactory) {
		this.cdapSessionFactory = cdapSessionFactory;
	}
	
	public DelimiterFactory getDelimiterFactory() {
		return delimiterFactory;
	}

	public void setDelimiterFactory(DelimiterFactory delimiterFactory) {
		this.delimiterFactory = delimiterFactory;
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
				log.info("Got a new request from "+socket.getInetAddress().getHostAddress());
				CDAPSession cdapSession = cdapSessionFactory.createCDAPSession();
				Delimiter delimiter = delimiterFactory.createDelimiter(DelimiterFactory.DIF);
				Serializer serializer = serializationFactory.createSerializerInstance();
				CDAPWorker cdapWorker = CDAPWorkerFactory.createCDAPWorker(type, socket, cdapSession, delimiter, serializer);
				executorService.execute(cdapWorker);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error(e.getMessage());
		}
	}
	
	/**
	 * Returns a new instance of a CDAP Echo server that will listen at port «port«
	 * The server is not started yet, the 'run' operation has to be called (blocking)
	 * @param port
	 * @return
	 */
	public static CDAPServer getNewInstance(int port, String type){
		CDAPSessionFactoryImpl cdapSessionFactory = new CDAPSessionFactoryImpl();
		WireMessageProviderFactory wmpFactory = new GoogleProtocolBufWireMessageProviderFactory();
		cdapSessionFactory.setWireMessageProviderFactory(wmpFactory);
		DelimiterFactory delimiterFactory = new DelimiterFactoryImpl();
		SerializationFactory serializationFactory = new GPBSerializationFactory();
		CDAPServer cdapEchoServer = new CDAPServer(cdapSessionFactory, delimiterFactory, serializationFactory, port, type);
		return cdapEchoServer;
	}
}
