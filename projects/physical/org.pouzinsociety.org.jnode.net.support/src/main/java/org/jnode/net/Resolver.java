/*
 * $Id: Resolver.java 4213 2008-06-08 02:02:10Z crawley $
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
 */
package org.jnode.net;

import java.net.UnknownHostException;
import org.jnode.driver.net.NetworkException;

/**
 * @author epr
 */
public interface Resolver {

    /**
     * Gets the address(es) of the given hostname.
     * 
     * @param hostname
     * @return All addresses of the given hostname. The returned array is at
     *         least 1 address long.
     * @throws UnknownHostException
     */
    public ProtocolAddress[] getByName(String hostname) throws UnknownHostException;

    /**
     * Gets the hostname of the given address.
     * 
     * @param address
     * @return All hostnames of the given hostname. The returned array is at
     *         least 1 hostname long.
     * @throws UnknownHostException
     */
    public String[] getByAddress(ProtocolAddress address) throws UnknownHostException;
    
    /**
     * Sets a Hostname / ProtocolAddress Entry.
     * 
     * @param hostname, protocoladdresses[]
     *
     * @throws NetworkException - if not supported.
     */    
    public void addEntry(String hostName, ProtocolAddress[] protocolAddresses) throws NetworkException;

}
