package java.net;

import java.io.IOException;

import rina.applibrary.api.SocketFactory;

public class FauxSocketFactory implements SocketFactory{

	public Socket createSocket(boolean fauxSockets, String hostname, int port) throws UnknownHostException, IOException {
		if (fauxSockets){
			return new Socket(false, hostname, port);
		}else{
			return new Socket(hostname, port);
		}
	}

	public ServerSocket createServerSocket(boolean fauxSockets, int port) throws IOException {
		if (fauxSockets){
			return new ServerSocket(port, false);
		}else{
			return new ServerSocket(port);
		}
	}

}
