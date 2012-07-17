package rina.ipcservice.impl.test;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.impl.IPCProcessFactoryImpl;

public class IPCProcessFactoryImplTest {
	
	private IPCProcessFactoryImpl factory = null;
	
	@Before
	public void setup(){
		factory = new IPCProcessFactoryImpl();
		factory.setRibDaemonFactory(new MockRIBDaemonFactory());
		factory.setDelimiterFactory(new MockDelimiterFactory());
		factory.setEncoderFactory(new MockEncoderFactory());
		factory.setCDAPSessionManagerFactory(new MockCDAPSessionManagerFactory());
		factory.setEnrollmentTaskFactory(new MockEnrollmentTaskFactory());
		factory.setRmtFactory(new MockRMTFactory());
		factory.setDataTransferAEFactory(new MockDataTransferAEFactory());
		factory.setFlowAllocatorFactory(new MockFlowAllocatorFactory());
		factory.setIPCManager(new MockIPCManager());
	}
	
	@Test
	public void testFactory() throws Exception{
		IPCProcess ipcProcess = factory.createIPCProcess("test1", "1", null);
		Assert.assertNotNull(ipcProcess);
		
		ipcProcess = factory.getIPCProcess("test2", "1");
		Assert.assertNull(ipcProcess);
		
		ipcProcess = factory.getIPCProcess("test1", "1");
		Assert.assertNotNull(ipcProcess);
		
		factory.destroyIPCProcess("test1", "1");
		ipcProcess = factory.getIPCProcess("test1", "1");
		Assert.assertNull(ipcProcess);
		
		ipcProcess = factory.createIPCProcess("test1", "1", null);
		Assert.assertNotNull(ipcProcess);
		
		ipcProcess = factory.createIPCProcess("test2", "1", null);
		Assert.assertNotNull(ipcProcess);
		
		Assert.assertEquals(2, factory.listIPCProcesses().size());
		factory.destroyIPCProcess("test2", "1");
		Assert.assertEquals(1, factory.listIPCProcesses().size());
		ipcProcess = factory.getIPCProcess("test2", "1");
		Assert.assertNull(ipcProcess);
		
		Assert.assertNotNull(factory.getCDAPSessionManagerFactory());
		Assert.assertNotNull(factory.getDelimiterFactory());
		Assert.assertNotNull(factory.getEncoderFactory());
	}

}
