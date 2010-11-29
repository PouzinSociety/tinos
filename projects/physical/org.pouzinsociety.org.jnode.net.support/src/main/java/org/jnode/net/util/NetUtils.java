/*
 * $Id: NetUtils.java 4215 2008-06-08 05:47:07Z crawley $
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
 */
package org.jnode.net.util;

import org.apache.commons.logging.*;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.NetworkLayerManager;
import org.jnode.net.SocketBuffer;

/**
 * Utility class for network devices
 * 
 * @author epr
 */
public class NetUtils {
    static NetworkLayerManager networkLayerManager = null;
    static Log log = LogFactory.getLog(NetUtils.class);

    NetUtils(NetworkLayerManager nlm) {
	networkLayerManager = nlm;
    }
	
    public static NetworkLayerManager getNetworkLayerManager() {
	return networkLayerManager;
    }

    /**
     * A packet has just been received, send it to the packet-type-manager.
     * 
     * @param skbuf
     */
    public static void sendToPTM(SocketBuffer skbuf) throws NetworkException {
    	if (networkLayerManager != null) {
        	networkLayerManager.receive(skbuf);
    	} else {
    		throw new NetworkException("Cannot find NetworkLayerManager");
    	}
    }

    /**
     * Gets the packet-type-manager
     */
    public static NetworkLayerManager getNLM() throws NetworkException {
    	if (networkLayerManager != null) {
        	return getNetworkLayerManager(); 
    	} else {
    		throw new NetworkException("Cannot find NetworkLayerManager");
    	}
    }
}
