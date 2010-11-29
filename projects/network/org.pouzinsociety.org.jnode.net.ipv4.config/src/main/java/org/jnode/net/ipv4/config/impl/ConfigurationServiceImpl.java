/*
 * $Id: ConfigurationServiceImpl.java 4214 2008-06-08 04:37:59Z crawley $
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
 *                 - moved to OSGi setup.
 *
 */
package org.jnode.net.ipv4.config.impl;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.IPv4ProtocolAddressInfo;
import org.jnode.net.ipv4.IPv4Route;
import org.jnode.net.ipv4.IPv4RoutingTable;
import org.jnode.net.ipv4.IPv4Service;
import org.jnode.net.ipv4.config.IPv4ConfigurationService;
import org.apache.commons.logging.*;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ConfigurationServiceImpl implements IPv4ConfigurationService {
	private final ConfigurationProcessor processor;
	private static final Log log = LogFactory.getLog(ConfigurationServiceImpl.class);
	private IPv4Service ipv4Service;
	private DeviceManager deviceManager;



	/**
	 * Initialize this instance.
	 * 
	 * @param config
	 */
	public ConfigurationServiceImpl() {
		this.processor = new ConfigurationProcessor();
	}
	
	public void setIpv4Service(IPv4Service ipv4Service) {
		this.ipv4Service = ipv4Service;
	}
	
	public void setDeviceManager(DeviceManager deviceManager) {
		this.deviceManager = deviceManager;
	}
	
	protected void start() {
		processor.start();
	}
	protected void stop() {
		processor.stop();
	}

	/**
	 * Set a static configuration for the given device.
	 * 
	 * @param device
	 * @param address
	 * @param netmask
	 */
	public void configureDeviceStatic(Device device, IPv4Address address,
			IPv4Address netmask, boolean persistent) throws NetworkException {
		log.debug("Calling StaticConfig : " + device.getId());
		final NetStaticDeviceConfig cfg = new NetStaticDeviceConfig(address,
				netmask);
		log.debug("Calling Apply : " + device.getId());
		processor.apply(device, cfg, false);
	}

	/**
	 * Configure the device using BOOTP.
	 * 
	 * @param device
	 * @param persistent
	 * @throws NetworkException
	 */
	public void configureDeviceBootp(Device device, boolean persistent)
			throws NetworkException {
		log.warn("configureDeviceBootp not enabled");
		// final NetBootpDeviceConfig cfg = new NetBootpDeviceConfig();
		// processor.apply(device, cfg, true);
	}

	/**
	 * Configure the device using DHCP.
	 * 
	 * @param device
	 * @param persistent
	 * @throws NetworkException
	 */
	public void configureDeviceDhcp(Device device, boolean persistent)
			throws NetworkException {
		log.warn("configureDeviceDhcp not enabled");
		// final NetDhcpConfig cfg = new NetDhcpConfig();
		// processor.apply(device, cfg, true);
	}

	/**
	 * @see org.jnode.net.ipv4.config.IPv4ConfigurationService#addRoute(org.jnode.net.ipv4.IPv4Address,
	 *      org.jnode.net.ipv4.IPv4Address, org.jnode.driver.Device, boolean)
	 */
	public void addRoute(IPv4Address target, IPv4Address netmask, IPv4Address gateway,
			Device device, boolean persistent) throws NetworkException {		
        if (device == null) {
            // Find the device ourselves
            device = findDevice(deviceManager, target, target.getDefaultSubnetmask() );
        }
        log.info(ipv4Service);
        final IPv4RoutingTable rt = ipv4Service.getRoutingTable();
        IPv4Route route = new IPv4Route(target, netmask, gateway, device);
        rt.add(route);
	}

	/**
	 * @see org.jnode.net.ipv4.config.IPv4ConfigurationService#deleteRoute(org.jnode.net.ipv4.IPv4Address,
	 *      org.jnode.net.ipv4.IPv4Address, org.jnode.driver.Device)
	 */
	public void deleteRoute(IPv4Address target, IPv4Address netmask, IPv4Address gateway,
			Device device) throws NetworkException {
        final IPv4RoutingTable rt = ipv4Service.getRoutingTable();

        for (IPv4Route route : rt.entries()) {
            if (!route.getDestination().equals(target)) {
                continue;
            }
            if (gateway != null) {
                if (!gateway.equals(route.getGateway())) {
                    continue;
                }
            }
            if (device != null) {
                if (device != route.getDevice()) {
                    continue;
                }
            }
            rt.remove(route);
            return;
        }
	}
	
	   /**
     * Search for a suitable device for the given target address.
     * 
     * @param dm
     * @param target
     * @param mask
     * @return
     * @throws NetworkException
     */
    private static Device findDevice(DeviceManager dm, IPv4Address target, IPv4Address mask)
        throws NetworkException {
        for (Device dev : dm.getDevicesByAPI(NetDeviceAPI.class)) {
            try {
                final NetDeviceAPI api = (NetDeviceAPI) dev.getAPI(NetDeviceAPI.class);
                final IPv4ProtocolAddressInfo addrInfo;
                addrInfo = (IPv4ProtocolAddressInfo) api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP);
                if (addrInfo != null) {
                    final IPv4Address devAddr = (IPv4Address) addrInfo.getDefaultAddress();
                    if (devAddr.matches(target, mask)) {
                        return dev;
                    }
                }
            } catch (ApiNotFoundException ex) {
                // Should not happen, but if it happens anyway, just ignore it.
            }
        }
        throw new NetworkException("No device found for " + target + "/" + mask);
    }

}
