/*
 * $Id: TCPSocketImplFactory.java 4214 2008-06-08 04:37:59Z crawley $
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
 *
 */
package org.jnode.net.ipv4.tcp;

import jnode.net.SocketImpl;
import jnode.net.SocketImplFactory;

/**
 * @author epr
 */
public class TCPSocketImplFactory implements SocketImplFactory {

    /** The protocol I'm using */
    private final TCPProtocol protocol;

    /**
     * Initialize a new instance
     * 
     * @param protocol
     */
    public TCPSocketImplFactory(TCPProtocol protocol) {
        this.protocol = protocol;
    }

    /**
     * @see java.net.SocketImplFactory#createSocketImpl()
     */
    public SocketImpl createSocketImpl() {
        return new TCPSocketImpl(protocol);
    }

}
