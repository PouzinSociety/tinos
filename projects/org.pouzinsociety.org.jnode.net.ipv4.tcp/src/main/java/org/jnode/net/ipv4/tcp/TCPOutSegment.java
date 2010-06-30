/*
 * $Id: TCPOutSegment.java 4241 2008-06-15 12:50:44Z lsantha $
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
 *                 - log4j -> apache commons.
 *
 */

package org.jnode.net.ipv4.tcp;

import java.net.SocketException;
import org.apache.commons.logging.*;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ipv4.IPv4Header;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TCPOutSegment extends TCPSegment {
    private static final boolean DEBUG = false;

    /**
     * My logger
     */
    private static final Log log = LogFactory.getLog(TCPOutSegment.class);

    private final TCPDataBuffer buffer;
    private int dataOffset;

    /**
     * Timeout counter, if 0, re-transmit
     */
    private int timeout;

    /**
     * Number of timeout ticks (usually grows)
     */
    private int timeoutTicks;

    /**
     * @param ipHdr
     * @param hdr
     * @param dataOffset
     */
    public TCPOutSegment(IPv4Header ipHdr, TCPHeader hdr, TCPDataBuffer buffer, int dataOffset,
                         int timeout) {
        super(ipHdr, hdr);
        this.buffer = buffer;
        this.dataOffset = dataOffset;
        this.timeout = timeout;
        this.timeoutTicks = timeout;
    }

    /**
     * Process timeout handling
     */
    public void timeout(TCPProtocol tcp) throws SocketException {
        timeout--;
        if (timeout == 0) {
            if (DEBUG) {
                log.debug("Resend segment " + getSeqNr());
            }
            send(tcp);
            timeoutTicks = timeoutTicks * 2;
            timeout = timeoutTicks;
        }
    }

    /**
     * Send this segment
     *
     * @param tcp
     */
    public void send(TCPProtocol tcp) throws SocketException {
        final SocketBuffer skbuf;
        if (hdr.getDataLength() > 0) {
            skbuf = buffer.createSocketBuffer(dataOffset, hdr.getDataLength());
        } else {
            skbuf = new SocketBuffer(0);
        }
        tcp.send(ipHdr, hdr, skbuf);
    }

    /**
     * @return Returns the dataOffset.
     */
    public final int getDataOffset() {
        return this.dataOffset;
    }

    public final void adjustDataOffset(int diff) {
        if (hdr.getDataLength() > 0) {
            this.dataOffset -= diff;
        }
    }

    /**
     * Does this segment only contain an ACK?
     *
     * @return True if this segment contains only an acknowledgment, false otherwise
     */
    public boolean isAckOnly() {
        return ((hdr.getFlags() == TCPConstants.TCPF_ACK) && (hdr.getDataLength() == 0));
    }
}
