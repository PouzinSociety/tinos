/*
 * $Id: VMNetworkInterface.java 2224 2006-01-01 12:49:03Z epr $
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
 *                 - java.net -> jnode.net
 */
package jnode.net;

import java.net.SocketException;
import java.util.Vector;

/**
 * This class models a network interface on the host computer. A network
 * interface contains a name (typically associated with a specific hardware
 * adapter) and a list of addresses that are bound to it. For example, an
 * ethernet interface may be named "eth0" and have the address 192.168.1.101
 * assigned to it.
 * 
 * @author Michael Koch (konqueror@gmx.de)
 * @since 1.4
 */
final class VMNetworkInterface {
    @SuppressWarnings("unchecked")
    public static Vector getInterfaces() throws SocketException {
    //    final VMNetAPI api = VMNetUtils.getAPI();
    //    final Collection<VMNetDevice> devs = api.getNetDevices();
        final Vector<NetworkInterface> intfs = new Vector<NetworkInterface>();
//        for (VMNetDevice dev : devs) {
//           final List<InetAddress> addrs = api.getInetAddresses(dev);
//           final InetAddress[] addrArr = (InetAddress[])addrs.toArray(new InetAddress[addrs.size()]);
//            // TODO re-insert next line
//           intfs.add(new NetworkInterface(dev.getId(), addrArr));
//        }        
        return intfs;
    }
}
