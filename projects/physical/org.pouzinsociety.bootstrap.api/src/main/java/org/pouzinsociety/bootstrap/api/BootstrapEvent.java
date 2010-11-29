/*
 * 2008 - 2010 (c) Waterford Institute of Technology
 *		   TSSG, EU ICT 4WARD
 *
 * 2010 (c) Pouzin Society
 *   - Forked from EU ICT 4WARD Open Source Distribution.
 *   - Organisation Strings updated to reflect fork.
 *
 *
 * 2010 (c) Pouzin Society
 *   - Forked from EU ICT 4WARD Open Source Distribution.
 *
 * 2010 (c) Pouzin Society
 *   - Forked from EU ICT 4WARD Open Source Distribution.
 *   - Organisation Strings updated to reflect fork.
 *
 *   - Organisation Strings updated to reflect fork.
 *
 *
 * Author        : pphelan(at)pouzinsociety.org
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
package org.pouzinsociety.bootstrap.api;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Set;

public class BootstrapEvent extends EventObject implements BootstrapConstants {
	private static final long serialVersionUID = 1L;
	private HashMap<String, String> packet = new HashMap<String, String>();
	/**
	* @param source
	*/
	public BootstrapEvent() {
		super("bootstrap");
		packet.put("BootStrap", "BootStrap");
	}

	public void setEventId(int eventId) {
		packet.put(BootstrapConstants.EventIdKey, Integer.valueOf(eventId).toString());
	}

	public int getEventId() {
		String eventId = packet.get(BootstrapConstants.EventIdKey);
		try {
			return (Integer.parseInt(eventId));
		} catch (NumberFormatException nfe) {
			return BootstrapConstants.NOT_SET;
		}
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
}
