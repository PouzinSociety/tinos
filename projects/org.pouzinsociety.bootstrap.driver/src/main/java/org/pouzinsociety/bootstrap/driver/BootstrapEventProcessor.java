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
package org.pouzinsociety.bootstrap.driver;

import java.util.ArrayList;
import org.jnode.util.Queue;
import org.jnode.util.QueueProcessor;
import org.jnode.util.QueueProcessorThread;
import org.pouzinsociety.bootstrap.api.*;

final public class BootstrapEventProcessor  implements QueueProcessor<BootstrapEvent>{
   /**
     * Event listeners
     */
    private ArrayList<BootstrapEventListener> listeners = new ArrayList<BootstrapEventListener>();

    /**
     * Cached array of listeners
     */
    private BootstrapEventListener[] listenerCache;

    /**
     * Event queue
     */
    private final Queue<BootstrapEvent> eventQueue = new Queue<BootstrapEvent>();

    /**
     * The thread that will dispatch the events to the listeners
     */
    private QueueProcessorThread<BootstrapEvent> thread;

    /**
     * Does this processor have any listeners.
     *
     * @return
     */
    final synchronized boolean isEmpty() {
        return listeners.isEmpty();
    }

    /**
     * @see org.jnode.driver.net.NetDeviceAPI#addEventListener(org.jnode.driver.net.NetDeviceListener)
     */
    final public synchronized void addEventListener(BootstrapEventListener listener) {
        listeners.add(listener);
        this.listenerCache = null;
        if (thread == null) {
            thread = new QueueProcessorThread<BootstrapEvent>("BootstrapEventProcessor",
                eventQueue, this);
            thread.start();
        }
    }

    /**
     * @see org.jnode.driver.net.NetDeviceAPI#removeEventListener(org.jnode.driver.net.NetDeviceListener)
     */
    final public synchronized void removeEventListener(BootstrapEventListener listener) {
        listeners.remove(listener);
        this.listenerCache = null;
        if (listeners.isEmpty() && (thread != null)) {
            thread.stopProcessor();
            thread = null;
        }
    }

    /**
     * Post an event that will be fired (on another thread) to the listeners.
     *
     * @param event
     */
    final void postEvent(BootstrapEvent event) {
        if (thread != null) {
            eventQueue.add(event);
        }
    }

    /**
     * @see org.jnode.util.QueueProcessor#process(java.lang.Object)
     */
    public void process(BootstrapEvent event) throws Exception {
       BootstrapEventListener[] listeners = this.listenerCache;
        if (listeners == null) {
            synchronized (this) {
                final int size = this.listeners.size();
                if (size > 0) {
                    listeners = (BootstrapEventListener[]) this.listeners
                        .toArray(new BootstrapEventListener[size]);
                    this.listenerCache = listeners;
                }
            }
        }
        if (listeners != null) {
            final int max = listeners.length;
            for (int i = 0; i < max; i++) {
                listeners[i].processEvent(event);
            }
        }
    }
}
