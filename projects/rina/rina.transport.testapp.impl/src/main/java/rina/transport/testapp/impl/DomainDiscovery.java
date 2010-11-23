/*
 * 2010 (c) Pouzin Society
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
package rina.transport.testapp.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pouzinsociety.bootstrap.api.BootStrapCompleteAPI;
import org.pouzinsociety.bootstrap.api.BootstrapException;
import rina.config.dao.MulticastTransportConnectionDao;
import rina.config.dao.MulticastTransportDao;
import rina.transport.api.TransportConstants;
import rina.transport.api.TransportEvent;
import rina.transport.api.TransportException;
import rina.transport.api.TransportListener;
import rina.transport.api.TransportTxRx;
import rina.transport.testapp.api.DomainDao;
import rina.transport.testapp.api.DomainDiscoveryAPI;
import rina.transport.testapp.api.DomainDiscoveryConstants;

public class DomainDiscovery implements BootStrapCompleteAPI, TransportListener, DomainDiscoveryConstants, DomainDiscoveryAPI {
	static private Log log = LogFactory.getLog(DomainDiscovery.class);
	TransportTxRx transportService;
	HashMap<String,String> domains = new HashMap<String, String>();
	String myDomain;
	boolean doSearch = false;

	public void setTransportService(TransportTxRx transportService) {
		this.transportService = transportService;
	}

	public void timerFired() {
		log.debug("DomainDiscovery Timer");
		if (doSearch)
			sendEvent(getRequest());
	}
	
	private void sendEvent(TransportEvent event) {
		try {
			transportService.transmit(event);
		} catch (TransportException e) {
			log.error("Exception : " + e.getMessage() + "\n - Unable to Send DomainDiscovery Events");
		}		
	}
	
	private TransportEvent getEmpty() {
		TransportEvent event = new TransportEvent(APPLICATION_NAME);
		event.setKeyValue(TransportConstants.KEY_APPLICATION_NAME, APPLICATION_NAME);
		event.setKeyValue(TransportConstants.KEY_DEST_DOMAIN, TransportConstants.DEST_DOMAIN_EXTERNAL);
		event.setKeyValue(TransportConstants.KEY_DEST_APPLICATION_NAME, APPLICATION_NAME);
		event.setKeyValue(KEY_DOMAIN, myDomain);
		return event;
	}
	
	private TransportEvent getRequest() {
		TransportEvent event = getEmpty();
		event.setKeyValue(TransportConstants.KEY_APPLICATION_EVENT, CMD_DISCOVERY);
		return event;
	}

	private TransportEvent getResponse() {
		TransportEvent event = getEmpty();
		event.setKeyValue(TransportConstants.KEY_APPLICATION_EVENT,CMD_RESPONSE);
		return event;
	}

	public void bootstrapComplete(Object cfgDao) throws BootstrapException {
		// Should decouple even more and just ask the transportService.
		// Only looking for an "external" capable tranportService.
		try {
			MulticastTransportDao dao = (MulticastTransportDao)cfgDao;
			myDomain = dao.getDomain();
			for (MulticastTransportConnectionDao medium : dao.getMediumList()) {
				if (medium.getDomain().equals("External"))
					doSearch = true;
			}
			
			if (doSearch) {
				transportService.addEventListener(this);
			}			
		} catch (Exception e) {
			log.error("Exception: " + e.getMessage());
		}		
	}

	public String getConfigDaoClassName() {
		return MulticastTransportDao.class.getName();
	}

	public void processEvent(TransportEvent event) {
		String destApplication = event.getKeyValue(TransportConstants.KEY_APPLICATION_NAME);
		if (destApplication == null) return;
		
		if (!destApplication.equals(APPLICATION_NAME)) {
			// Not Interested
			return;
		}
		/*
		String instanceID  = event.getKeyValue(TransportConstants.KEY_INSTANCE_NAME);
		if (instanceID == null) return;
		if (!instanceID.equals(Discovery.INSTANCE_NAME)) {
			log.info("Event but not for me (" +instanceID + ")");
			return; // Not for me
		}
		
		*/
		String cmd = event.getKeyValue(TransportConstants.KEY_APPLICATION_EVENT);
		if (cmd == null) return;		// No Events ??

		// Discovery Request
		if (cmd.equals(CMD_DISCOVERY)) {
			// lets tell them about us :)
			log.info("DiscoveryResponse to " + event.getKeyValue(KEY_DOMAIN));
			sendEvent(getResponse());
			return;
		}
		
		// Discovery Response
		if (cmd.equals(CMD_RESPONSE)) {
			String remoteDomain = event.getKeyValue(KEY_DOMAIN);
			String remoteIP = event.getKeyValue(TransportConstants.KEY_SRC_IP);
			// String remoteNode = event.getKeyValue(TransportConstants.KEY_TX_NODE);
			synchronized (domains) {	
				if (domains.containsKey(remoteDomain) == true) {
					// Already know about it
					String knownIP = domains.get(remoteDomain);
					if (!knownIP.equals(remoteIP)) {
						// GATEWAY CHANGED
						domains.put(remoteDomain, remoteIP);
					}
				} else {
					domains.put(remoteDomain, remoteIP);
					log.info("Added: " +  remoteDomain + ", IP: " + remoteIP);
				}
			}
			return;
		}
		log.info("Unknown Event: " + event.toString());
	}

	// Allow a "snap" to see what current known domains.
	public List<DomainDao> getDiscoveredDomains() {
		ArrayList<DomainDao> domainList = new ArrayList<DomainDao>();
		synchronized (domains) {
			for (String key: domains.keySet())
				domainList.add(new DomainDao(key, domains.get(key)));
		}
		// TODO Auto-generated method stub
		return domainList;
	}

}
