package rina.examples.apps.fauxsockets.chatserver;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class TCPServer implements Runnable{
	
	public static final int port = 46743;
	
	private ServerSocket serverSocket = null;
	private ExecutorService executorService = null;
	private ChatServer chatServer = null;
	
	private boolean end = false;
	
	public TCPServer(ExecutorService executorService){
		this.executorService = executorService;
		chatServer = new ChatServer();
	}
	
	public void finish(){
		this.end = true;
		try{
			serverSocket.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void run(){
		try{
			serverSocket = new ServerSocket(port);
			Socket socket = null;
			
			while(!end){
				System.out.println("ChatServer: Waiting for chat clients to connect");
				socket = serverSocket.accept();
				System.out.println("ChatServer: Accepted socket! "+socket.getLocalPort()+" "+socket.getPort());
				ChatWorker chatWorker = new ChatWorker(socket, chatServer);
				executorService.execute(chatWorker);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

}
