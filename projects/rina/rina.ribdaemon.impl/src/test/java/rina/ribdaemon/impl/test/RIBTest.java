package rina.ribdaemon.impl.test;

import org.junit.Before;
import org.junit.Test;

import rina.ribdaemon.api.RIBDaemonException;
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
}
