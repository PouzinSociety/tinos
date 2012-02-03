package rina.applibrary.impl.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import rina.applibrary.api.ApplicationRegistration;
import rina.applibrary.api.Flow;
import rina.applibrary.api.IPCException;
import rina.ipcservice.api.ApplicationProcessNamingInfo;

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
		ApplicationProcessNamingInfo sourceApplication = new ApplicationProcessNamingInfo();
		sourceApplication.setApplicationProcessName("junit-test");
		ApplicationProcessNamingInfo destinationApplication = new ApplicationProcessNamingInfo();
		destinationApplication.setApplicationProcessName("echo-server");
		Flow flow = new Flow(sourceApplication, destinationApplication, null, sduListener);
		Assert.assertTrue(flow.isAllocated());
		
		System.out.println("Flow allocated, port Id = " + flow.getPortId());
		
		//2 Write an SDU, wait 1 second and see if the listener has received the SDU back from the 
		//echo server
		String sdu = "The rain in Spain stays mainly in the plain";
		flow.write(sdu.getBytes());
		wait2Seconds();
		Assert.assertEquals(sdu, sduListener.getLastSDU());
		
		//3 Do it again
		sdu = "Today it's very cold and about to snow";
		flow.write(sdu.getBytes());
		wait2Seconds();
		Assert.assertEquals(sdu, sduListener.getLastSDU());
		
		//4 Deallocate the flow
		flow.deallocate();
		Assert.assertFalse(flow.isAllocated());
		System.out.println("Flow deallocated");
	}
	
	@Test
	public void testBlockingServerApplicationWithClient() throws IPCException{
		//1 Register this application with the RINAServer
		ApplicationProcessNamingInfo applicationProcess = new ApplicationProcessNamingInfo();
		applicationProcess.setApplicationProcessName("echo-server");
		applicationProcess.setApplicationProcessInstance("1");
		
		ApplicationRegistration registration = new ApplicationRegistration(applicationProcess);
		Assert.assertFalse(registration.isUnregistered());
		
		//Block until a new flow is accepted
		SimpleSDUListener sduListener = new SimpleSDUListener();
		Flow flow = registration.accept(sduListener);
		Assert.assertTrue(flow.isAllocated());
		System.out.println("Server: Accepted a flow! PortId = " + flow.getPortId());
		
		//wait 1 second, read the SDU
		wait2Seconds();
		Assert.assertEquals("Switzerland is a good country if you like mountains", sduListener.getLastSDU());
		System.out.println("Server: Received SDU, echoing it back! "+sduListener.getLastSDU());
		flow.write(sduListener.getLastSDU().getBytes());
		
		//wait 1 second, read the SDU
		wait2Seconds();
		Assert.assertEquals("And it can be even a better country if you also like cheese", sduListener.getLastSDU());
		System.out.println("Server: Received SDU, echoing it back! "+sduListener.getLastSDU());
		flow.write(sduListener.getLastSDU().getBytes());
		
		//wait 1 second, check that the flow is unallocated
		wait2Seconds();
		Assert.assertFalse(flow.isAllocated());
		
		//unregister the application
		registration.unregister();
		Assert.assertTrue(registration.isUnregistered());
	}
	
	@Test
	public void testNonBlockingServerApplicationWithClient() throws IPCException{
		//1 Register this application with the RINAServer
		ApplicationProcessNamingInfo applicationProcess = new ApplicationProcessNamingInfo();
		applicationProcess.setApplicationProcessName("echo-server");
		applicationProcess.setApplicationProcessInstance("1");
		
		SimpleSDUListener sduListener = new SimpleSDUListener();
		SimpleFlowListener flowListener = new SimpleFlowListener(sduListener);
		ApplicationRegistration registration = new ApplicationRegistration(applicationProcess, flowListener);
		
		//wait 1 second, check that there is already a flow accepted
		wait2Seconds();
		Flow flow = flowListener.getFlow();
		Assert.assertTrue(flow.isAllocated());
		System.out.println("Server: Accepted a flow! PortId = " + flow.getPortId());
		
		//wait 1 second, read the SDU
		wait2Seconds();
		Assert.assertEquals("Switzerland is a good country if you like mountains", sduListener.getLastSDU());
		System.out.println("Server: Received SDU, echoing it back! "+sduListener.getLastSDU());
		flow.write(sduListener.getLastSDU().getBytes());
		
		//wait 1 second, read the SDU
		wait2Seconds();
		Assert.assertEquals("And it can be even a better country if you also like cheese", sduListener.getLastSDU());
		System.out.println("Server: Received SDU, echoing it back! "+sduListener.getLastSDU());
		flow.write(sduListener.getLastSDU().getBytes());
		
		//wait 1 second, check that the flow is unallocated
		wait2Seconds();
		Assert.assertFalse(flow.isAllocated());
		
		//unregister the application
		registration.unregister();
		Assert.assertTrue(registration.isUnregistered());
	}
	
	private void wait2Seconds(){
		try{
			Thread.sleep(2000);
		}catch(InterruptedException ex){
			ex.printStackTrace();
		}
	}

}