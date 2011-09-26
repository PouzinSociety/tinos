/*
 * $Id: IPv4NetworkLayer.java 4214 2008-06-08 04:37:59Z crawley $
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
 *                 - log4j -> apache commons
 *                 - multicast support
 *                 - updates to OSGi configuration
 *
 */
package org.jnode.net.ipv4.layer;

import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.jnode.driver.Device;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.HardwareAddress;
import org.jnode.net.InvalidLayerException;
import org.jnode.net.LayerAlreadyRegisteredException;
import org.jnode.net.NetworkLayer;
import org.jnode.net.NoSuchProtocolException;
import org.jnode.net.ProtocolAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.net.TransportLayer;
import org.jnode.net.arp.ARPService;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.IPv4Constants;
import org.jnode.net.ipv4.IPv4FragmentList;
import org.jnode.net.ipv4.IPv4Header;
import org.jnode.net.ipv4.IPv4Protocol;
import org.jnode.net.ipv4.IPv4ProtocolAddressInfo;
import org.jnode.net.ipv4.IPv4Route;
import org.jnode.net.ipv4.IPv4RoutingTable;
import org.jnode.net.ipv4.IPv4Service;
import org.jnode.net.ipv4.icmp.ICMPConstants;
import org.jnode.net.ipv4.icmp.ICMPHeader;
import org.jnode.net.ipv4.icmp.ICMPRedirectHeader;
import org.jnode.net.ipv4.icmp.ICMPTimeExceededHeader;
import org.jnode.net.ipv4.icmp.ICMPUnreachableHeader;
import org.jnode.net.Resolver;
import org.jnode.util.NumberUtils;
import org.jnode.util.Statistics;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.context.BundleContextAware;

/**
 * @author epr
 */
public class IPv4NetworkLayer implements NetworkLayer, IPv4Constants, IPv4Service, BundleContextAware {

    /**
     * My logger
     */
    private static final Log log = LogFactory.getLog(IPv4NetworkLayer.class);


    private final HashMap<Integer, IPv4Protocol> protocols = new HashMap<Integer, IPv4Protocol>();

    /**
     * List of in-complete fragments
     */
    private final HashMap<Object, IPv4FragmentList> fragments =
            new HashMap<Object, IPv4FragmentList>();

    /**
     * System time of last call to removeDeadFragments
     */
    private long lastFragmentCleanup = 0;

    /**
     * My statistics
     */
    private final IPv4Statistics stat = new IPv4Statistics();

    /**
     * The routing table
     */
    private final IPv4RoutingTable rt = new IPv4RoutingTable();

    /**
     * The sender
     */
    private final IPv4Sender sender;

    /**
     * IP Forwarding
     */
    private boolean ipForward = false;

    /**
     * The ARP network layer
     */
    private ARPService arp;
    /**
     * Name Resolver
     */
    private Resolver resolver;

    /**
     * OSGi
     */
    private static final String IP_FORWARD_SPECIFIER = "Ip-Forward";
    private BundleContext bundleContext;

    /**
     * Initialize a new instance
     */
    public IPv4NetworkLayer() throws NetworkException {
        sender = new IPv4Sender(this);
	// Check if IP Forwarding is enabled.
    }

    /**
     * Gets the name of this type
     */
    public String getName() {
        return "ipv4";
    }

    /**
     * Gets the protocol ID this packettype handles
     */
    public int getProtocolID() {
        return EthernetConstants.ETH_P_IP;
    }

    /**
     * Can this packet type process packets received from the given device?
     */
    public boolean isAllowedForDevice(Device dev) {
        // For all devices
        return true;
    }

    /**
     * Process a packet that has been received and matches getType()
     * 
     * @param skbuf
     * @param deviceAPI
     * @throws SocketException
     */
    public void receive(SocketBuffer skbuf, NetDeviceAPI deviceAPI) throws SocketException {

        // Update statistics
        stat.ipackets.inc();

        // Get IP header
        final IPv4Header hdr = new IPv4Header(skbuf);
        if (!hdr.isChecksumOk()) {
            stat.badsum.inc();
            return;
        }
        // Set the header object in the buffer-field
        skbuf.setNetworkLayerHeader(hdr);

        // Remove header from skbuf-data
        skbuf.pull(hdr.getLength());
        // Trim the end of the message, to we have a valid length
        skbuf.trim(hdr.getDataLength());

        // Now test if the size of the buffer equals the datalength in the
        // header, if now ignore the packet
        if (skbuf.getSize() < hdr.getDataLength()) {
            stat.badlen.inc();
            return;
        }

        // Update the ARP cache for the source address
        updateARPCache(skbuf.getLinkLayerHeader().getSourceAddress(), hdr.getSourceAddress());

        // Get my IP address
        final IPv4ProtocolAddressInfo myAddrInfo =
                (IPv4ProtocolAddressInfo) deviceAPI.getProtocolAddressInfo(getProtocolID());
        if (myAddrInfo == null) {
            stat.nodevaddr.inc();
        }

        // Should I process this packet, or is it for somebody else?
        final IPv4Address dstAddr = hdr.getDestination();
        final boolean shouldProcess;
        if (myAddrInfo != null) {
            shouldProcess = (myAddrInfo.contains(dstAddr) || dstAddr.isMulticast());
        } else {
            // I don't have an IP address yet, if the linklayer says
            // it is for me, we'll process it, otherwise we'll drop it.
            shouldProcess = !skbuf.getLinkLayerHeader().getDestinationAddress().isBroadcast();
        }
        
        if (!shouldProcess) {
            if (ipForward == true) {
                 ip_forward(skbuf);
                 return;
            } else {
                 log.debug("IPPacket not for me, ignoring (dst=" + dstAddr + ")");
                 return;
            }
        }

        // Is it a fragment?
        if (hdr.isFragment()) {
            // Yes it is a fragment
            stat.fragments.inc();
            deliverFragment(hdr, skbuf);
        } else {
            // It is a complete packet, find the protocol handler
            // and let it do the rest
            deliver(hdr, skbuf);
        }

        // Do a cleanup of the fragmentlist from time to time
        final long now = System.currentTimeMillis();
        if ((now - lastFragmentCleanup) >= (IP_FRAGTIMEOUT * 2)) {
            removeDeadFragments();
        }
    }

    /**
     * Gets the routing table
     */
    public IPv4RoutingTable getRoutingTable() {
        return rt;
    }

    /**
     * Deliver a packet to the corresponding protocol
     * 
     * @param hdr
     * @param skbuf
     */
    private void deliver(IPv4Header hdr, SocketBuffer skbuf) throws SocketException {
        final IPv4Protocol protocol;
        try {
            protocol = getProtocol(hdr.getProtocol());
            protocol.receive(skbuf);
        } catch (NoSuchProtocolException ex) {
            log.debug("Found unknown IP src=" + hdr.getSource() + ", dst=" + hdr.getDestination() +
                    ", prot=0x" + NumberUtils.hex(hdr.getProtocol(), 2));
        }
    }

    /**
     * Process the delivery of a fragment
     * 
     * @param hdr
     * @param skbuf
     * @throws NetworkException
     */
    private void deliverFragment(IPv4Header hdr, SocketBuffer skbuf) throws SocketException {
        final Object key = hdr.getFragmentListKey();
        final IPv4FragmentList flist = (IPv4FragmentList) fragments.get(key);
        if (flist == null) {
            // This is a fragment for a new list
            fragments.put(key, new IPv4FragmentList(skbuf));
        } else {
            if (flist.isAlive()) {
                flist.add(skbuf);
                if (flist.isComplete()) {
                    // The fragmentlist is now complete, deliver it
                    final SocketBuffer pbuf = flist.getPacket();
                    final IPv4Header phdr = (IPv4Header) pbuf.getNetworkLayerHeader();
                    stat.reassembled.inc();
                    deliver(phdr, pbuf);
                } 
            } else {
                // Timeout of fragmentlist, destroy it
                fragments.remove(key);
            }
        }
    }

    /**
     * Remove all dead fragments from the fragment list
     */
    private final void removeDeadFragments() {
        final ArrayList<Object> deadFragmentKeys = new ArrayList<Object>();
        // First collect all dead fragment keys
        // Do not remove the directly, since that will create an error
        // in the iterator.
        for (IPv4FragmentList f : fragments.values()) {
            if (!f.isAlive()) {
                deadFragmentKeys.add(f.getKey());
            }
        }
        if (!deadFragmentKeys.isEmpty()) {
            // Now remove all dead fragments
            for (Object key : deadFragmentKeys) {
                fragments.remove(key);
            }
            // We're done
            log.debug("Removed " + deadFragmentKeys.size() + " dead fragments");
        }
        // Update our last invocation timestamp
        lastFragmentCleanup = System.currentTimeMillis();
    }

    /**
     * Gets the protocol for a given ID
     * 
     * @param protocolID
     * @throws NoSuchProtocolException
     *             No protocol with the given ID was found.
     */
    public IPv4Protocol getProtocol(int protocolID) throws NoSuchProtocolException {
        final IPv4Protocol protocol;
        protocol = (IPv4Protocol) protocols.get(protocolID);
        if (protocol == null) {
            throw new NoSuchProtocolException("with ID " + protocolID);
        }
        return protocol;
    }

    /**
     * Register a protocol
     * 
     * @param protocol
     */
    public void registerProtocol(IPv4Protocol protocol) {
        protocols.put(protocol.getProtocolID(), protocol);
    }

    /**
     * Unregister a protocol
     * 
     * @param protocol
     */
   public void unregisterProtocol(IPv4Protocol protocol) {
        protocols.remove(protocol.getProtocolID());
    }

    /**
     * Register a transportlayer as possible destination of packets received by
     * this networklayer
     * 
     * @param layer
     */
    public void registerTransportLayer(TransportLayer layer)
        throws LayerAlreadyRegisteredException, InvalidLayerException {
        if (layer instanceof IPv4Protocol) {
            registerProtocol((IPv4Protocol) layer);
        } else {
            throw new InvalidLayerException("No IPv4Protocol");
        }
    }

    /**
     * Unregister a transportlayer
     * 
     * @param layer
     */
    public void unregisterTransportLayer(TransportLayer layer) {
        if (layer instanceof IPv4Protocol) {
            unregisterProtocol((IPv4Protocol) layer);
        }
    }

    /**
     * Gets all registered transport-layers
     */
    public Collection<TransportLayer> getTransportLayers() {
        final ArrayList<TransportLayer> result = new ArrayList<TransportLayer>(protocols.values());
        return result;
    }

    /**
     * Gets a registered transportlayer by its protocol ID.
     * 
     * @param protocolID
     */
    public TransportLayer getTransportLayer(int protocolID) throws NoSuchProtocolException {
        return getProtocol(protocolID);
    }

    /**
     * @see org.jnode.net.NetworkLayer#getStatistics()
     */
    public Statistics getStatistics() {
        return stat;
    }

    /**
     * @see org.jnode.net.ipv4.IPv4Service#transmit(org.jnode.net.ipv4.IPv4Header,
     *      org.jnode.net.SocketBuffer)
     */
    public void transmit(IPv4Header hdr, SocketBuffer skbuf) throws SocketException {
        sender.transmit(hdr, skbuf);
    }

    /**
     * Gets the protocol addresses for a given name, or null if not found.
     * 
     * @param hostname
     * @return
     */
    public ProtocolAddress[] getHostByName(String hostname) {
      try {
            return resolver.getByName(hostname);
        } catch (UnknownHostException ex) {
            return null;
        }
    }

    private void updateARPCache(HardwareAddress hwAddr, ProtocolAddress pAddr) {
            arp.getCache().set(hwAddr, pAddr, true);
    }

    public ARPService getArp() {
	return arp;
    }

    public void setArp(ARPService arp) {
	this.arp = arp;
    }
    public void setResolver(Resolver resolver) {
	this.resolver = resolver;
    }

    public void setBundleContext(BundleContext bundleContext) {
	this.bundleContext = bundleContext;
	Bundle bundle = this.bundleContext.getBundle();
	try {
		String ipForwardMode =
			(String)bundle.getHeaders().get(IP_FORWARD_SPECIFIER);
		if (ipForwardMode != null) {
			if (!ipForwardMode.isEmpty()) {
				// A setting of "On" enables, everything else disables.
				ipForward = ipForwardMode.equals("On");
			}
		}
	} catch (Exception e) {
		ipForward = false;
		log.error("Cannot determine " + IP_FORWARD_SPECIFIER + " mode - default disabled");
		log.error(e);
	}
	log.info("IPv4 IP Forward : " + ((ipForward == true) ? "On" : "Off"));
    }
    
    public void setIpForward(Boolean setting) {
    	ipForward = setting;
    }

   /**
    * IP Forwarding
    */
   private void ip_forward(SocketBuffer skbuf) throws SocketException {       
       // Gets the original IP header
       final IPv4Header origIpHdr = (IPv4Header) skbuf.getNetworkLayerHeader();
       final IPv4Address destIp = origIpHdr.getDestination();
       IPv4Route route;
       
       log.info("IpHdr : " + origIpHdr.toString());
     
	   // Do not forward linklayer broadcast messages
       if (skbuf.getLinkLayerHeader().getDestinationAddress().isBroadcast()) {
           stat.ipcantforward.inc();
           return;
       }
       
       // Do not respond to networklayer broadcast/multicast messages
       if (destIp.isBroadcast() || destIp.isMulticast()) {
           stat.ipcantforward.inc();
           return;
       }
       
       // Do not forward Class D, E, 0, loopback Addresses
       if (destIp.isClassD() || destIp.isClassE() || destIp.isAny() || destIp.equals(new IPv4Address("127.0.0.1"))) {
           stat.ipcantforward.inc();
    	   return;
       }
       
       // Decrement TTL
       if (origIpHdr.getTtl() <= 1) {
           stat.ipfexceed.inc();
           log.info("IpHdr : " + origIpHdr.toString() + " = ICMP Time Exceeded");
    	   icmp_time_exceeded(skbuf, origIpHdr.getSource(), ICMPConstants.ICMP_EXC_TTL);
    	   return;
       }
       
       // Check if we have a route for this destination
       try {
    	   route = rt.search(destIp);
       } catch (NoRouteToHostException ex) {
    	   // No Route Available
           stat.ipfunreach.inc();
           log.info("IpHdr : " + origIpHdr.toString() + " = ICMP Unreachable");
    	   icmp_unreachable(skbuf, origIpHdr.getSource(), ICMPConstants.ICMP_HOST_UNREACH);
    	   return;
       }
       
       // Is the route on the same device as receiver ?
       if (skbuf.getDevice().equals(route.getDevice())) {
           stat.ipfredirect.inc();
           log.info("IpHdr : " + origIpHdr.toString() + " = ICMP Redirect");
    	   icmp_redirect(skbuf, origIpHdr.getSource(), ICMPConstants.ICMP_REDIR_HOST, route.getGateway());
    	   return;
       }
    
       // Forward Packet, drop TTL
       origIpHdr.setTtl(origIpHdr.getTtl() - 1);

       // Select interface based on route
       skbuf.setDevice(route.getDevice());
 
       log.info("IpHdr : " + origIpHdr.toString() + " = Forwarding - Device : " + route.getDevice().toString());
       
       // Transmit it, let the transmit find the route
       transmit(origIpHdr, skbuf);
   }
   
   private void icmp_unreachable(SocketBuffer srcBuf, IPv4Address dstAddr, int errorCode) throws SocketException {       
       // Build the response IP header
       final int tos = 0;
       final int ttl = 0xFF;
       final IPv4Header ipHdr = new IPv4Header(tos, ttl, IPv4Constants.IPPROTO_ICMP, dstAddr, 0);
       
       // Build the response ICMP header
       final ICMPHeader icmpHdr = new ICMPUnreachableHeader(errorCode);

       // Unpull the original IP header
       srcBuf.unpull(srcBuf.getNetworkLayerHeader().getLength());       
       // Only need the original IP header + 64bits
       srcBuf.trim(srcBuf.getNetworkLayerHeader().getLength() + 8);

       // Create a response buffer
       final SocketBuffer skbuf = new SocketBuffer();
       // Prefix the ICMP header to the response buffer
       icmpHdr.prefixTo(skbuf);
       // Append the original message to the response buffer
       skbuf.append(srcBuf);
       
       // Send it
       transmit(ipHdr, skbuf);
   }
   
   private void icmp_time_exceeded(SocketBuffer srcBuf, IPv4Address dstAddr, int errorCode) throws SocketException {
       // Build the response IP header
       final int tos = 0;
       final int ttl = 0xFF;
       final IPv4Header ipHdr = new IPv4Header(tos, ttl, IPv4Constants.IPPROTO_ICMP, dstAddr, 0);

       // Build the response ICMP header
       final ICMPHeader icmpHdr = new ICMPTimeExceededHeader(errorCode);
 
       // Unpull the original IP header
       srcBuf.unpull(srcBuf.getNetworkLayerHeader().getLength());
       // Only need the original IP header + 64bits
       srcBuf.trim(srcBuf.getNetworkLayerHeader().getLength() + 8);

       // Create a response buffer
       final SocketBuffer skbuf = new SocketBuffer();
       // Prefix the ICMP header to the response buffer
       icmpHdr.prefixTo(skbuf);
       // Append the original message to the response buffer
       skbuf.append(srcBuf);
       
       // Send it
       transmit(ipHdr, skbuf);
   }
   
   private void icmp_redirect(SocketBuffer srcBuf, IPv4Address dstAddr, int errorCode, IPv4Address gateway) throws SocketException {
       // Build the response IP header
       final int tos = 0;
       final int ttl = 0xFF;
       final IPv4Header ipHdr = new IPv4Header(tos, ttl, IPv4Constants.IPPROTO_ICMP, dstAddr, 0);

       // Build the response ICMP header
       final ICMPHeader icmpHdr = new ICMPRedirectHeader(errorCode, gateway);
 
       // Unpull the original IP header
       srcBuf.unpull(srcBuf.getNetworkLayerHeader().getLength());
       // Only need the original IP header + 64bits
       srcBuf.trim(srcBuf.getNetworkLayerHeader().getLength() + 8);

       // Create a response buffer
       final SocketBuffer skbuf = new SocketBuffer();
       // Prefix the ICMP header to the response buffer
       icmpHdr.prefixTo(skbuf);
       // Append the original message to the response buffer
       skbuf.append(srcBuf);
       
       // Send it
       transmit(ipHdr, skbuf);
   }

}
