package rina.applibrary.impl.test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rina.applibrary.impl.RINAFactory;
import rina.cdap.api.CDAPSessionManager;
import rina.delimiting.api.Delimiter;
import rina.encoding.api.Encoder;

public class RINASoftwareServer implements Runnable{
	
	/**
	 * Controls when the server will finish the execution
	 */
	private boolean end = false;
	
	/**
	 * The server socket that listens for incoming connections
	 */
	private ServerSocket serverSocket = null;
	
	private Delimiter delimiter = null;
	
	private CDAPSessionManager cdapSessionManager = null;
	
	private Encoder encoder = null;
	
	private ExecutorService executorService = null;
	
	public RINASoftwareServer(){
		delimiter = RINAFactory.getDelimiterInstance();
		cdapSessionManager = RINAFactory.getCDAPSessionManagerInstance();
		encoder = RINAFactory.getEncoderInstance();
		executorService = Executors.newFixedThreadPool(2);
		try {
			serverSocket = new ServerSocket(RINAFactory.DEFAULT_PORT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setEnd(boolean end){
		this.end = end;
		if (end){
			try{
				this.serverSocket.close();
			}catch(IOException ex){
				ex.printStackTrace();
			}
		}
	}

	public void run() {
		try{
			Socket socket = null;
			RINASoftwareSocketReader socketReader = null;
			
			while(!end){
				socket = serverSocket.accept();
				
				String address = socket.getInetAddress().getHostAddress();
				String hostname = socket.getInetAddress().getHostName();
				
				//Accept only local connections
				if (!address.equals("127.0.0.1") && !hostname.equals("localhost")){
					System.out.println("Connection attempt from "+address+" blocked");
					socket.close();
					continue;
				}
				
				System.out.println("Got a new request from "+socket.getInetAddress().getHostAddress() + 
						". Local port: "+socket.getLocalPort()+"; Remote port: "+socket.getPort());
				
				socketReader = new RINASoftwareSocketReader(socket, delimiter, cdapSessionManager, encoder);
				executorService.execute(socketReader);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			if (!end){
				setEnd(true);
			}
		}
	}

}
