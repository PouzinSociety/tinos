package rina.examples.apps.echoserver.test;

import rina.examples.apps.echoserver.EchoServer;
import rina.ipcservice.api.IPCException;

public class EchoServerRunner implements Runnable{

	private EchoServer echoServer = null;
	
	public void run() {
		echoServer = new EchoServer();
		
		try{
			echoServer.start();
		}catch(IPCException ex){
			ex.printStackTrace();
		}	
	}
	
	public void stop(){
		try{
			echoServer.stop();
		}catch(IPCException ex){
			ex.printStackTrace();
		}
	}

}
