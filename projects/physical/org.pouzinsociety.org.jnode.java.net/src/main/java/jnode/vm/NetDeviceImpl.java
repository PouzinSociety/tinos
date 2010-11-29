/*
 * $Id: NetDeviceImpl.java 2224 2006-01-01 12:49:03Z epr $
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

import jnode.net.VMNetDevice;
import org.jnode.driver.Device;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class NetDeviceImpl extends VMNetDevice {

    private final Device device;
    
    public NetDeviceImpl(Device device) {
        this.device = device;
    }
    
    /**
     * @see java.net.VMNetDevice#getId()
     */
    public String getId() {
        return device.getId();
    }
    
    /**
     * @return Returns the device.
     */
    public final Device getDevice() {
        return this.device;
    }
}
