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
package rina.config.dao.test;

import java.util.ArrayList;
import java.util.List;
import rina.config.dao.MulticastTransportConnectionDao;
import rina.config.dao.MulticastTransportDao;
import junit.framework.TestCase;

public class TestMulticastTransportDao extends TestCase {
	protected void setUp() {
		
	}
	
	protected void tearDown () {
		
	}
	
	public void testDaoXML() throws Exception {
		
		List<MulticastTransportConnectionDao> mediumList = new ArrayList<MulticastTransportConnectionDao>();
		mediumList.add(new MulticastTransportConnectionDao("O2", "node1", "225.1.2.3", "3000", "im0"));
		mediumList.add(new MulticastTransportConnectionDao("External", "gateway", "225.1.4.3", "3000", "im1"));
		
		MulticastTransportDao sm = new MulticastTransportDao("O2", "node1", mediumList);
		
		String xml = MulticastTransportDao.toXML(sm);
		System.out.print(xml);
		
		/* Output :
<MulticastTransport domain="O2" node="node1">
<MulticastTransportConnection domain="O2" multicastAddr="225.1.2.3" multicastPort="3000" interface="im0" />
<MulticastTransportConnection domain="VF" multicastAddr="225.1.4.3" multicastPort="3000" interface="im1" />
</MulticastTransport>
		*/
		
		MulticastTransportDao dao = MulticastTransportDao.fromXML(xml);
		System.out.print(dao);
		
	}
	
}
