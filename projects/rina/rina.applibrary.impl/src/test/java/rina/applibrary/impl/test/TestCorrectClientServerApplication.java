package rina.applibrary.impl.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import rina.applibrary.api.ApplicationProcess;
import rina.applibrary.api.Flow;
import rina.applibrary.api.IPCException;

/**
 * Tests the normal course of events for both a client and a server application
 * @author eduardgrasa
 *
 */
public class TestCorrectClientServerApplication {
	
	private RINASoftwareServer RINAServer = null;
	private ExecutorService executorService = null;
	
	@Before
	/**
	 * Create and start the RINA Server in a separate thread
	 */
	public void setup(){
		RINAServer = new RINASoftwareServer();
		executorService = Executors.newFixedThreadPool(2);
		executorService.execute(RINAServer);
	}
	
	@After
	/**
	 * Stop the RINA Server
	 */
	public void teardown(){
		RINAServer.setEnd(true);
	}
	
	@Test
	public void testClientApplicationWithEchoServer() throws IPCException{
		SimpleSDUListener sduListener = new SimpleSDUListener();
		
		//1 Create a flow to the echo server application
		ApplicationProcess sourceApplication = new ApplicationProcess();
		sourceApplication.setApplicationProcessName("junit-test");
		ApplicationProcess destinationApplication = new ApplicationProcess();
		destinationApplication.setApplicationProcessName("echo-server");
		Flow flow = new Flow(sourceApplication, destinationApplication, null, sduListener);
		Assert.assertTrue(flow.isAllocated());
		
		System.out.println("Flow allocated, port Id = " + flow.getPortId());
		
		//2 Write an SDU, wait 1 second and see if the listener has received the SDU back from the 
		//echo server
		String sdu = "The rain in Spain stays mainly in the plain";
		flow.write(sdu.getBytes());
		wait1Second();
		Assert.assertEquals(sdu, sduListener.getLastSDU());
		
		//3 Do it again
		sdu = "Today it's very cold and about to snow";
		flow.write(sdu.getBytes());
		wait1Second();
		Assert.assertEquals(sdu, sduListener.getLastSDU());
		
		//4 Deallocate the flow
		flow.deallocate();
		Assert.assertFalse(flow.isAllocated());
		System.out.println("Flow deallocated");
	}
	
	private void wait1Second(){
		try{
			Thread.sleep(1000);
		}catch(InterruptedException ex){
			ex.printStackTrace();
		}
	}

}
