package rina.examples.apps.fauxsockets.chatserver;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ChatServer {
	
	private List<String> userNames = null;
	private Map<String, ChatRoom> chatRooms = null;
	
	public ChatServer(){
		userNames = new ArrayList<String>();
		chatRooms = new HashMap<String, ChatRoom>();
	}
	
	public synchronized boolean isValidUser(String userName){
		if (userNames.contains(userName)){
			return false;
		}else{
			return true;
		}
	}
	
	public synchronized void addUserToChatRoom(String userName, String chatRoomName, Socket socket) throws Exception{
		ChatRoom chatRoom = chatRooms.get(chatRoomName);
		if (chatRoom == null){
			chatRoom = new ChatRoom(chatRoomName);
			chatRooms.put(chatRoomName, chatRoom);
			System.out.println("ChatServer: Added chatroom "+chatRoomName+" to the system");
		}
		
		chatRoom.addUser(new ChatUser(userName, socket));
		userNames.add(userName);
		System.out.println("ChatServer: User "+userName+" added to chatroom "+chatRoomName);
	}
	
	public synchronized void removeUserFromChatRoom(String userName, String chatRoomName){
		ChatRoom chatRoom = chatRooms.get(chatRoomName);
		if (chatRoom == null){
			return;
		}
		
		chatRoom.removeUser(userName);
		userNames.remove(userName);
		System.out.println("ChatServer: User "+userName+" removed from chatroom "+chatRoomName);
		
		if (chatRoom.getUsers() == 0){
			chatRooms.remove(chatRoom.getName());
			System.out.println("ChatServer: Chatroom "+chatRoomName+" is empty. Removed it from the system");
		}
	}
	
	public synchronized void broadcastMessage(String chatRoomName, String message){
		ChatRoom chatRoom = chatRooms.get(chatRoomName);
		if (chatRoom != null){
			chatRoom.broadcast(message);
		}
	}
	
	public String getChatRoomNames(){
		String result = "";
		Iterator<String> iterator = chatRooms.keySet().iterator();
		while (iterator.hasNext()){
			result = result + "'" + iterator.next() + "' ";
		}
		
		return result;
	}
}
