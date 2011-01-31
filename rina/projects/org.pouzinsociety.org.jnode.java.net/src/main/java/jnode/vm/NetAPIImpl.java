/*
 * $Id: NetAPIImpl.java 4215 2008-06-08 05:47:07Z crawley $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
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
 
package jnode.vm;

import jnode.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import jnode.net.VMNetAPI;
import jnode.net.VMNetDevice;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.*;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.net.NetworkLayer;
import org.jnode.net.NetworkLayerManager;
import org.jnode.net.ProtocolAddress;
import org.jnode.net.ProtocolAddressInfo;
import org.jnode.net.ethernet.EthernetConstants;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NetAPIImpl implements VMNetAPI {

    private static NetworkLayerManager networkLayerManager;
    private static DeviceManager devManager;
    private static Log log = LogFactory.getLog(NetAPIImpl.class);

    public void setNetworkLayerManager(
			NetworkLayerManager networkLayerManager) {
		NetAPIImpl.networkLayerManager = networkLayerManager;
	}

	public void setDevManager(DeviceManager devManager) {
		NetAPIImpl.devManager = devManager;
	}

	/**
     * @see java.net.VMNetAPI#getInetAddresses(VMNetDevice)
     */
    public List<InetAddress> getInetAddresses(VMNetDevice netDevice) {
        final ArrayList<InetAddress> list = new ArrayList<InetAddress>();
        try {
        	final Device device = devManager.getDevice(netDevice.getId());
        	final NetDeviceAPI api = device.getAPI(NetDeviceAPI.class);
            final ProtocolAddressInfo info = api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP);
           for (ProtocolAddress ipaddr : info.addresses()) {
        	   log.info("Device(" + device.toString() + " Adding IP Address:" + ipaddr.toString());
                list.add(ipaddr.toInetAddress());
           }
        } catch (DeviceNotFoundException ex) {
        	log.error("Unable to locate device :" + netDevice.getId());
        } catch (ApiNotFoundException ex) {
        	log.error("Unable to locate NetDeviceAPI for device :" + netDevice.getId());
            // Ignore
        }
        return list;
    }

    /**
     * Is the given device a network device.
     * 
     * @param device
     * @return boolean
     */
    public boolean isNetDevice(Device device) {
        return device.implementsAPI(NetDeviceAPI.class);
    }

    /**
     * Return a network device by its address
     * 
     * @param addr
     *            The address of the interface to return
     * @exception SocketException
     *                If an error occurs
     * @exception NullPointerException
     *                If the specified addess is null
     */
    public VMNetDevice getByInetAddress(InetAddress addr) throws SocketException {
    	log.info("getByInetAddress");
        for (Device dev : DeviceUtils.getDevicesByAPI(NetDeviceAPI.class)) {
            try {
                final NetDeviceAPI api = dev.getAPI(NetDeviceAPI.class);
                final ProtocolAddressInfo info = api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP);
               log.info("Checking Device (" + dev.toString() + 
                		") Address : (" + addr.toString() + ") Present : (" + info.contains(addr) + ")");
                if (info.contains(addr)) {
                    return new NetDeviceImpl(dev);
                }
            } catch (ApiNotFoundException ex) {
                // Ignore
            }
        }
        throw new SocketException("no network interface is bound to such an IP address");
    }

    /**
     * Gets all net devices.
     * 
     * @return A list of Device instances.
     */
    public Collection<VMNetDevice> getNetDevices() {
    	log.info("getNetDevices");
        final ArrayList<VMNetDevice> list = new ArrayList<VMNetDevice>();
        final Collection<Device> devs = DeviceUtils.getDevicesByAPI(NetDeviceAPI.class);
        for (Device dev : devs) {
       	log.info("Adding Device (" + dev.toString() + ")");
            list.add(new NetDeviceImpl(dev));
        }
        return list;
    }

    /**
     * Gets the default local address.
     * 
     * @return InetAddress
     */
    
    public InetAddress getLocalAddress() throws UnknownHostException {
        for (Device dev : devManager.getDevicesByAPI(NetDeviceAPI.class)) {
        	try {
        		final NetDeviceAPI api = (NetDeviceAPI) dev.getAPI(NetDeviceAPI.class);
       		log.info("getLocalAddress: " + dev.toString());
        		final ProtocolAddressInfo addrInfo = api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP);
       		log.info("getLocalAddress: addrInfo " + addrInfo.toString());
                if (addrInfo != null) {
                    final ProtocolAddress addr = addrInfo.getDefaultAddress();
                    if (addr != null) {
                    	log.info("getLocalAddress(" + (addr.toInetAddress()).toString() + ")");
                        //return addr.toInetAddress();
                    }
                }
            } catch (ApiNotFoundException ex) {
                // Strange, but ignore
            }
        }

        
        throw new UnknownHostException("No configured address found");
    }

    public byte[][] getHostByName(String hostname) throws UnknownHostException {

        ArrayList<byte[]> list = null;
        for (NetworkLayer layer : networkLayerManager.getNetworkLayers()) {
            final ProtocolAddress[] addrs = layer.getHostByName(hostname);
            if (addrs != null) {
                if (list == null) {
                    list = new ArrayList<byte[]>();
                }
                final int cnt = addrs.length;
                for (int j = 0; j < cnt; j++) {
                    final ProtocolAddress pa = addrs[j];
                    if (pa != null) {
                        list.add(pa.toByteArray());
                    }
                }
            }
        }

        if ((list == null) || list.isEmpty()) {
            throw new UnknownHostException(hostname);
        } else {
            return (byte[][]) list.toArray(new byte[list.size()][]);
        }
    }

    /**
     * @see java.net.VMNetAPI#getHostByAddr(byte[])
     */
    public String getHostByAddr(byte[] ip) throws UnknownHostException {
        throw new UnknownHostException("Not implemented");
    }

    /**
     * @see java.net.VMNetAPI#getByName(java.lang.String)
     */
    public VMNetDevice getByName(String name) {
        try {
    	    final Device device = devManager.getDevice(name);
            if (isNetDevice(device)) {
                return new NetDeviceImpl(device);
            } else {
                return null;
            }
        } catch (DeviceNotFoundException ex) {
            return null;
        }
    }
}
