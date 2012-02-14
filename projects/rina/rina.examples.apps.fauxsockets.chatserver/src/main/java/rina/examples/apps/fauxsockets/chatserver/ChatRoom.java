package rina.examples.apps.fauxsockets.chatserver;

import java.util.ArrayList;
import java.util.List;

public class ChatRoom {
	
	private String name = null;
	private List<ChatUser> users = null;
	
	public ChatRoom(String name){
		this.name = name;
		this.users = new ArrayList<ChatUser>();
	}
	
	public String getName(){
		return this.name;
	}
	
	public void addUser(ChatUser chatUser){
		if (users.contains(chatUser)){
			return;
		}
		
		users.add(chatUser);
	}
	
	public void removeUser(String userName){
		for(int i=0; i<users.size(); i++){
			if (users.get(i).getUserName().equals(userName)){
				users.remove(i);
				return;
			}
		}
	}
	
	public void broadcast(String message){
		for(int i=0; i<users.size(); i++){
			users.get(i).sendMessage(message);
		}
	}
	
	public int getUsers(){
		return users.size();
	}
	
	public boolean equals(Object object){
		if (object == null){
			return false;
		}
		
		if (!(object instanceof ChatRoom)){
			return false;
		}
		
		ChatRoom candidate = (ChatRoom) object;
		return this.name.equals(candidate.getName());
	}

}
