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
package org.pouzinsociety.socket.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.SocketException;
import java.net.UnknownHostException;
import jnode.net.Socket;
import jnode.net.SocketImplFactory;
import org.apache.commons.logging.*;
import org.jnode.net.TransportLayer;
import org.pouzinsociety.bootstrap.api.BootStrapCompleteAPI;
import org.pouzinsociety.bootstrap.api.BootstrapException;

public class SocketClient implements Runnable,BootStrapCompleteAPI {
	private static final Log log = LogFactory.getLog(SocketClient.class);
	private TransportLayer tcpTransport;

	public SocketClient(TransportLayer tcpTransport) throws SocketException {
		this.tcpTransport = tcpTransport;
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

			SocketImplFactory sFactory = tcpTransport.getSocketImplFactory();
			Socket.setSocketImplFactory(sFactory);

			Socket kkSocket = null;
			PrintWriter out = null;
			BufferedReader in = null;

			try {
				log.info("SocketClient: Attempting connect with Server");
				kkSocket = new Socket("localhost", 4444);
				out = new PrintWriter(kkSocket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
			} catch (UnknownHostException e) {
				log.error("SocketClient: Don't know about host: localhost.");
				return;
			} catch (IOException e) {
				log.error("SocketClient: Couldn't get I/O for the connection to: localhost.");
				return;
			}

			String fromServer;
			int packetsRecv = 0;
			do {
				log.info("SocketClient: Send(Foo) to Server");
				out.println("Foo");
				while ((fromServer = in.readLine()) != null) {
					packetsRecv++;
					log.info("SocketClient: Recvd(" + packetsRecv + ") from Server : [" + fromServer + "]");
					break;
				}
				Thread.sleep(1000);
			} while(kkSocket.isConnected() && (packetsRecv < 5));
			out.close();
			in.close();
		} catch (Exception e) {
			log.error("SocketClient: Exception: " + e.getMessage());
		}
		log.info("SocketClient: Finished");
	}

}
