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
		RIBObject aux = new FakeRIBObject(null, "daf/management/naming/whatevercastnames", null, 0);
		rib.addRIBObject(aux);
		aux = new FakeRIBObject(null, "daf/management/naming/applicationprocessname", null, 0);
		rib.addRIBObject(aux);
		aux = new FakeRIBObject(null, "daf/management/naming/whatevercastnames/jore1", null, 0);
		rib.addRIBObject(aux);
		aux = new FakeRIBObject(null, "daf/management/enrollment/members", null, 0);
		rib.addRIBObject(aux);
		aux = new FakeRIBObject(null, "dif/management/members", null, 0);
		rib.addRIBObject(aux);
		aux = new FakeRIBObject(null, "daf/management/enrollment/members/nore3", null, 0);
		rib.addRIBObject(aux);
		aux = new FakeRIBObject(null, "daf/management/enrollment/members/nore5", null, 0);
		rib.addRIBObject(aux);
		List<RIBObject> result = rib.getRIBObjects();
		for(int i=0; i<result.size(); i++){
			System.out.println(result.get(i).getObjectName());
		}
	}
}
