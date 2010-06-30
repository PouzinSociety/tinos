/*
 * $Id: IPv4Service.java 4213 2008-06-08 02:02:10Z crawley $
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

 * Modifications : Changes to port JNode code base to OSGi platform.
 *                 - Additional i/f methods due to startup changes.
 *
 */
package org.jnode.net.ipv4;

import java.net.SocketException;

import org.jnode.net.NoSuchProtocolException;
import org.jnode.net.SocketBuffer;

/**
 * @author epr
 */
public interface IPv4Service {

    /**
     * Gets the routing table
     */
    public IPv4RoutingTable getRoutingTable();

    /**
     * Transmit an IP packet.
     * The given buffer must contain all packet data AND the header(s)
     * of any IP sub-protocols, before this method is called.
     * <p>
     * The following fields of the IP header must be set:
     * tos, ttl, protocol, dstAddress.
     * <p>
     * All other header fields are set, unless they have been set before.
     * <p>
     * The following fields are always set (also when set before):
     * version, hdrlength, identification, fragmentOffset, checksum
     * <p>
     * If the device attribute of the skbuf has been set, the packet will
     * be send to this device, otherwise a suitable route will be searched
     * for in the routing table.
     * 
     * @param hdr
     * @param skbuf
     * @throws SocketException The packet cannot be transmitted
     */
    public void transmit(IPv4Header hdr, SocketBuffer skbuf) throws SocketException;

    /**
     * Gets the protocol for a given ID
     * @param protocolID
     * @throws NoSuchProtocolException No protocol with the given ID was found.
     */
    public IPv4Protocol getProtocol(int protocolID) throws NoSuchProtocolException;
    
    public void registerProtocol(IPv4Protocol protocol);
    public void unregisterProtocol(IPv4Protocol protocol);
}
