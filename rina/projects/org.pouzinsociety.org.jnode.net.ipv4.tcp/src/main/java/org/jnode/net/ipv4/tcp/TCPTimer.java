/*
 * $Id: TCPTimer.java 4214 2008-06-08 04:37:59Z crawley $
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

import org.apache.commons.logging.*;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TCPTimer extends Thread {

    private static final Log log = LogFactory.getLog(TCPTimer.class);
    private final TCPControlBlockList cbList;
    private boolean stop = false;
    private long counter;
    private static int autoNr = 0;

    /**
     * Create a new instance
     * 
     * @param cbList
     */
    public TCPTimer(TCPControlBlockList cbList) {
        super(autoName());
        this.cbList = cbList;
    }

    /**
     * Keep calling timeout forever.
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        while (!stop) {
            try {
                cbList.timeout();
                counter += TCPConstants.TCP_TIMER_PERIOD;
            } catch (Throwable ex) {
                log.error("Error in TCP timer", ex);
            }
            try {
                Thread.sleep(TCPConstants.TCP_TIMER_PERIOD);
            } catch (InterruptedException ex) {
                // Ignore
            }
        }
    }

    /**
     * @return Returns the counter.
     */
    public final long getCounter() {
        return this.counter;
    }

    private static synchronized String autoName() {
        return "tcp-timer-" + (autoNr++);
    }
}
