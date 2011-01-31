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
package org.pouzinsociety.transport.im.configmanager;

import java.util.List;
import org.pouzinsociety.config.dao.*;

public class NodeConfigDao {
	List<EthernetOverIMDao> interfaces;
	List<RouteDao> routes;
	List<HostEntryDao> hosts;
	
	public List<EthernetOverIMDao> getInterfaces() {
		return interfaces;
	}
	public void setInterfaces(List<EthernetOverIMDao> interfaces) {
		this.interfaces = interfaces;
	}
	public List<RouteDao> getRoutes() {
		return routes;
	}
	public void setRoutes(List<RouteDao> routes) {
		this.routes = routes;
	}
	public List<HostEntryDao> getHosts() {
		return hosts;
	}
	public void setHosts(List<HostEntryDao> hosts) {
		this.hosts = hosts;
	}
}
