// Copyright (c) 2005 Brian Wellington (bwelling@xbill.org)

package org.xbill.glue;

import java.io.*;

import org.jnode.net.TransportLayer;
import org.xbill.glue.UDPClient;
import org.xbill.DNS.utils.hexdump;

import jnode.net.*;

final public class UDPClient {

	private DatagramSocket socket;
	private int timeout = 0;

	public UDPClient(TransportLayer transportLayer, long endTime)
			throws IOException {
		DatagramSocketImplFactory factory = transportLayer
				.getDatagramSocketImplFactory();
		DatagramSocket.setDatagramSocketImplFactory(factory);
		socket = new DatagramSocket();
		long tmp = endTime - System.currentTimeMillis();
		timeout = (tmp > 0) ? (int) tmp : 0;
		if (timeout > 0)
			socket.setSoTimeout(timeout);
	}

	private byte[] readUDP(DatagramSocket s, int max) throws IOException {
		DatagramPacket dp = new DatagramPacket(new byte[max], max);
		s.receive(dp);
		byte[] in = dp.getData();
		System.err.println(hexdump.dump("UDP read", in));
		return (in);
	}

	private void writeUDP(SocketAddress remote, DatagramSocket s, byte[] out)
			throws IOException {
		System.err.println(hexdump.dump("UDP write", out));
		DatagramPacket packet = new DatagramPacket(out, out.length, remote);
		s.send(packet);
	}

	byte[] sendAndWait(SocketAddress remote, byte[] out, int maxPacketSize)
			throws IOException {
		byte[] in;
		try {
			writeUDP(remote, socket, out);
			in = readUDP(socket, maxPacketSize);
		} finally {
			socket.close();
		}
		return in;
	}

	public static byte[] sendrecv(TransportLayer transportLayer,
			SocketAddress local, SocketAddress remote, byte[] data, int max,
			long endTime) throws IOException {
		UDPClient client = new UDPClient(transportLayer, endTime);
		return client.sendAndWait(remote, data, max);
	}

}
