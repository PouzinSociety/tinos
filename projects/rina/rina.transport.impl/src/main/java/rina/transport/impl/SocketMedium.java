package rina.transport.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import jnode.net.DatagramPacket;
import jnode.net.InetAddress;
import jnode.net.InetSocketAddress;
import jnode.net.MulticastSocket;
import jnode.net.NetworkInterface;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import rina.config.dao.MulticastTransportConnectionDao;
import rina.transport.api.TransportConstants;
import rina.transport.api.TransportEvent;

public class SocketMedium implements Runnable {
	private static final Log log = LogFactory.getLog(SocketMedium.class);
	MulticastTransportConnectionDao dao;

	private Boolean stopFlag = false;
	private MulticastSocket socket;
	private InetAddress group;
	private int groupPort;
	private SocketMediumCallback callback;

	@SuppressWarnings("unchecked")
	public SocketMedium(MulticastTransportConnectionDao dao, SocketMediumCallback callback) throws Exception {
		this.dao = dao;
		this.callback = callback;

		// Setup the Socket
    	group = InetAddress.getByName(dao.getMulticastAddress());
    	groupPort = Integer.parseInt(dao.getMulticastPort());
    	NetworkInterface netif = NetworkInterface.getByName(dao.getNetworkInterface());
    	Enumeration<InetAddress> ipAddrs = netif.getInetAddresses();
    	InetAddress interfaceAddr = ipAddrs.nextElement();
    	log.info("InterfaceAddr: " + interfaceAddr.toString());
    	InetSocketAddress sockAddr = new InetSocketAddress(interfaceAddr, groupPort);
    	
		socket = new MulticastSocket(sockAddr);
		socket.setNetworkInterface(netif);
		socket.joinGroup(group);
		
		// RECV Loop in another Thread
		new Thread(this).start();
	}
	
	public void run() {
		log.info(this.getClass().toString() + " :RECV Loop Started ");
		try {
// RECV			
			byte[] inputBuffer = new byte[1024];
			DatagramPacket recvPkt;
			Boolean localStopFlag;
			synchronized (stopFlag) {
				localStopFlag = stopFlag;
			}
			while (localStopFlag != true) {
				recvPkt = new DatagramPacket(inputBuffer, inputBuffer.length);
				socket.receive(recvPkt);				
				ByteArrayInputStream bytesIn = new ByteArrayInputStream(recvPkt.getData(),
						recvPkt.getOffset(), recvPkt.getLength());
				ObjectInputStream objIn = new ObjectInputStream(bytesIn);				
				Object obj = objIn.readObject();
				TransportEvent event = (TransportEvent)obj;
				
				event.setKeyValue(TransportConstants.KEY_SRC_DOMAIN, dao.getDomain());
				event.setKeyValue(TransportConstants.KEY_RX_NODE, dao.getNode());
				event.setKeyValue(TransportConstants.KEY_RX_TIME, getDateTime());
				event.setKeyValue(TransportConstants.KEY_SRC_IP, recvPkt.getAddress().getHostAddress());
				
				callback.receive(event);
				
				synchronized (stopFlag) {
					localStopFlag = stopFlag;
				}
			}
//RECV
			socket.leaveGroup(group);
			socket.close();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		log.info(this.getClass().toString() + " : Finished ");
	}
	
	public void close() {
		synchronized (stopFlag) {
			stopFlag = true;
		}
		try {
			socket.leaveGroup(group);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		socket.close();
	}
	
	private String getDateTime() {
		return (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(new Date());
	}
	
	public void write(TransportEvent packet) throws Exception {
		
			packet.setKeyValue(TransportConstants.KEY_TX_TIME, getDateTime());
			packet.setKeyValue(TransportConstants.KEY_TX_NODE, dao.getNode());
			packet.setKeyValue(TransportConstants.KEY_TX_DOMAIN, dao.getDomain());
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			ObjectOutputStream oMessageOut = new ObjectOutputStream(outBytes);
			oMessageOut.writeObject(packet);
			oMessageOut.flush();
			oMessageOut.close();
			outBytes.flush();
			byte[] sendBuffer = outBytes.toByteArray();
			DatagramPacket sendPkt = new DatagramPacket(sendBuffer, sendBuffer.length,
					group, groupPort);
			
			socket.send(sendPkt);
			log.info("MulticastTransport-Tx: " + packet.toString());
	}
}
