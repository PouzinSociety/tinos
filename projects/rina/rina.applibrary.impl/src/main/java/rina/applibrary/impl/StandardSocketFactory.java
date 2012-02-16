package rina.applibrary.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import rina.applibrary.api.SocketFactory;

public class StandardSocketFactory implements SocketFactory{

	public synchronized Socket createSocket(boolean fauxSockets, String hostname, int port) throws UnknownHostException, IOException {
		return new Socket(hostname, port);
	}

	public synchronized ServerSocket createServerSocket(boolean fauxSockets, int port) throws IOException {
		return new ServerSocket(port);
	}

}
