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
package org.pouzinsociety.transport.test;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.*;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.pouzinsociety.transport.im.ConnectionImpl;

public class TestClientServer implements Runnable, PacketListener {
	Log log = LogFactory.getLog(TestClientServer.class);
	ConnectionImpl server, client;
	
	public TestClientServer() {
		server = new ConnectionImpl();
		client = new ConnectionImpl();
		log.info("Constructor");
	}
	
	public void setConfiguration(String[] config) {
		server.setConfiguration(config[0], config[1], config[2], config[3], config[4], config[5]);
	}
	public void setConfigurationTwo(String[] config) {
		client.setConfiguration(config[0], config[1], config[2], config[3], config[4], config[5]);
	}

	
	public void run() {
		log.info("hello");
		try {
			log.info("Connecting");
			server.connect(this);
			client.connect(this);
			byte[] testArray = { (byte)0x00, (byte)0x00, (byte) 0xDE, (byte)0xAD, (byte)0xBE, (byte)0xEF, (byte)0x00, (byte)0x00 };
			for (int i = 0; i < 5; i++) {
			server.transmit(testArray);
			client.transmit("client: hello".getBytes());
			Thread.sleep(500);
			}
			
			log.info("Disconnect");
			server.disconnect();
			client.disconnect();
		} catch (Exception e) {
			log.error(e);
		}
	}

	public void processPacket(Packet packet) {
		// TODO Auto-generated method stub
		log.info("Packet Recv");
		Message msg = (Message)packet;
		Collection<String> props  = msg.getPropertyNames();
		for (Iterator<String> keys = props.iterator(); keys.hasNext(); ) {
			String key = keys.next();
			log.info("Key|Value [" + key + "|" + msg.getProperty(key) + "]");	
		}
	}
	
}
