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
package org.pouzinsociety.config.dao;

public class DeviceDao {
    String device_name;
    String device_prefix;

    public DeviceDao() {
        this.device_name = "0";
        this.device_prefix = "net";
    }

    public String getDevice_name() {
        return this.device_name;
    }

    public void setDevice_name(String device_name) {
	if (device_name == null)
		return;
		
        if (device_name.startsWith(this.device_prefix)) {
            this.device_name = device_name;
        } else {
            this.device_name = this.device_prefix + device_name;
        }
    }

    public String getDevice_prefix() {
        return this.device_prefix;
    }

    public void setDevice_prefix(String device_prefix) {
        this.device_prefix = device_prefix;
    }
}
