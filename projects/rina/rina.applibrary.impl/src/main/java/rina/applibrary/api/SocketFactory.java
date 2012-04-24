package rina.applibrary.api;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Creates the required sockets.
 * @author eduardgrasa
 *
 */
public interface SocketFactory {
	public Socket createSocket(boolean fauxSockets, String hostname, int port) throws UnknownHostException, IOException;
	public ServerSocket createServerSocket(boolean fauxSockets, int port) throws IOException;
}
