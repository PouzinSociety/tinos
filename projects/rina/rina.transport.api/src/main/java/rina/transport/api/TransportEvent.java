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

import java.util.EventObject;
import java.util.HashMap;
import java.util.Set;

public class TransportEvent extends EventObject implements TransportConstants {
		private static final long serialVersionUID = 1L;
		private HashMap<String, String> packet = new HashMap<String, String>();
	  
		public TransportEvent() {
	        super("TransportEvent");
	        packet.put("TransportEvent", "TransportEvent");
	    }
		public TransportEvent(String transportCreator) {
	        super(transportCreator);
	        packet.put("TransportEvent", "TransportEvent");
	    }
	    	    
	    public void setKeyValue(String key, String value) {
	    	if (key.equals("BootStrap")) {
	    		throw new IllegalArgumentException("Cannot set (BootStrap)");
	    	}
	    	packet.put(key, value);
	    }
	    
	    public Set<String> keySet() {
	    	return packet.keySet();
	    }
	    
	    public String getKeyValue(String key) {
	    	return packet.get(key);
	    }
	    
	    public boolean containsKey(String key) {
	    	return packet.containsKey(key);
	    }
	    
	    public String toString() {
	    	StringBuffer buf = new StringBuffer();
	    	buf.append("TransportEvent:\n");
	    	for (String key : packet.keySet())
	    		buf.append("Key (" + key + ") -> (" + packet.get(key) + ")\n");	    	
	    	return buf.toString();	    	
	    }
	    
}
