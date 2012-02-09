package rina.examples.apps.cliclient;

import java.util.Calendar;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import rina.applibrary.api.Flow;
import rina.applibrary.api.SDUListener;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.IPCException;

public class EchoClient implements SDUListener{
	
	public static final String ECHO_SERVER_APPLICATION_PROCESS_NAME = "/rina/examples/apps/echoServer";
	public static final String APPLICATION_PROCESS_NAME = "/rina/examples/apps/cliClient";
	
	private Scanner scanner = null;
	private String sent = null;
	private String received = null;
	private Flow flow = null;
	private CountDownLatch latch = null;
	private long before = 0;
	private long time = 0;
	
	public EchoClient(){
		System.out.println("Welcome to the Echo Server Client.");
		scanner = new Scanner(System.in);
		latch = new CountDownLatch(1);
	}
	
	public void run(){
		System.out.println("Requesting a flow to the "+ECHO_SERVER_APPLICATION_PROCESS_NAME+" application...");
		try{
			flow = new Flow(new ApplicationProcessNamingInfo(APPLICATION_PROCESS_NAME, null), 
					new ApplicationProcessNamingInfo(ECHO_SERVER_APPLICATION_PROCESS_NAME, null), null, this);
			System.out.println("Flow allocated! The portId assigned to the flow is "+flow.getPortId());
		}catch(IPCException ex){
			System.out.println("Problems allocating flow: ");
			ex.printStackTrace();
			return;
		}

		while (true){
			System.out.print("Enter the sentence to be echoed back, or 'exit' to finish: ");
			sent = scanner.nextLine();
			
			if (sent.equals("exit")){
				break;
			}
			
			try{
				before = Calendar.getInstance().getTimeInMillis();
				flow.write(sent.getBytes());
				latch.await();
				time = Calendar.getInstance().getTimeInMillis() - before;
				System.out.println("Received reply in "+time+" miliseconds: "+received);
				latch = new CountDownLatch(1);
			}catch(Exception ex){
				ex.printStackTrace();
			}			
		}
		
		try{
			System.out.println("Deallocating flow ...");
			flow.deallocate();
			System.out.println("Flow deallocated!");
		}catch(IPCException ex){
			System.out.println("Problems deallocating flow");
			ex.printStackTrace();
		}
		
		System.out.println("Have a good day, bye bye!");
	}

	public void sduDelivered(byte[] sdu) {
		received = new String(sdu);
		try{
			latch.countDown();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}

}
