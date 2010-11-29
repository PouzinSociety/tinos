/*
 * $Id: ConfigurationProcessor.java 4214 2008-06-08 04:37:59Z crawley $
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

 * Modifications : Changes to port JNode code base to OSGi platform.
 *                 - Switched to Apache Commons Logging.
 *
 */
package org.jnode.net.ipv4.config.impl;

import org.apache.commons.logging.*;
import org.jnode.driver.Device;
import org.jnode.util.Queue;
import org.jnode.util.QueueProcessor;
import org.jnode.util.QueueProcessorThread;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ConfigurationProcessor implements QueueProcessor<ConfigurationQueueEntry> {

    private final Queue<ConfigurationQueueEntry> queue = new Queue<ConfigurationQueueEntry>();

    static final Log log = LogFactory.getLog(ConfigurationProcessor.class);

    private QueueProcessorThread<ConfigurationQueueEntry> thread;

    /**
     * Apply the configuration on the given device. This is an asynchronous
     * call.
     * 
     * @param device
     * @param config
     */
    public void apply(Device device, NetDeviceConfig config, boolean waitUntilReady) {
        final ConfigurationQueueEntry entry = new ConfigurationQueueEntry(device, config);
        queue.add(entry);
        if (waitUntilReady) {
            entry.waitUntilReady();
        }
    }

    public void start() {
        thread = new QueueProcessorThread<ConfigurationQueueEntry>(
                "Net configuration processor", queue, this);
        thread.start();
    }

    public void stop() {
        thread.stopProcessor();
    }

    /**
     * @see org.jnode.util.QueueProcessor#process(java.lang.Object)
     */
    public void process(ConfigurationQueueEntry entry) {
        entry.apply();
    }
}
