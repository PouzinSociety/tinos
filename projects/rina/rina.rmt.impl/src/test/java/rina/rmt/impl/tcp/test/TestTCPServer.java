package rina.rmt.impl.tcp.test;

import java.net.ServerSocket;
import java.net.Socket;

import rina.delimiting.api.Delimiter;
import rina.delimiting.api.DelimiterFactory;
import rina.delimiting.impl.DelimiterFactoryImpl;

/**
 * A TCP Server that pretends to be a remote IPC process
 * @author eduardgrasa
 */
public class TestTCPServer implements Runnable{

	private byte[] buffer = new byte[50];
	private byte[] delimitedMessage = null;
	private Delimiter delimiter = new DelimiterFactoryImpl().createDelimiter(DelimiterFactory.DIF);
	
	public void run() {
		try{
			ServerSocket serverSocket = new ServerSocket(40000);
			Socket socket = serverSocket.accept();
			Thread.sleep(1000);
			socket.getInputStream().read(buffer);
			delimitedMessage = delimiter.getDelimitedSdu(buffer);
			System.out.println(new String(delimitedMessage));
			socket.getOutputStream().write(delimiter.getDelimitedSdu("This is the response message".getBytes()));
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}

}
