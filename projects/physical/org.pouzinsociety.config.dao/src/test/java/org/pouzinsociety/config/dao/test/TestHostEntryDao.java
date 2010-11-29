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
import junit.framework.TestCase;
import org.pouzinsociety.config.dao.HostEntryDao;

public class TestHostEntryDao extends TestCase{
	
	protected void setUp() {
		
	}
	
	protected void tearDown () {
		
	}
	
	public void testRouteXml() throws Exception {
		HostEntryDao dao = new HostEntryDao();
		dao.setHostname("node1");
		dao.setIpAddress(new String[] { "10.0.0.1" });
	
		System.out.println(dao.toString());
		String xml = HostEntryDao.toXML(dao);
		System.out.print(xml);

		
		dao.setHostname("node1");
		dao.setIpAddress(new String[] { "10.0.0.1", "10.0.0.2" });
	
		System.out.println(dao.toString());
		xml = HostEntryDao.toXML(dao);
		System.out.print(xml);
		
		List<HostEntryDao> list = HostEntryDao.fromXML(xml);
		assertEquals(dao.getHostname(), list.get(0).getHostname());
	}
	
	public void testXmlList() throws Exception {
		List<HostEntryDao> list = new LinkedList<HostEntryDao>();
		for (int i =0; i < 5; i++) {
			HostEntryDao dao = new HostEntryDao();
			dao.setHostname("node" + i);
			if ( i % 2 == 0)
				dao.setIpAddress(new String[] { "10.0.0." + i });
			else
				dao.setIpAddress(new String[] { "10.0.0." + i, "10.0.1." + i });
			
			list.add(dao);
		}
		String xml = HostEntryDao.toXML(list);
		System.out.println("XML:(" + xml +")");
		List<HostEntryDao> hostList = HostEntryDao.fromXML(xml);

		for (int i = 0; i < hostList.size(); i++) {
			HostEntryDao host = hostList.get(i);
			System.out.println(host);
		}
		assertEquals(list.size(), hostList.size());	
	}
}
