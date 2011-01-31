/*
 * $Id: ResolverImpl.java 4215 2008-06-08 05:47:07Z crawley $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * Modifications : Changes to port JNode code base to OSGi platform.
 *                 - Rewrite of Resolver to be simple Host file implementation.
 *
 */
package org.jnode.net.ipv4.resolver;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.*;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.ProtocolAddress;
import org.jnode.net.Resolver;
import org.jnode.net.ipv4.IPv4Address;


/**
 * @author Martin Hartvig
 */
public class ResolverImpl implements Resolver {
	private Map<String, ProtocolAddress[]> hosts = new HashMap<String, ProtocolAddress[]>();
	private Map<String, String[]> rhosts = new HashMap<String, String[]>();
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(ResolverImpl.class);

	ResolverImpl() {
		synchronized (hosts) {
			hosts.put("localhost", new ProtocolAddress[] {new IPv4Address("127.0.0.1")});
			rhosts.put("127.0.0.1", new String[] { "localhost" });
		}
	}

	public void addEntry(String hostName, ProtocolAddress[] protocolAddresses)
	throws NetworkException {
		synchronized (hosts) {
			hosts.put(hostName, protocolAddresses);
			
			for (ProtocolAddress ad : protocolAddresses) {
				String pAddr = ad.toString();
				String[] currentArray = rhosts.get(pAddr);
				if (currentArray == null) {
					rhosts.put(pAddr, new String[] { hostName });
				} else {
					String[] newArray = new String[currentArray.length + 1];
					newArray = currentArray.clone();
					newArray[currentArray.length] = hostName;
					rhosts.put(pAddr, newArray);
				}
			}
		}
		/**
		StringBuffer buf = new StringBuffer();
		for (String ipAddr : rhosts.keySet()) {
			buf.append("Entry: " + ipAddr + " - (");
			for (String hostname : rhosts.get(ipAddr)){
				buf.append(hostname + ",");
			}
			buf.append(")");
		}
		log.info(buf.toString());
		*/
	}
	
	/**
	 * Get from hosts file.
	 * 
	 * @param _hostname
	 * @return
	 */
	private ProtocolAddress[] getFromHostsFile(String hostname) {
		ProtocolAddress[] lookup;
		synchronized (hosts) {
			lookup = hosts.get(hostname);
		}
		return lookup;
	}

	/**
	 * Gets the address(es) of the given hostname.
	 * 
	 * @param hostname
	 * @return All addresses of the given hostname. The returned array is at
	 *         least 1 address long.
	 * @throws java.net.UnknownHostException
	 */
	public ProtocolAddress[] getByName(final String hostname) throws UnknownHostException {
		if (hostname == null) {
			throw new UnknownHostException("null");
		}
		if (hostname.equals("*")) {
			// FIXME ... why is this a special case? Comment please or fix it.
			throw new UnknownHostException("*");
		}
		ProtocolAddress[] protocolAddresses = getFromHostsFile(hostname);
		if (protocolAddresses != null) {
			return protocolAddresses;
		}
		throw new UnknownHostException(hostname);
	}

	/**
	 * Gets the hostname of the given address.
	 * 
	 * @param address
	 * @return All hostnames of the given hostname. The returned array is at
	 *         least 1 hostname long.
	 * @throws java.net.UnknownHostException
	 */

	public String[] getByAddress(ProtocolAddress address) throws UnknownHostException {
		String[] lookup;
		synchronized (hosts) {
			lookup = rhosts.get(address.toString());
		}		
		if (lookup != null)
			return lookup;
		
		throw new UnknownHostException(address.toString());
	}


}
