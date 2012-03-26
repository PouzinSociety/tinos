package rina.rmt.impl.tcp.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.rmt.api.BaseRMT;
import rina.rmt.impl.tcp.TCPRMTImpl;

/**
 * Test the RMT implementation on its "called site": the RMT will 
 * accept a remote connection from the test program, who pretends 
 * to be the calling IPC process
 * @author eduardgrasa
 *
 */
public class TCPRMTImplCalledTest {
	
	private TCPRMTImpl rmt = null;
	private TestTCPServer remoteIPCProcess = null;
	private ExecutorService executorService = null;
	private FakeRIBDaemon ribdaemon = null;
	
	@Before
	public void setup(){
		System.setProperty(BaseRMT.RMT_PORT_PROPERTY, ""+40001);
		this.rmt = new TCPRMTImpl();
		IPCProcess fakeIPCProcess = new FakeIPCProcess();
		fakeIPCProcess.addIPCProcessComponent(rmt);
		this.ribdaemon = (FakeRIBDaemon) fakeIPCProcess.getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		this.executorService = Executors.newFixedThreadPool(3);
		remoteIPCProcess = new TestTCPServer();
		executorService.execute(remoteIPCProcess);
	}
	
	@Test
	public void testConnectionFromRemoteProcess() throws Exception{
		int portId = rmt.allocateFlow("test", null);
		rmt.sendCDAPMessage(portId, "Request message".getBytes());
		Thread.sleep(2000);
		Assert.assertTrue(ribdaemon.isMessageReceived());
	}

}
