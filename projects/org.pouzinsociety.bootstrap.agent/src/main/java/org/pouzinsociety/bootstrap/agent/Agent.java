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
package org.pouzinsociety.bootstrap.agent;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pouzinsociety.bootstrap.api.BootStrapAPI;
import org.pouzinsociety.bootstrap.api.BootstrapConstants;
import org.pouzinsociety.bootstrap.api.BootstrapEvent;
import org.pouzinsociety.bootstrap.api.BootstrapEventListener;
import org.pouzinsociety.bootstrap.api.BootstrapException;
import org.pouzinsociety.config.dao.EthernetOverIMDao;
import org.pouzinsociety.config.dao.HostEntryDao;
import org.pouzinsociety.config.dao.RouteDao;
import org.pouzinsociety.config.stack.impl.SetupInterfaces;
import java.util.Date;
import java.text.SimpleDateFormat;

public class Agent implements BootstrapEventListener {
	private static Log log = LogFactory.getLog(Agent.class);
	private BootStrapAPI medium;
	private SetupInterfaces setupService;
	private boolean nodeConfigured = false;

	public void setMedium(BootStrapAPI medium) {
		this.medium = medium;
		medium.addEventListener(this);
	}

	public void setSetupService(SetupInterfaces setupService) {
		this.setupService = setupService;
	}

	private String getDateTime() {
		return (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(new Date());
	}

	public void timerFired() {
		if (nodeConfigured == true) // Nothing to do
			return;
		log.debug("(" + medium.getInstanceId() + ") Requesting Configuration");
		try {
			BootstrapEvent event = new BootstrapEvent();
			event.setEventId(BootstrapConstants.CONFIG_REQUEST);
			event.setKeyValue("dest", "ConfigurationService");
			event.setKeyValue("LocalTime", getDateTime());
			medium.transmit(event);
		} catch (BootstrapException e) {
			log.error("Exception : " + e.getMessage() + "\n - Unable to Send CONFIG_REQUEST");
		}
	}

	public void processEvent(BootstrapEvent event) {
		log.debug("<AgentRECV>\n");
		if (event.getEventId() == BootstrapConstants.CONFIG_RESPONSE &&
			event.getKeyValue("dest").equals(medium.getInstanceId())) {

			nodeConfigured = true;

			Set<String> keys = event.keySet(); StringBuffer buf = new StringBuffer();
			for (String key : keys)
				buf.append("Key(" + key + ")Value(" + event.getKeyValue(key) + ")\n");
			log.debug("Received ConfigResponse: " + buf.toString());

			try {
				List<EthernetOverIMDao> interfaces = EthernetOverIMDao.fromXML(event.getKeyValue("interfaces"));
				List<RouteDao> routes = RouteDao.fromXML(event.getKeyValue("routes"));
				List<HostEntryDao> hosts = HostEntryDao.fromXML(event.getKeyValue("hosts"));		
				setupService.setDeviceList(interfaces);
				setupService.setRouteList(routes);
				setupService.setHostList(hosts);
				setupService.execute();
				nodeConfigured = true;
			} catch (Exception e) {	
				nodeConfigured = false;  // Try again
				log.error(e.getStackTrace());
				log.error("Exception : " + e.getMessage() + e);
			}
		}
		log.debug("</AgentRECV>\n");
	}
}
