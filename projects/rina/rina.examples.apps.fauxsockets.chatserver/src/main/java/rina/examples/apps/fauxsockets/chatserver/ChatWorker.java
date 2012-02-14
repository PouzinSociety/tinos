package rina.examples.apps.fauxsockets.chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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
	private ChatServer chatServer = null;
	private StringBuffer stringBuffer = null;
	
	public ChatWorker(Socket socket, ChatServer chatServer){
		this.socket = socket;
		this.chatServer = chatServer;
		this.stringBuffer = new StringBuffer();
	}

	public void run() {
		String line = null;
		String textToEcho = null;
		
		try{
			System.out.println("Chat Worker started!");
			//bufferedReader =  new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
			printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);
			userName = this.readLine(socket.getInputStream());//bufferedReader.readLine();
			if (!chatServer.isValidUser(userName)){
				printWriter.println("Invalid userName");
				socket.close();
				return;
			}else{
				printWriter.println(this.chatServer.getChatRoomNames());
			}
			chatRoom = this.readLine(socket.getInputStream());//bufferedReader.readLine();
			this.chatServer.addUserToChatRoom(userName, chatRoom, socket);
			
			while((line=this.readLine(socket.getInputStream())/*bufferedReader.readLine()*/)!=null){
				textToEcho = userName+"@"+chatRoom+": "+line;
				System.out.println("User "+userName+" at chatRoom "+chatRoom+" says: "+line);
				this.chatServer.broadcastMessage(chatRoom, textToEcho);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		System.out.println("Disconnecting!");
		this.chatServer.removeUserFromChatRoom(userName, chatRoom);
	}
	
	private String readLine(InputStream stream) throws IOException{
		stringBuffer.delete(0, stringBuffer.length());
		int i = 0;
		char c = 0;
		
		while((i = stream.read()) != -1){
			c = (char) i;
			if (c == '\n' || c == '\r'){
				return stringBuffer.toString();
			}else{
				stringBuffer.append(c);
			}
		}
		
		return null;
	}

}
