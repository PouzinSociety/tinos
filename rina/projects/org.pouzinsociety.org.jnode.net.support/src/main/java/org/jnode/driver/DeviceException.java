/*
 * $Id: DeviceException.java 4153 2008-05-30 12:20:45Z lsantha $
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

/**
 * Generic exception of devices in the device framework.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DeviceException extends Exception {

   /**
     * 
     */
    private static final long serialVersionUID = 1L;

   /**
     *
     */
    public DeviceException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public DeviceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public DeviceException(Throwable cause) {
        super(cause);
    }

    /**
     * @param s
     */
    public DeviceException(String s) {
        super(s);
    }
}
