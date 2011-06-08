package rina.rmt.impl.tcp.test;

import java.net.Socket;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import rina.delimiting.api.Delimiter;
import rina.ipcprocess.api.IPCProcess;
import rina.rmt.impl.tcp.TCPRMTImpl;

/**
 * Test the RMT implementation on its "caller site": the RMT will 
 * allocate a new flow to the test program, who pretends 
 * to be a remote IPC process listening on a certain port
 * @author eduardgrasa
 *
 */
public class TCPRMTImplCallerTest {
	
	private TCPRMTImpl rmt = null;
	private Delimiter delimiter = null;
	private FakeRIBDaemon ribdaemon = null;
	
	@Before
	public void setup(){
		this.rmt = new TCPRMTImpl(40001);
		IPCProcess fakeIPCProcess = new FakeIPCProcess();
		this.rmt.setIPCProcess(fakeIPCProcess);
		fakeIPCProcess.setRmt(rmt);
		this.delimiter = fakeIPCProcess.getDelimiter();
		this.ribdaemon = (FakeRIBDaemon) fakeIPCProcess.getRibDaemon();
	}
	
	@Test
	public void testConnectionFromRemoteProcess() throws Exception{
		byte[] buffer = new byte[50];

		Socket clientSocket = new Socket("localhost", 40001);
		byte[] delimitedSdu = delimiter.getDelimitedSdu("CDAP message coming".getBytes());
		clientSocket.getOutputStream().write(delimitedSdu);
		try{
			Thread.sleep(1000);
		}catch(InterruptedException ex){
			ex.printStackTrace();
		}
		clientSocket.getInputStream().read(buffer);
		String reply = new String(buffer);
		System.out.println(reply);
		Assert.assertTrue(ribdaemon.isMessageReceived());
		Assert.assertTrue(buffer[0]==27);
	}

}
