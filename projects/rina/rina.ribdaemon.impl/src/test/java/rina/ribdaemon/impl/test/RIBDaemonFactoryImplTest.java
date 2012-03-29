package rina.ribdaemon.impl.test;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.ribdaemon.api.RIBDaemon;
import rina.ribdaemon.impl.RIBDaemonFactoryImpl;

public class RIBDaemonFactoryImplTest {

	private RIBDaemonFactoryImpl ribDaemonFactory = null;
	
	@Before
	public void setup(){
		ribDaemonFactory = new RIBDaemonFactoryImpl();
	}
	
	@Test
	public void test(){
		RIBDaemon ribDaemon = ribDaemonFactory.createRIBDaemon(new ApplicationProcessNamingInfo("test", "1"));
		Assert.assertNotNull(ribDaemon);
		ribDaemon = ribDaemonFactory.getRIBDaemon(new ApplicationProcessNamingInfo("test", "1"));
		Assert.assertNotNull(ribDaemon);
		ribDaemonFactory.destroyRIBDaemon(new ApplicationProcessNamingInfo("test", "1"));
		ribDaemon = ribDaemonFactory.getRIBDaemon(new ApplicationProcessNamingInfo("test", "2"));
		Assert.assertNull(ribDaemon);
	}
}
