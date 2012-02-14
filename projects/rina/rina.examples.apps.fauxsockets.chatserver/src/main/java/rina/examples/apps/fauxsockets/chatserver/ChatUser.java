package rina.examples.apps.fauxsockets.chatserver;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatUser {
	
	private String userName = null;
	private PrintWriter printWriter = null;
	
	public ChatUser(String userName, Socket socket) throws Exception{
		this.userName = userName;
		this.printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);
	}
	
	public String getUserName(){
		return userName;
	}
	
	public void sendMessage(String message){
		printWriter.println(message);
	}
	
	public boolean equals(Object object){
		if (object == null){
			return false;
		}
		
		if (!(object instanceof ChatUser)){
			return false;
		}
		
		ChatUser candidate = (ChatUser) object;
		return this.userName.equals(candidate.getUserName());
	}
}
