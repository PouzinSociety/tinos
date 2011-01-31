// Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)

package org.xbill.glue;

import java.io.*;
import jnode.net.*;

import org.jnode.net.TransportLayer;
import org.xbill.glue.TCPClient;
import org.xbill.DNS.utils.hexdump;

public final class TCPClient {
	Socket socket;
	int timeout = 0;

	public TCPClient(TransportLayer transportLayer, long endTime) throws IOException {
		SocketImplFactory factory = transportLayer.getSocketImplFactory();
		Socket.setSocketImplFactory(factory);
		socket = new Socket();
		
		long tmp = endTime - System.currentTimeMillis();
		timeout = (tmp > 0) ? (int)tmp : 0;
		if (timeout > 0)
			socket.setSoTimeout((int) timeout);
	}
	
	// needed : ZoneTransferIn
	public void
	bind(SocketAddress addr) throws IOException {
		// Do Nothing for the moment.
	}
	// needed : ZoneTransferIn
	public void connect(SocketAddress addr) throws IOException {
		socket.connect(addr);
	}
	// needed : ZoneTransferIn
	public void
	send(byte [] data) throws IOException {
		writeTCP(socket, data);
	}
	// needed : ZoneTransferIn
	public byte[]
	recv() throws IOException {
		return readTCP(socket);
	}
	// needed : ZoneTransferIn
	public void cleanup() throws IOException {
		socket.close();
	}

	private byte[] readTCP(Socket s) throws IOException {
		DataInputStream dataIn;

		dataIn = new DataInputStream(s.getInputStream());
		int inLength = dataIn.readUnsignedShort();
		byte[] in = new byte[inLength];
		dataIn.readFully(in);
		
		System.err.println(hexdump.dump("TCP read", in));
		return (in);
	}

	private void writeTCP(Socket s, byte[] out) throws IOException {
		System.err.println(hexdump.dump("TCP write", out));
		OutputStream outStream = s.getOutputStream();
		byte[] lengthArray = new byte[2];
		lengthArray[0] = (byte) (out.length >>> 8);
		lengthArray[1] = (byte) (out.length & 0xFF);
		outStream.write(lengthArray);
		outStream.write(out);
		//pphelan
		outStream.flush();
	}
	
	

	byte[] sendAndWait(SocketAddress remote, byte[] out) throws IOException {
		byte[] in;
		connect(remote);	
		try {
			writeTCP(socket, out);
			in = readTCP(socket);
		}
		finally {
			socket.close();
		}
		return in;
	}

	static public byte[] sendrecv(TransportLayer transportLayer, SocketAddress local, SocketAddress remote,
			byte[] data, long endTime) throws IOException {
		TCPClient client = new TCPClient(transportLayer, endTime);
		return client.sendAndWait(remote, data);
	}

}
