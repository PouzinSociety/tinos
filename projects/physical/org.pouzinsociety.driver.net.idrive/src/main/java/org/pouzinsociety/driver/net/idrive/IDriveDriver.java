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
package org.pouzinsociety.driver.net.idrive;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jnode.driver.DriverException;
import org.jnode.driver.net.NetworkException;
import org.jnode.driver.net.spi.AbstractNetDriver;
import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetAddress;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ethernet.EthernetHeader;
import org.jnode.net.ethernet.EthernetUtils;
import org.pouzinsociety.config.dao.EthernetOverIMDao;
import org.pouzinsociety.transport.im.ConnectionImpl;

public class IDriveDriver extends AbstractNetDriver implements
    EthernetConstants, PacketListener {
	private EthernetOverIMDao dao;
	private ConnectionImpl networkMedium = new ConnectionImpl();
	
	public IDriveDriver(EthernetOverIMDao deviceDao) throws DriverException {
		super();
		
		dao = deviceDao;
		try {
			networkMedium.setConfiguration(dao.getIm_server(), dao.getIm_port(), 
					dao.getIm_buddyId(), dao.getIm_buddyPassword(),
					dao.getIm_resourceId(), dao.getIm_chatroom());
			networkMedium.connect(this);
		} catch(Exception e) {
			throw new DriverException("Cannot open IM Channel");
		}
	}
    
    /**
     * Gets the hardware address of this device
     */
    public HardwareAddress getAddress() {
        return new EthernetAddress(dao.getEthernetAddress());
    }

    /**
     * Gets the maximum transfer unit, the number of bytes this device can
     * transmit at a time.
     */
    public int getMTU() {
        return ETH_DATA_LEN;
    }

    /**
     * @see org.jnode.driver.net.spi.AbstractNetDriver#doTransmit(SocketBuffer,
     *      HardwareAddress)
     */
    protected final void doTransmit(SocketBuffer skbuf,
                                    HardwareAddress destination) throws NetworkException {
        skbuf.insert(ETH_HLEN);
        if (destination != null) {
            destination.writeTo(skbuf, 0);
        } else {
            EthernetAddress.BROADCAST.writeTo(skbuf, 0);
        }
        getAddress().writeTo(skbuf, 6);
        skbuf.set16(12, skbuf.getProtocolID());

        try {
        	networkMedium.transmit(skbuf.toByteArray());
        } catch (Exception e) {
        	throw new NetworkException("Unable to Tx packet");
        }
    }
    
	public void processPacket(Packet imPacket) {
		byte[] pdu = (byte[])imPacket.getProperty("PDU");
		if (pdu != null) {
			String node = (String)imPacket.getProperty("Node");
			if (node.equals(dao.getIm_resourceId())) {
				// System.out.println("My own packet returnth");
				return;
			}
			try {
				onReceive(new SocketBuffer(pdu, 0, pdu.length));
			} catch (NetworkException e) {
				e.printStackTrace();
			}
		}
		
	}


    /**
     * @see org.jnode.driver.net.spi.AbstractNetDriver#onReceive(org.jnode.net.SocketBuffer)
     */
    public void onReceive(SocketBuffer skbuf) throws NetworkException {

        // Extract ethernet header
        final EthernetHeader hdr = new EthernetHeader(skbuf);
        skbuf.setLinkLayerHeader(hdr);
        skbuf.setProtocolID(EthernetUtils.getProtocol(hdr));
        skbuf.pull(hdr.getLength());

        // Send to PM
        super.onReceive(skbuf);
    }

    /**
     * @see org.jnode.driver.net.spi.AbstractNetDriver#getDevicePrefix()
     */
    protected String getDevicePrefix() {
        return dao.getDevice_prefix();
    }

    /**
     * @see org.jnode.driver.net.spi.AbstractNetDriver#renameToDevicePrefixOnly()
     */
    protected boolean renameToDevicePrefixOnly() {
        return true;
    }

}
