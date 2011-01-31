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
package org.pouzinsociety.bootstrap.driver;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.*;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jnode.driver.DriverException;
import org.jnode.driver.net.NetworkException;
import org.jnode.driver.net.spi.AbstractNetDriver;
import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetAddress;
import org.pouzinsociety.bootstrap.api.BootStrapAPI;
import org.pouzinsociety.bootstrap.api.BootstrapEvent;
import org.pouzinsociety.bootstrap.api.BootstrapEventListener;
import org.pouzinsociety.bootstrap.api.BootstrapException;
import org.pouzinsociety.config.dao.IMDao;
import org.pouzinsociety.transport.im.ConnectionImpl;


public class BootstrapDriver extends AbstractNetDriver implements BootStrapAPI, PacketListener {
	private Log log = LogFactory.getLog(BootstrapDriver.class);
	private BootstrapEventProcessor eventProcessor;
	private IMDao dao;
	private ConnectionImpl networkMedium = new ConnectionImpl();
	private String resourceId, hostname;

	public BootstrapDriver(IMDao deviceDao) throws DriverException {
		super();
		dao = deviceDao;

		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			throw new DriverException("Cannot determine localhost");
		}

		resourceId = hostname + "-" + (new Long((Calendar.getInstance()).getTimeInMillis())).toString();
		try {
			networkMedium.setConfiguration(dao.getIm_server(), dao.getIm_port(), 
					dao.getIm_buddyId(), dao.getIm_buddyPassword(),
					resourceId, dao.getIm_chatroom());
			networkMedium.connect(this);
			log.debug("Connected with " + resourceId);
		} catch(Exception e) {
			throw new DriverException("Connection with (" + resourceId + ") failed");
		}
	}


	protected final void doTransmit(SocketBuffer skbuf,
			HardwareAddress destination) throws NetworkException {
	}

	/**
	 * @see org.jnode.driver.net.spi.AbstractNetDriver#getDevicePrefix()
	 */
	protected String getDevicePrefix() {
		return "bootstrap";
	}

	public void processPacket(Packet packet){
		String id = (String)packet.getProperty("BootStrap");
		if (id == null) {
			log.debug("RECV : Not a bootStrap Packet");
			return;
		}
		BootstrapEvent event = new BootstrapEvent();
		Collection<String> propertyNames = packet.getPropertyNames();
		for (Iterator<String> iter = propertyNames.iterator(); iter.hasNext();) {
			String propName = (String)iter.next();
			if (propName.equals("BootStrap")) {
				continue;
			}
			event.setKeyValue(propName, (String)packet.getProperty(propName));
		}
		try {
			if (!event.getKeyValue("src").equals(resourceId))
				eventProcessor.process(event);
		} catch (Exception e) {
			log.debug("Event Processor Exception : " + e.getMessage());
		}

	}

	public HardwareAddress getAddress() {
		return new EthernetAddress("BA-D0-00-00-BA-BE");
	}

	public int getMTU() {
		return 0; // Deliberately make us un-usable by IP stack
	}

	protected boolean renameToDevicePrefixOnly() {
		return true;
	}

	public void addEventListener(BootstrapEventListener listener) {
		BootstrapEventProcessor proc = this.eventProcessor;
		if (proc == null) {
			this.eventProcessor = proc = new BootstrapEventProcessor();
		}
		proc.addEventListener(listener);

	}

	public void removeEventListener(BootstrapEventListener listener) {
		final BootstrapEventProcessor proc = this.eventProcessor;
		if (proc != null) {
			proc.removeEventListener(listener);
		}
	}

	public void transmit(BootstrapEvent event) throws BootstrapException {
		Message message = new Message(dao.getIm_chatroom(), Message.Type.groupchat);
		message.setBody("EventId(" + event.getEventId() + ") -> All");
		event.setKeyValue("src", resourceId);
		for (String key : event.keySet()) {
			message.setProperty(key, event.getKeyValue(key));
		}
		try {
			networkMedium.transmit(message);
		} catch (Exception e) {
			throw new BootstrapException("Unable to Tx", e);
		}
	}

	public String getInstanceId() {
		return resourceId;
	}
}
