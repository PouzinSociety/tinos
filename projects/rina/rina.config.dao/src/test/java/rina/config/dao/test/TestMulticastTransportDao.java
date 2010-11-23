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
