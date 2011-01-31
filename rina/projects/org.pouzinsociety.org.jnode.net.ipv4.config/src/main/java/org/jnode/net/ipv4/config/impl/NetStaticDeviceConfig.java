/*
 * $Id: NetStaticDeviceConfig.java 4214 2008-06-08 04:37:59Z crawley $
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
 *                 - Removed preferences / now set via method calls.
 *
 */
package org.jnode.net.ipv4.config.impl;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.IPv4ProtocolAddressInfo;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NetStaticDeviceConfig extends NetDeviceConfig {

    private IPv4Address address;
    private IPv4Address netmask;

    /**
     * Initialize this instance.
     */
    public NetStaticDeviceConfig() {
        this(null, null);
    }

    /**
     * Initialize this instance.
     * 
     * @param address
     * @param netmask
     */
    public NetStaticDeviceConfig(IPv4Address address, IPv4Address netmask) {
        this.address = address;
        this.netmask = netmask;
    }

    /**
     * @throws NetworkException
     * @see org.jnode.net.ipv4.config.impl.NetDeviceConfig#apply(Device)
     */
    public void doApply(Device device) throws NetworkException {
        final NetDeviceAPI api;
        try {
            api = (NetDeviceAPI) device.getAPI(NetDeviceAPI.class);
        } catch (ApiNotFoundException ex) {
            throw new NetworkException("Device is not a network device", ex);
        }

        if (netmask == null) {
            netmask = address.getDefaultSubnetmask();
        }
        IPv4ProtocolAddressInfo addrInfo =
                (IPv4ProtocolAddressInfo) api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP);
        if (addrInfo == null) {
            addrInfo = new IPv4ProtocolAddressInfo(address, netmask);
            api.setProtocolAddressInfo(EthernetConstants.ETH_P_IP, addrInfo);
        } else {
            addrInfo.add(address, netmask);
            addrInfo.setDefaultAddress(address, netmask);
        }
    } 
}
