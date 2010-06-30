/*
 * $Id: AbstractDeviceCore.java 4157 2008-05-30 14:55:37Z hagar-wize $
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
package org.jnode.driver.net.spi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jnode.driver.DriverException;
import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.util.TimeoutException;

/**
 * This abstract class is not intended for any external purpose. It only serves
 * as a voluntary guide for driver implementation of network cards.
 *
 * @author epr
 */
public abstract class AbstractDeviceCore {

    /**
     * My logger
     */
	private static final Log log = LogFactory.getLog(AbstractDeviceCore.class);

    /**
     * Gets the hardware address of this device
     */
    public abstract HardwareAddress getHwAddress();

    /**
     * Initialize the device
     */
    public abstract void initialize()
        throws DriverException;

    /**
     * Disable the device
     */
    public abstract void disable();

    /**
     * Release all resources
     */
    public abstract void release();

    /**
     * Transmit the given buffer
     *
     * @param buf
     * @param timeout
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public abstract void transmit(SocketBuffer buf, HardwareAddress destination, long timeout)
        throws InterruptedException, TimeoutException;
}
