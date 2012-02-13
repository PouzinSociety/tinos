package rina.examples.apps.fauxsockets.chatserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatWorker implements Runnable{
	
	private Socket socket = null;
	private String userName = null;
	private String chatRoom = null;
	private BufferedReader bufferedReader = null;
	private PrintWriter printWriter = null;
	
	public ChatWorker(Socket socket){
		this.socket = socket;
	}

	public void run() {
		String line = null;
		String textToEcho = null;
		
		try{
			System.out.println("Chat Worker started!");
			bufferedReader =  new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
			printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);
			userName = bufferedReader.readLine();
			chatRoom = bufferedReader.readLine();
			System.out.println("User "+userName+" joined chat room "+chatRoom);
			
			while((line=bufferedReader.readLine())!=null){
				textToEcho = userName+"@"+chatRoom+": "+line;
				System.out.println("User "+userName+" at chatRoom "+chatRoom+" says: "+line);
				printWriter.println(textToEcho);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			System.out.println("Disconnecting!");
		}
		
	}

}
