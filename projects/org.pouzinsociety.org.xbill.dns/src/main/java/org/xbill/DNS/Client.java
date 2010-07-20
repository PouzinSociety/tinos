// Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)

package org.xbill.DNS;

import java.io.IOException;

import jnode.net.Socket;
import jnode.net.SocketAddress;
import jnode.net.SocketImplFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jnode.net.TransportLayer;

class Client {
	protected Socket socket = null;
	protected int endTime = 0;
	
	/**
     * My logger
     */
    private static final Log log = LogFactory.getLog(Client.class);

	protected Client(int endTime, TransportLayer transportLayer){
		this.endTime = endTime;
		try{
			SocketImplFactory sFactory = transportLayer.getSocketImplFactory();
			Socket.setSocketImplFactory(sFactory);
			socket = new Socket();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	void bind(SocketAddress addr) throws IOException {
		socket.bind(addr);
	}

	void
	connect(SocketAddress addr) throws IOException {
		socket.connect(addr,endTime);
	}

	void
	send(byte [] data) throws IOException {
		log.debug("Write" + data);
		socket.getOutputStream().write(data);
	}

	private byte []
	              _recv(int length) throws IOException {
		byte [] data = new byte[length];
		for(int i=0; i<length; i++){
			data[i] = (byte) socket.getInputStream().read();
		}
		return data;
	}

	byte []
	      recv() throws IOException {
		byte [] buf = _recv(2);
		int length = ((buf[0] & 0xFF) << 8) + (buf[1] & 0xFF);
		byte [] data = _recv(length);
		log.debug("Read"+ data);
		return data;
	}

	static byte []
	             sendrecv(SocketAddress local, SocketAddress remote, byte [] data, int endTime, 
	            		 TransportLayer transportLayer)
	throws IOException
	{
		Client client = new Client(endTime, transportLayer);
		try {
			if (local != null)
				client.bind(local);
			client.connect(remote);
			client.send(data);
			return client.recv();
		}
		finally {
			client.cleanup();
		}
	}

	static byte []
	             sendrecv(SocketAddress addr, byte [] data, int endTime, 
	            		 TransportLayer transportLayer) throws IOException {
		return sendrecv(null, addr, data, endTime, transportLayer);
	}

	void
	cleanup() throws IOException {
		socket.close();
	}

}
