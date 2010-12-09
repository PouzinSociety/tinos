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
import jnode.net.DatagramPacket;
import jnode.net.DatagramSocket;
import jnode.net.DatagramSocketImplFactory;
import jnode.net.InetAddress;
import jnode.net.PlainDatagramSocketImpl;
import jnode.net.PlainDatagramSocketImplFactory;
import org.apache.commons.logging.*;
import org.jnode.net.TransportLayer;
import org.pouzinsociety.bootstrap.api.BootStrapCompleteAPI;
import org.pouzinsociety.bootstrap.api.BootstrapException;


public class SocketClient implements Runnable, BootStrapCompleteAPI {
	private static final Log log = LogFactory.getLog(SocketClient.class);
	private TransportLayer udpTransport;
	private String request = "DEAD";


	public SocketClient(TransportLayer udpTransport) throws SocketException {
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
		log.info("SocketClient: Started");	    	
		try {
			log.info("SocketClient: Sleeping for 1000ms to allow ServerStart");
			Thread.sleep(1000);
			log.info("SocketClient: Waking Up for work");

			 DatagramSocketImplFactory sFactory = udpTransport.getDatagramSocketImplFactory();
			
		     DatagramSocket.setDatagramSocketImplFactory(sFactory);
		     PlainDatagramSocketImpl.setUDPFactory((PlainDatagramSocketImplFactory)sFactory);
				
		     DatagramSocket socket = new DatagramSocket();
		     
		     byte buf[];
		     
		     InetAddress address = InetAddress.getByName("localhost");
		     int packetsRecv = 0;
		     do {
		    	 log.info("SocketClient: send(" + request + ") to Server");
		    	 buf = request.getBytes();
		    	 DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 3333);
		    	 socket.send(packet);
		    	 buf = new byte[256];
		    	 packet = new DatagramPacket(buf, buf.length);
		    	 socket.receive(packet);
		     
		    	 packetsRecv++;
		    	 String response = new String(packet.getData());    
		    	 log.info("SocketClient: recv(" + packetsRecv + ")[" + response + "] from Server");
		     } while(packetsRecv < 5);
		   socket.close();
		} catch (Exception e) {
			log.error("SocketClient: Exception: " + e.getMessage());
		}
		log.info("SocketClient: Finished");
	}

}
