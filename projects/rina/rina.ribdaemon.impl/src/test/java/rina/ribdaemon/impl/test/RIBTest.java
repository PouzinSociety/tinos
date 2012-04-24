package rina.ribdaemon.impl.test;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.impl.rib.RIB;

public class RIBTest {

	private RIB rib = null;
	
	@Before
	public void setup(){
		rib = new RIB();
	}
	
	@Test
	public void testRIBNodes() throws RIBDaemonException{
		//TODO
	}
	
	@Test
	public void testPrintRIB() throws RIBDaemonException{
		RIBObject aux = new FakeRIBObject(null, null, 0L, "daf/management/naming/whatevercastnames");
		rib.addRIBObject(aux);
		aux = new FakeRIBObject(null, null, 0L, "daf/management/naming/applicationprocessname");
		rib.addRIBObject(aux);
		aux = new FakeRIBObject(null, null, 0L, "daf/management/naming/whatevercastnames/jore1");
		rib.addRIBObject(aux);
		aux = new FakeRIBObject(null, null, 0L, "daf/management/enrollment/members");
		rib.addRIBObject(aux);
		aux = new FakeRIBObject(null, null, 0L, "dif/management/members");
		rib.addRIBObject(aux);
		aux = new FakeRIBObject(null, null, 0L, "daf/management/enrollment/members/nore3");
		rib.addRIBObject(aux);
		aux = new FakeRIBObject(null, null, 0L, "daf/management/enrollment/members/nore5");
		rib.addRIBObject(aux);
		List<RIBObject> result = rib.getRIBObjects();
		for(int i=0; i<result.size(); i++){
			System.out.println(result.get(i).getObjectName());
		}
	}
}
