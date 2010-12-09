/*
 * 2008 - 2010 (c) Waterford Institute of Technology
 *		   TSSG, EU ICT 4WARD
 *
 * 2010 (c) Pouzin Society
 *   - Forked from EU ICT 4WARD Open Source Distribution.
 *   - Organisation Strings updated to reflect fork.
 *
 *
 * Author        : pphelan(at)tssg.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.pouzinsociety.socket.udp;

import java.net.SocketException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jnode.net.TransportLayer;
import org.pouzinsociety.bootstrap.api.BootStrapCompleteAPI;
import org.pouzinsociety.bootstrap.api.BootstrapException;
import jnode.net.DatagramPacket;
import jnode.net.DatagramSocket;
import jnode.net.DatagramSocketImplFactory;
import jnode.net.InetAddress;
import jnode.net.PlainDatagramSocketImpl;
import jnode.net.PlainDatagramSocketImplFactory;

public class SocketServer implements Runnable,BootStrapCompleteAPI {
	private static final Log log = LogFactory.getLog(SocketServer.class);
	private TransportLayer udpTransport;
	private String response = "BEEF";

	public SocketServer(TransportLayer udpTransport) throws SocketException {
		this.udpTransport = udpTransport;
	}

	public void bootstrapComplete(Object arg0) throws BootstrapException {
		log.debug("BootStrapComplete()");
		new Thread(this).start();
	}
	public String getConfigDaoClassName() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void run() {
		log.info("SocketServer : Started ");
		try {
		    DatagramSocketImplFactory sFactory = udpTransport.getDatagramSocketImplFactory();
	        DatagramSocket.setDatagramSocketImplFactory(sFactory);
	        PlainDatagramSocketImpl.setUDPFactory((PlainDatagramSocketImplFactory)sFactory);
			
			DatagramSocket serverSocket = new DatagramSocket(3333);
			log.info("SocketServer: Opened Server");
			
			byte buf[] = new byte[256];
			DatagramPacket packet;
			log.info("SocketServer: Entering recv loop with Client");
			int packetsRecv = 0;
			do {
					packet = new DatagramPacket(buf, buf.length);
					serverSocket.receive(packet);
					packetsRecv++;
					String recv = new String(packet.getData());
					log.info("SocketServer: Recv(" + packetsRecv + ") from Client : [ " + recv + "]");
					
					InetAddress address = packet.getAddress();
					int port = packet.getPort();
					
					buf = response.getBytes();
					packet = new DatagramPacket(buf, buf.length, address, port);
					serverSocket.send(packet);
					
					log.info("SocketServer: Send(" + response + ") to Client");
				
			} while (packetsRecv < 5);
			serverSocket.close();
			
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		log.info("SocketServer : Finished ");
	}
}
