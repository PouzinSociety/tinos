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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jnode.util.Queue;
import org.jnode.util.QueueProcessor;
import org.jnode.util.QueueProcessorThread;


public abstract class AbstractTransport implements TransportTxRx, QueueProcessor<TransportEvent> {
	 static Log log = LogFactory.getLog(AbstractTransport.class);
		private String transportName;
		/**
	     * Queue used to store events ready for transmission
	     */
	    private final Queue<TransportEvent> txQueue = new Queue<TransportEvent>();
	    
	    /**
	     * Thread used to transmit frames
	     */
	    private QueueProcessorThread<TransportEvent> txThread;
	    /**
	     * Event processor
	     */
	    private TransportEventProcessor eventProcessor;
		
	    public AbstractTransport(String transportName) {
	    	this.transportName = transportName;
		}
	    
	    public String getTransportName() {
	    	return this.transportName;
	    }
			
		public void addEventListener(TransportListener listener) {
	        TransportEventProcessor proc = this.eventProcessor;
	        log.info("Adding Listener");
	        if (proc == null) {
	            this.eventProcessor = proc = new TransportEventProcessor();
	        }
	        proc.addEventListener(listener);
	    }

	    public void removeEventListener(TransportListener listener) {
	        final TransportEventProcessor proc = this.eventProcessor;
	        if (proc != null) {
	            proc.removeEventListener(listener);
	        }
	    }
	    
	    /**
	     * Post an event that will be fired (on another thread) to the listeners.
	     *
	     * @param event
	     */
	    public void postEvent(TransportEvent event) {
	       // log.info("EventPosted: " + event.toString());
	        final TransportEventProcessor proc = this.eventProcessor;
	        if (proc != null) {
	            proc.postEvent(event);
	        }
	    }
	    
	    /**
	     * @see org.jnode.util.QueueProcessor#process(java.lang.Object)
	     *  - The actual Transmit.
	     */
	    public abstract void process(TransportEvent event) throws Exception;
	    
	    
	    public final void transmit(TransportEvent event)
	        throws TransportException {
	        txQueue.add(event);
	    }

		public void startTxThread() {
		      txThread = new QueueProcessorThread<TransportEvent>("Transport-tx", txQueue, this);
		      txThread.start();
		}
}
