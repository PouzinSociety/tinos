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
package org.pouzinsociety.config.dao.test;

import java.util.LinkedList;
import java.util.List;
import org.pouzinsociety.config.dao.RouteDao;
import junit.framework.TestCase;

public class TestRouteDao extends TestCase {
	
	protected void setUp() {
		
	}
	
	protected void tearDown () {
		
	}
	
	public void testRouteXml() throws Exception {
		RouteDao route = new RouteDao();
		route.setTarget("10.0.0.0");
		route.setNetmask("255.255.255.0");
		route.setDevice("im0");
		
		String xml = RouteDao.toXML(route);
		System.out.print(xml);
		
		RouteDao route1 = new RouteDao();
		List<RouteDao> routeList = RouteDao.fromXML(xml);
		
		assertEquals(1, routeList.size());
		
		route1 = routeList.get(0);
		assertEquals(route.getTarget(), route1.getTarget());
	}
	
	public void testRouteXmlList() throws Exception {
		List<RouteDao> list = new LinkedList<RouteDao>();
		for (int i =0; i < 5; i++) {
			RouteDao route = new RouteDao();
			route.setTarget("10.0.0." + i);
			route.setNetmask("255.255.255.0");
			route.setDevice("im" + i);
			list.add(route);
		}
		String xml = RouteDao.toXML(list);
		System.out.println("XML:(" + xml +")");
		List<RouteDao> routeList = RouteDao.fromXML(xml);

		for (int i = 0; i < routeList.size(); i++) {
			RouteDao route = routeList.get(i);
			System.out.println(route);
		}
		
		assertEquals(list.size(), routeList.size());
		

		
	}

}
