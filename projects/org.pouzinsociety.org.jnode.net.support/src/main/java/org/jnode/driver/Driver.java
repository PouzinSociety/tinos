/*
 * $Id: Driver.java 4153 2008-05-30 12:20:45Z lsantha $
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
package org.jnode.driver;

/**
 * Abstract driver of a Device.
 * <p/>
 * Every device driver must extend this class directly or indirectly.
 * <p/>
 * A suitable driver for a specific Device is found by a DeviceToDriverMapper.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @see org.jnode.driver.Device
 * @see org.jnode.driver.DeviceToDriverMapper
 */
public abstract class Driver {

    /**
     * The device this driver it to control
     */
    private Device device;

    /**
     * Sets the device this driver is to control.
     *
     * @param device The device to control, never null
     *               from the device.
     * @throws DriverException
     */
    protected final void connect(Device device)
        throws DriverException {
        if (this.device != null) {
            throw new DriverException("This driver is already connected to a device");
        }
        verifyConnect(device);
        this.device = device;
        afterConnect(device);
    }

    /**
     * Gets the device this driver is to control.
     *
     * @return The device I'm driving
     */
    public final Device getDevice() {
        return device;
    }

    /**
     * This method is called just before a new device is set to this driver.
     * If we should refuse the given device, throw a DriverException.
     *
     * @param device
     * @throws DriverException
     */
    protected void verifyConnect(Device device)
        throws DriverException {
        /* do nothing for now */
    }

    /**
     * This method is called after a new device is set to this driver.
     * You can initialize the driver and/or the device here.
     * Note not to start the device yet.
     *
     * @param device
     */
    protected void afterConnect(Device device) {
        /* do nothing for now */
    }

    /**
     * Start the device.
     *
     * @throws DriverException
     */
    protected abstract void startDevice()
        throws DriverException;

    /**
     * Stop the device.
     *
     * @throws DriverException
     */
    protected abstract void stopDevice()
        throws DriverException;

}
