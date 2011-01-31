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
package org.pouzinsociety.transport.im.configmanager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.Collection;
import org.apache.commons.logging.*;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.pouzinsociety.bootstrap.api.BootstrapConstants;
import org.pouzinsociety.bootstrap.api.BootstrapEvent;
import org.pouzinsociety.config.dao.EthernetOverIMDao;
import org.pouzinsociety.config.dao.HostEntryDao;
import org.pouzinsociety.config.dao.IMDao;
import org.pouzinsociety.config.dao.RouteDao;
import org.pouzinsociety.transport.im.ConnectionImpl;


public class ControllerBuddy implements PacketListener {
	Log log = LogFactory.getLog(ControllerBuddy.class);
	IMDao imConnectionDetails;
	ConnectionImpl medium;

	// Config Data for Nodes
	HashMap<String,Integer> nodeNames = new HashMap<String,Integer>();
	List<NodeConfigDao> nodes;
	int indexCounter = 0;



	public void setNodes(List<NodeConfigDao> nodes) {
		this.nodes = nodes;
	}

	public ControllerBuddy(IMDao imConfig) throws Exception {
		imConnectionDetails = imConfig;
		medium = new ConnectionImpl();

		medium.setConfiguration(imConnectionDetails.getIm_server(),
				imConnectionDetails.getIm_port(),
				imConnectionDetails.getIm_buddyId(),
				imConnectionDetails.getIm_buddyPassword(),
				imConnectionDetails.getIm_resourceId(),
				imConnectionDetails.getIm_chatroom());
		medium.connect(this);
		log.info("Connected");
	}

	protected void finalize() throws Throwable {
		medium.disconnect();
	}


	public void processPacket(Packet packet) {
		String id = (String)packet.getProperty("BootStrap");
		if (id != null) {
			log.info("Incoming Bootstrap Event");
			BootstrapEvent event = new BootstrapEvent();
			StringBuffer buf = new StringBuffer();
			Collection<String> propertyNames = packet.getPropertyNames();
			for (Iterator<String> iter = propertyNames.iterator(); iter.hasNext();) {
				String propName = (String)iter.next();
				if (propName.equals("BootStrap")) {
					continue;
				}
				event.setKeyValue(propName, (String)packet.getProperty(propName));
				buf.append("Key(" + propName + ") V(" + (String)packet.getProperty(propName)  + ")");
			}
			log.info("Event: " + buf.toString());
			processBootstrapEvent(event);
		}
	}

	private void processBootstrapEvent(BootstrapEvent event) {
		log.info("processBootstrapEvent: (" + event.getEventId() + ")");
		if (event.getEventId() == BootstrapConstants.CONFIG_REQUEST) {

			String requester = event.getKeyValue("src");
			Integer index = nodeNames.get(requester);
			NodeConfigDao node = null;
			if (index == null) {
				synchronized (this) {
				if (indexCounter < nodes.size()) {
					node = nodes.get(indexCounter);
					nodeNames.put(requester, Integer.valueOf(indexCounter));
					indexCounter++;
				}
				}
			} else {
				node = nodes.get(index);
			}
			if (node == null) {
				log.error("No Configurations left");
				return;
			}

			BootstrapEvent response = new BootstrapEvent();
			response.setEventId(BootstrapConstants.CONFIG_RESPONSE);
			response.setKeyValue("src", imConnectionDetails.getIm_resourceId());
			response.setKeyValue("dest", requester);
			response.setKeyValue("LocalTime", getDateTime());
			response.setKeyValue("interfaces", EthernetOverIMDao.toXML(node.interfaces));
			response.setKeyValue("routes", RouteDao.toXML(node.routes));
			response.setKeyValue("hosts", HostEntryDao.toXML(node.hosts));
			sendBootstrapEvent(response);
		}

	}

	private void sendBootstrapEvent(BootstrapEvent event) {
		Message message = new Message(imConnectionDetails.getIm_chatroom(),
				Message.Type.groupchat);
		message.setBody("EventId(" + event.getEventId() + ") -> " + event.getKeyValue("dest"));
		StringBuffer buf = new StringBuffer();
		buf.append("Body: " + message.getBody() + "\n");
		for (String key : event.keySet()) {
			buf.append("Key(" + key + ")\nValue( " + event.getKeyValue(key) + ")\n");
			message.setProperty(key, event.getKeyValue(key));
		}
		log.info("Sending Configuration : \n" + buf.toString());

		try {
			medium.transmit(message);
		} catch (Exception e) {
			log.error("Exception - Cannot TX");
		}
	}

	private String getDateTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}
}
