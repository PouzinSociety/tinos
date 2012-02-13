package rina.examples.apps.fauxsockets.chatserver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
	
	public static void main(String[] args){
		ExecutorService executorService = Executors.newCachedThreadPool();
		TCPServer chatServer = new TCPServer(executorService);
		executorService.execute(chatServer);
	}

}
