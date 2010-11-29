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
package org.pouzinsociety.config.stack.impl;

import java.util.List;
import org.apache.commons.logging.*;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.DriverException;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.ProtocolAddress;
import org.jnode.net.Resolver;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.IPv4RoutingTable;
import org.jnode.net.ipv4.IPv4Service;
import org.jnode.net.ipv4.config.IPv4ConfigurationService;
import org.pouzinsociety.config.dao.EthernetOverIMDao;
import org.pouzinsociety.config.dao.HostEntryDao;
import org.pouzinsociety.config.dao.RouteDao;
import org.pouzinsociety.config.stack.StackConfiguration;
import org.pouzinsociety.driver.net.idrive.IDriveDevice;

public class SetupInterfaces implements StackConfiguration {
	List<EthernetOverIMDao> deviceList;
	List<HostEntryDao> hostList;
	List<RouteDao> routeList;
	private DeviceManager deviceManager;
	private IPv4ConfigurationService ipv4ConfigService;
	private IPv4Service ipv4Service;
	private Resolver hostResolverService;
	private Boolean complete = false;

	private static final Log log = LogFactory.getLog(SetupInterfaces.class);

	public SetupInterfaces() {
		routeList = null;
		deviceList = null;
		hostList = null;
	}

	private void setupHostFile() {
		if (hostList == null)
			return;

		for (HostEntryDao dao : hostList) {
			try {
				String hostName = dao.getHostname(); 
				String[] pAddrs = dao.getIpAddress();
				
				log.info("hostname(" + hostName + "," + pAddrs + ")");
				ProtocolAddress[] protocolAddresses = new ProtocolAddress[pAddrs.length];
				for (int i = 0; i < pAddrs.length; i++) {
					protocolAddresses[i] = new IPv4Address(pAddrs[i]);
				}				
				log.info("hostName(" + hostName + "," + protocolAddresses + ")");
				hostResolverService.addEntry(hostName, protocolAddresses);

			} catch (IllegalArgumentException iae) {
				log.error("Incorrectly formatted host address for hostname");
			} catch (NetworkException ne) {
				log.error("Resolver does not support addEntry");
				break;
			}
		}
	}
	private void setupDevices() {
		// Check if devices are present / stop them
		for (EthernetOverIMDao ethDevice : deviceList) {
			boolean registerDevice = false;
			try {
				Device dev = deviceManager.getDevice(ethDevice.getDevice_name());
				deviceManager.unregister(dev);
			} catch (DeviceNotFoundException dnfe) {
				registerDevice = true;
			} catch (DriverException de) {
				registerDevice = false;
				log.debug(de);
				log.error("Unable to unregister device : " + ethDevice.getDevice_name());
			}

			if (registerDevice == true) {
				try {
					deviceManager.register(new IDriveDevice(deviceManager.getSystemBus(), ethDevice));
				} catch (DeviceAlreadyRegisteredException dare) {
					log.error("Device already registered : " + ethDevice.getDevice_name());					
				} catch (DriverException de) {
					log.error("Unable to register device" + de.getMessage());
				}				
			}
		}
	}

	private void setupIP() {
		for (EthernetOverIMDao ethDevice : deviceList) {
			if (ethDevice.getIp_address() != null) {
			try {
				Device dev = deviceManager.getDevice(ethDevice.getDevice_name());
				ipv4ConfigService.configureDeviceStatic(dev, new IPv4Address(ethDevice.getIp_address()),
						new IPv4Address(ethDevice.getIp_netmask()), false);
			} catch (DeviceNotFoundException dnfe) {
				log.error("DeviceNotFound : " + ethDevice.getDevice_name());
			} catch (NetworkException ne) {
				log.error("Unable to configure : " + ethDevice.getDevice_name());
			}
			}
		}
	}

	private void setupRoutes() {
		for (RouteDao route : routeList) {
			try {
				Device dev = deviceManager.getDevice(route.getDevice());
				ipv4ConfigService.addRoute(new IPv4Address(route.getTarget()),
						(route.getNetmask() == null) ? null : new IPv4Address(route.getNetmask()),
								(route.getGateway() == null) ? null : new IPv4Address(route.getGateway()),
										dev, false);
			} catch (DeviceNotFoundException dnfe) {
				log.error("DeviceNotFound : " + route);
			} catch (NetworkException ne) {
				log.error("Unable to configure : " + route);
			}
		}
	}
	
	private void showRoutes(String heading) {
		StringBuffer buf = new StringBuffer();
		buf.append(heading + "- Route Table : \n");
		try {
			IPv4RoutingTable routeTable = ipv4Service.getRoutingTable();
			buf.append(routeTable);
		} catch (Exception e) {
			buf.append("Exception (" + e.getMessage() + ") - Unable to read Route Table");
		}
		log.info(buf.toString());
	}

	private void setupLoopback() {
		try {
			Device dev = deviceManager.getDevice("loopback");
			ipv4ConfigService.configureDeviceStatic(dev, new IPv4Address("127.0.0.1"), new IPv4Address("255.255.255.255"), false);		
			ipv4ConfigService.addRoute(new IPv4Address("127.0.0.1"), new IPv4Address("0.0.0.0"), new IPv4Address("127.0.0.1"), dev, false);
		} catch (DeviceNotFoundException dnfe) {
			log.debug(dnfe);
			log.error("DeviceNotFound : loopback");
		} catch (NetworkException ne) {
			log.debug(ne);
			log.error("Unable to configure : loopback");
		}
	}

	private void showInterfaces(String heading) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(heading + " : \n");
		try {
			for (Device dev : deviceManager.getDevicesByAPI(NetDeviceAPI.class)) {
				final NetDeviceAPI api = (NetDeviceAPI) dev.getAPI(NetDeviceAPI.class);
				buffer.append("Device:\n\t"
						+ dev.getId()
						+ ": MAC-Address " + api.getAddress()
						+ " MTU "+ api.getMTU()
						+ "\n\t\t"
						+ api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP) + "\n");
			}
		} catch (Exception e) {
			buffer.append("Exception occurred : " + e.getMessage());
		}
		log.info(buffer.toString());		
	}

	public void execute() {
		synchronized (deviceList) {
			complete = false;
			setupHostFile();
			showInterfaces("Before");
			setupLoopback();
			showInterfaces("After Loopback");
			setupDevices();
			showInterfaces("After Devices");
			setupIP();
			showInterfaces("After SetupIP");
			showRoutes("Before Route Setup");
			setupRoutes();
			showRoutes("After Route Setup");
			
			try {
			Thread.sleep(2000);
			} catch (Exception e) {			
			}
			showInterfaces("ConfigComplete");
			showRoutes("ConfigComplete");
			complete = true;
		}
	}

	public void setDeviceManager(DeviceManager deviceManager) {
		this.deviceManager = deviceManager;
	}
	public void setIpv4ConfigService(IPv4ConfigurationService ipv4ConfigService) {
		this.ipv4ConfigService = ipv4ConfigService;
	}
	public List<EthernetOverIMDao> getDeviceList() {
		return deviceList;
	}
	public void setDeviceList(List<EthernetOverIMDao> deviceList) {
		this.deviceList = deviceList;
	}
	public void setHostList(List<HostEntryDao> hostList) {
		this.hostList = hostList;
	}

	public void setHostResolverService(Resolver hostResolverService) {
		this.hostResolverService = hostResolverService;
	}
	
	public void setIpv4Service(IPv4Service ipv4Service) {
		this.ipv4Service = ipv4Service;
	}
	
	public void setRouteList(List<RouteDao> routeList) {
		this.routeList = routeList;
	}

	public String getMessage() {
		return "Static Configuration of Node Interfaces";
	}

	public Boolean Complete() {
		// TODO Auto-generated method stub
		return complete;
	}
}
