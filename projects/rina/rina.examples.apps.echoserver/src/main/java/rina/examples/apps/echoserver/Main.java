package rina.examples.apps.echoserver;

import rina.ipcservice.api.IPCException;

public class Main {

	public static void main(String[] args){
		EchoServer echoServer = new EchoServer();
		try {
			echoServer.start();
		} catch (IPCException e) {
			e.printStackTrace();
		}
	}
}
