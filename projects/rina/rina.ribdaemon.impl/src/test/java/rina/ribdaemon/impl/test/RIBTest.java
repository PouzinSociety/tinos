package rina.ribdaemon.impl.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObjectNames;
import rina.ribdaemon.impl.rib.RIB;
import rina.ribdaemon.impl.rib.RIBNode;

public class RIBTest {

	private RIB rib = null;
	
	@Before
	public void setup(){
		rib = new RIB();
	}
	
	@Test
	public void testRIBNodes() throws RIBDaemonException{
		RIBNode ribNode = rib.getRIBNode(RIBObjectNames.DAF);
		Assert.assertNotNull(ribNode);
		ribNode = rib.getRIBNode(RIBObjectNames.DAF+RIBObjectNames.SEPARATOR+RIBObjectNames.MANAGEMENT);
		Assert.assertNotNull(ribNode);
		ribNode = rib.getRIBNode(RIBObjectNames.DAF+RIBObjectNames.SEPARATOR+RIBObjectNames.MANAGEMENT+RIBObjectNames.SEPARATOR+
				RIBObjectNames.ENROLLMENT);
		Assert.assertNotNull(ribNode);
		ribNode = rib.getRIBNode(RIBObjectNames.DAF+RIBObjectNames.SEPARATOR+RIBObjectNames.MANAGEMENT+RIBObjectNames.SEPARATOR+
				RIBObjectNames.OPERATIONAL_STATUS);
		Assert.assertNotNull(ribNode);
		ribNode = rib.getRIBNode(RIBObjectNames.DAF+RIBObjectNames.SEPARATOR+RIBObjectNames.MANAGEMENT+RIBObjectNames.SEPARATOR+
				RIBObjectNames.NAMING);
		Assert.assertNotNull(ribNode);
		ribNode = rib.getRIBNode(RIBObjectNames.DAF+RIBObjectNames.SEPARATOR+RIBObjectNames.MANAGEMENT+RIBObjectNames.SEPARATOR+
				RIBObjectNames.NAMING+RIBObjectNames.SEPARATOR+RIBObjectNames.APNAME);
		Assert.assertNotNull(ribNode);
		ribNode = rib.getRIBNode(RIBObjectNames.DAF+RIBObjectNames.SEPARATOR+RIBObjectNames.MANAGEMENT+RIBObjectNames.SEPARATOR+
				RIBObjectNames.NAMING+RIBObjectNames.SEPARATOR+RIBObjectNames.CURRENT_SYNONYM);
		Assert.assertNotNull(ribNode);
		ribNode = rib.getRIBNode(RIBObjectNames.DAF+RIBObjectNames.SEPARATOR+RIBObjectNames.MANAGEMENT+RIBObjectNames.SEPARATOR+
				RIBObjectNames.NAMING+RIBObjectNames.SEPARATOR+RIBObjectNames.SYNONYMS);
		Assert.assertNotNull(ribNode);
		ribNode = rib.getRIBNode(RIBObjectNames.DAF+RIBObjectNames.SEPARATOR+RIBObjectNames.MANAGEMENT+RIBObjectNames.SEPARATOR+
				RIBObjectNames.NAMING+RIBObjectNames.SEPARATOR+RIBObjectNames.WHATEVERCAST_NAMES);
		Assert.assertNotNull(ribNode);
		ribNode = rib.getRIBNode(RIBObjectNames.DIF);
		Assert.assertNotNull(ribNode);
	}
}
