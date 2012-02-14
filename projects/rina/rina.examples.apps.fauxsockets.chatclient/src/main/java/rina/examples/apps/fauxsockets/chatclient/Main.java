package rina.examples.apps.fauxsockets.chatclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
	
	private static StringBuffer stringBuffer = null;
	
	public static void main(String[] args) throws Exception{
		Socket socket =  new Socket("localhost", 46743);
		PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);
		//BufferedReader bufferedReader =  new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
		
		System.out.println("Connected to the chat server. Enter your username");
		Scanner scanner = new Scanner(System.in);
		String userName = scanner.nextLine();
		printWriter.println(userName);
		String line = readLine(socket.getInputStream());
		if (line == null || line.equals("Invalid userName")){
			System.out.println(line);
			System.exit(0);
		}
		System.out.println("Enter the chat room that you want to join. You can either join an existing one or create a new one.");
		System.out.println("Available chat rooms: "+line);
		String chatRoom = scanner.nextLine();
		printWriter.println(chatRoom);
		
		System.out.println("You have joined the room "+chatRoom+". Start typing or type 'exit' to terminate the program");
		ExecutorService executorService = Executors.newCachedThreadPool();
		executorService.execute(new SocketReader(socket));
		
		while(!(line=scanner.nextLine()).equals("exit")){
			printWriter.println(line);
		}
		
		socket.close();
		System.exit(0);
	}
	
	public synchronized static String readLine(InputStream stream) throws IOException{
		if (stringBuffer == null){
			stringBuffer = new StringBuffer();
		}
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
