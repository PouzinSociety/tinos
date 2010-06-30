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
import org.pouzinsociety.config.dao.EthernetOverIMDao;
import junit.framework.TestCase;

public class TestEthernetOverIMDao extends TestCase {
	protected void setUp() {	
	}
	
	protected void tearDown () {
	}
	
	public void testXml() throws Exception {
		EthernetOverIMDao dao = new EthernetOverIMDao();
		dao.setNode_name("node1");
		dao.setDevice_name("0");
		dao.setEthernetAddress("DE-AD-BE-EF-00-00");
		dao.setIm_server("localhost");
		dao.setIm_port("5222");
		dao.setIm_buddyId("tester");
		dao.setIm_buddyPassword("tester");
		dao.setIm_chatroom("subnet1@conference.chimera");
		dao.setIp_address("10.0.0.1");
		dao.setIp_netmask("255.255.255.0");
		
		String xml = EthernetOverIMDao.toXML(dao);
		System.out.println(xml);
		
		List<EthernetOverIMDao> ifaceList = EthernetOverIMDao.fromXML(xml);
		assertEquals(dao.getEthernetAddress(), ifaceList.get(0).getEthernetAddress());
	}
	
	public void testXmlList() throws Exception {
		List<EthernetOverIMDao> list = new LinkedList<EthernetOverIMDao>();
		for (int i = 0; i< 5; i++) {
			EthernetOverIMDao dao = new EthernetOverIMDao();
			dao.setNode_name("node1");
			dao.setDevice_name(new Integer(i).toString());
			dao.setEthernetAddress("DE-AD-BE-EF-00-0" + i);
			dao.setIm_server("localhost");
			dao.setIm_port("5222");
			dao.setIm_buddyId("tester" + i);
			dao.setIm_buddyPassword("tester" + i);
			dao.setIm_chatroom("subnet" + i + "@conference.chimera");
			dao.setIp_address("10.0." + i + ".1");
			dao.setIp_netmask("255.255.255.0");			
			list.add(dao);
		}
		String xml = EthernetOverIMDao.toXML(list);
		System.out.println(xml);
		List<EthernetOverIMDao> ifaceList = EthernetOverIMDao.fromXML(xml);
		for (int i = 0; i < ifaceList.size(); i++) {
			EthernetOverIMDao iface = ifaceList.get(i);
			System.out.println(iface);
		}
		
		assertEquals(list.size(), ifaceList.size());
	}

}
