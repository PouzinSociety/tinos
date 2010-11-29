/*
 * 2010 (c) Pouzin Society
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
package rina.transport.api;

import java.util.ArrayList;
import org.jnode.util.Queue;
import org.jnode.util.QueueProcessor;
import org.jnode.util.QueueProcessorThread;

final class TransportEventProcessor implements QueueProcessor<TransportEvent> {

    /**
     * Event listeners
     */
    private ArrayList<TransportListener> listeners = new ArrayList<TransportListener>();

    /**
     * Cached array of listeners
     */
    private TransportListener[] listenerCache;

    /**
     * Event queue
     */
    private final Queue<TransportEvent> eventQueue = new Queue<TransportEvent>();

    /**
     * The thread that will dispatch the events to the listeners
     */
    private QueueProcessorThread<TransportEvent> thread;

    /**
     * Does this processor have any listeners.
     *
     * @return
     */
    final synchronized boolean isEmpty() {
        return listeners.isEmpty();
    }

    final synchronized void addEventListener(TransportListener listener) {
        listeners.add(listener);
        this.listenerCache = null;
        if (thread == null) {
            thread = new QueueProcessorThread<TransportEvent>("TransportEventProcessor",
                eventQueue, this);
            thread.start();
        }
    }

    final synchronized void removeEventListener(TransportListener listener) {
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
    final void postEvent(TransportEvent event) {
        if (thread != null) {
            eventQueue.add(event);
        }
    }

    public void process(TransportEvent event) throws Exception {
        TransportListener[] listeners = this.listenerCache;
        if (listeners == null) {
            synchronized (this) {
                final int size = this.listeners.size();
                if (size > 0) {
                    listeners = (TransportListener[]) this.listeners
                        .toArray(new TransportListener[size]);
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
