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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jnode.net.TransportLayer;
import org.pouzinsociety.bootstrap.api.BootStrapCompleteAPI;
import org.pouzinsociety.bootstrap.api.BootstrapException;
import jnode.net.ServerSocket;
import jnode.net.Socket;
import jnode.net.SocketImplFactory;

public class SocketServer implements BootStrapCompleteAPI,Runnable {
	private static final Log log = LogFactory.getLog(SocketServer.class);
	private TransportLayer tcpTransport;

	public SocketServer(TransportLayer tcpTransport) throws SocketException {
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
		log.info("SocketServer : Started ");
		try {
	    	SocketImplFactory sFactory = tcpTransport.getSocketImplFactory();
			Socket.setSocketImplFactory(sFactory);
			ServerSocket.setSocketFactory(sFactory);
			
			
			ServerSocket serverSocket = new ServerSocket(4444);
			log.info("Opened Server");
			Socket clientSocket = null;
			try {
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				log.error("Accept failed." + e.getMessage());
				serverSocket.close();
				return;
			}
			log.info("clientSocket:" + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			String fromClient;
			log.info("Entering recv loop with Client");
			int packetsRecv = 0;
			while ((fromClient = in.readLine()) != null) {
				packetsRecv++;
				log.info("Recvd(" + packetsRecv + ") from Client : [" + fromClient + "]");
				out.println("Bar");
				log.info("Send(Bar) to Client");
			}
			out.close();
			in.close();
			clientSocket.close();
			serverSocket.close();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		log.info("SocketServer : Finished ");
	}
}
