package rina.examples.apps.fauxsockets.chatclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class SocketReader implements Runnable{
	
	private Socket socket = null;
	
	public SocketReader(Socket socket){
		this.socket = socket;
	}

	public void run() {
		try{
			//BufferedReader bufferedReader =  new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
			String line = null;
			
			while((line= Main.readLine(socket.getInputStream())/*bufferedReader.readLine()*/)!=null){
				System.out.println(line);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			System.out.println("Lost the connection with the server! Ending session");
			System.exit(0);
		}
	}

}
