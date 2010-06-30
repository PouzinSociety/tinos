/*
 * $Id: DeviceUtils.java 4153 2008-05-30 12:20:45Z lsantha $
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
 *
 */
package org.jnode.driver;

import java.util.Collection;
import java.util.Collections;
import javax.naming.NameNotFoundException;
//import org.jnode.naming.InitialNaming;

/**
 * Class with utility methods for the device framework.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DeviceUtils {

    /**
     * Cached devicemanager reference
     */
    static DeviceManager deviceManager;
    
    public DeviceUtils(DeviceManager dm) {
		deviceManager = dm;
	}
    
   /**
     * Gets the device manager
     *
     * @return The device manager
     * @throws NameNotFoundException
     */
    public static DeviceManager getDeviceManager()
        throws NameNotFoundException {
        // TODO - use this to find the bold callers
    	throw new NameNotFoundException("Fix me");
    }

    /**
     * Gets a device by name
     *
     * @param deviceID
     * @return The device
     * @throws DeviceNotFoundException
     */
    public static Device getDevice(String deviceID)
        throws DeviceNotFoundException {
    	return deviceManager.getDevice(deviceID);
    }

    /**
     * Gets a specific API from a device.
     *
     * @param deviceID the ame of the requested device
     * @param api      the API class to use
     * @return The api implementation
     * @throws DeviceNotFoundException
     * @throws ApiNotFoundException
     */
    public static <T extends DeviceAPI> T getAPI(String deviceID, Class<T> api)
        throws DeviceNotFoundException, ApiNotFoundException {
    	// FIX-ME
            throw new DeviceNotFoundException("DeviceManager not found");
    }


    /**
     * Returns a collection of all known devices that implement the given api..
     * The collection is not modifiable, but the underlying collection
     * can change, so be aware of exceptions in iterators.
     *
     * @param apiClass
     * @return All known devices the implement the given api.
     */
    public static Collection<Device> getDevicesByAPI(Class<? extends DeviceAPI> apiClass) {
    	// FIX-ME
            return Collections.emptyList();
    }
}
