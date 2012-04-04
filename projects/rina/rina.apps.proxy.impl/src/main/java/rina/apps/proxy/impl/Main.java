package rina.apps.proxy.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

	public static void main(String[] args){
		ExecutorService executorService = Executors.newCachedThreadPool();
		
		ProxyServer proxyServer = new ProxyServer(executorService);
		try {
			executorService.execute(proxyServer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
