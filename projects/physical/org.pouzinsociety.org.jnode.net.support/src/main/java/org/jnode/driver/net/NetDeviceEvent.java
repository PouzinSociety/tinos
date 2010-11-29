/*
 * $Id: NetDeviceEvent.java 4156 2008-05-30 12:54:00Z hagar-wize $
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
 */
package org.jnode.driver.net;

import java.util.EventObject;

import org.jnode.driver.Device;

/**
 * Base class for all net device events.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class NetDeviceEvent extends EventObject {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Identification of the event
     */
    private final int id;

    /**
     * @param source
     */
    public NetDeviceEvent(Device source, int id) {
        super(source);
        this.id = id;
    }

    /**
     * Gets the device that it the source of this event.
     *
     * @return
     */
    public final Device getDevice() {
        return (Device) getSource();
    }

    /**
     * Gets the event ID.
     *
     * @return Returns the id.
     */
    public final int getId() {
        return id;
    }
}
