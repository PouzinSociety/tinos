package rina.examples.apps.fauxsockets.chatclient;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
	
	public static void main(String[] args) throws Exception{
		Socket socket =  new Socket("localhost", 46743);
		System.out.println("Connected to the chat server. Enter your username");
		Scanner scanner = new Scanner(System.in);
		String userName = scanner.nextLine();
		System.out.println("Enter the chat room that you want to join");
		String chatRoom = scanner.nextLine();
		PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);
		printWriter.println(userName);
		printWriter.println(chatRoom);
		printWriter.flush();
		String line = null;
		
		System.out.println("You have joined the room "+chatRoom+". Start typing or type 'exit' to terminate the program");
		ExecutorService executorService = Executors.newCachedThreadPool();
		executorService.execute(new SocketReader(socket));
		
		while(!(line=scanner.nextLine()).equals("exit")){
			printWriter.println(line);
		}
		
		socket.close();
		System.exit(0);
	}

}
