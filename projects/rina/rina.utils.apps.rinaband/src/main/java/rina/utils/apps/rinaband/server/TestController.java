package rina.utils.apps.rinaband.server;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import rina.applibrary.api.ApplicationRegistration;
import rina.applibrary.api.Flow;
import rina.applibrary.api.FlowListener;
import rina.applibrary.api.SDUListener;
import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.ObjectValue;
import rina.utils.apps.rinaband.StatisticsInformation;
import rina.utils.apps.rinaband.TestInformation;
import rina.utils.apps.rinaband.protobuf.RINABandStatisticsMessageEncoder;
import rina.utils.apps.rinaband.protobuf.RINABandTestMessageEncoder;

/**
 * Controls the negotiation of test parameters and 
 * the execution of a single test
 * @author eduardgrasa
 *
 */
public class TestController implements SDUListener, FlowListener{
	
	private enum State {WAIT_CREATE, WAIT_START, EXECUTING, COMPLETED};
	
	/**
	 * The state of the test
	 */
	private State state = State.WAIT_CREATE;
	
	/**
	 * The information of this test
	 */
	private TestInformation testInformation = null;
	
	/**
	 * The APNamingInfo associated to the data AE of the RINABand application
	 */
	private ApplicationProcessNamingInfo dataApNamingInfo = null;
	
	/**
	 * The control AE application registration
	 */
	private ApplicationRegistration dataRegistration = null;
	
	/**
	 * The CDAPSessionManager
	 */
	private CDAPSessionManager cdapSessionManager = null;
	
	/**
	 * The flow from the RINABand client
	 */
	private Flow flow = null;
	
	/**
	 * The map of allocated flows, with the 
	 * classes that deal with each individual flow
	 */
	private Map<Integer, TestWorker> allocatedFlows = null;
	
	/**
	 * Epoch times are in miliseconds
	 */
	private long epochTimeFirstSDUReceived = 0;
	private long epochTimeLastSDUReceived = 0;
	private int completedSends = 0;
	
	private long epochTimeFirstSDUSent = 0;
	private long epochTimeLastSDUSent = 0;
	private int completedReceives = 0;
	
	public TestController(ApplicationProcessNamingInfo dataApNamingInfo, Flow flow,
			CDAPSessionManager cdapSessionManager){
		this.dataApNamingInfo = dataApNamingInfo;
		this.cdapSessionManager = cdapSessionManager;
		this.flow = flow;
		this.allocatedFlows = new Hashtable<Integer, TestWorker>();
	}

	public void sduDelivered(byte[] sdu) {
		try{
			CDAPMessage cdapMessage = this.cdapSessionManager.decodeCDAPMessage(sdu);
			System.out.println("Received CDAP Message: "+cdapMessage.toString());
			switch(cdapMessage.getOpCode()){
			case M_CREATE:
				handleCreateMessageReceived(cdapMessage);
				break;
			case M_START:
				handleStartMessageReceived(cdapMessage);
				break;
			case M_STOP:
				handleStopMessageReceived(cdapMessage);
				break;
			default:
				printMessage("Received CDAP Message with wrong opcode, ignoring it.");
			}
		}catch(Exception ex){
			printMessage("Error decoding CDAP Message.");
			ex.printStackTrace();
		}
	}
	
	/**
	 * Check the data in the TestInformation object, change the values 
	 * that we do not agree with and register the Data AE that will 
	 * receive the test flow Allocations
	 * @param cdapMessage
	 */
	private void handleCreateMessageReceived(CDAPMessage cdapMessage){
		if (this.state != State.WAIT_CREATE){
			printMessage("Received CREATE Test message while not in WAIT_CREATE state." + 
			" Ignoring it.");
			return;
		}
		
		ObjectValue objectValue = cdapMessage.getObjValue();
		if (objectValue == null || objectValue.getByteval() == null){
			printMessage("The create message did not contain an object value. Ignoring the message");
			return;
		}
		
		try{
			//1 Decode and update the testInformation object
			this.testInformation = RINABandTestMessageEncoder.decode(objectValue.getByteval());
			this.testInformation.setAei(""+flow.getPortId());
			if (this.testInformation.getNumberOfFlows() > RINABandServer.MAX_NUMBER_OF_FLOWS){
				this.testInformation.setNumberOfFlows(RINABandServer.MAX_NUMBER_OF_FLOWS);
			}
			if (this.testInformation.getNumberOfSDUs() > RINABandServer.MAX_SDUS_PER_FLOW){
				this.testInformation.setNumberOfSDUs(RINABandServer.MAX_SDUS_PER_FLOW);
			}
			if (this.testInformation.getSduSize() > RINABandServer.MAX_SDU_SIZE_IN_BYTES){
				this.testInformation.setSduSize(RINABandServer.MAX_SDU_SIZE_IN_BYTES);
			}
			
			//2 Update the DATA AE and register it
			this.dataApNamingInfo.setApplicationEntityInstance(this.testInformation.getAei());
			this.dataRegistration = new ApplicationRegistration(this.dataApNamingInfo, this);
			
			//3 Reply and update state
			CDAPMessage replyMessage = cdapMessage.getReplyMessage();
			objectValue.setByteval(RINABandTestMessageEncoder.encode(this.testInformation));
			replyMessage.setObjValue(objectValue);
			sendCDAPMessage(replyMessage);
			this.state = State.WAIT_START;
			printMessage("Waiting to START a new test with the following parameters.");
			printMessage(this.testInformation.toString());
		}catch(Exception ex){
			printMessage("Error handling CREATE Test message.");
			ex.printStackTrace();
		}
		
	}
	
	/**
	 * Start the test. Be prepared to accept new flows, receive data and/or to create 
	 * new flows and write data
	 * @param cdapMessage
	 */
	private void handleStartMessageReceived(CDAPMessage cdapMessage){
		if (this.state != State.WAIT_START){
			printMessage("Received START Test message while not in WAIT_START state." + 
			" Ignoring it.");
			return;
		}
		
		Iterator<Entry<Integer, TestWorker>> iterator = allocatedFlows.entrySet().iterator();
		while(iterator.hasNext()){
			iterator.next().getValue().execute();
		}
		
		this.state = State.EXECUTING;
		printMessage("Started test execution");
	}
	
	private void handleStopMessageReceived(CDAPMessage cdapMessage){
		if (this.state != State.EXECUTING){
			printMessage("Received STOP Test message while not in EXECUTING state." + 
			" Ignoring it.");
			return;
		}
		
		int counter = 0;
		while (this.epochTimeLastSDUReceived == 0 || this.epochTimeLastSDUSent == 0){
			if (counter >=10){
				break;
			}
			
			try{
				printMessage("Waiting for the last SDU sent/received value");
				Thread.sleep(100);
				counter ++;
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		
		//1 Write statistics as response and print the stats
		try{
			//Update the statistics and send the M_STOP_R message
			StatisticsInformation statsInformation = RINABandStatisticsMessageEncoder.decode(cdapMessage.getObjValue().getByteval());
			if (this.testInformation.isClientSendsSDUs()){
				statsInformation.setServerTimeFirstSDUReceived(this.epochTimeFirstSDUReceived*1000L);
				statsInformation.setServerTimeLastSDUReceived(this.epochTimeLastSDUReceived*1000L);
			}
			if (this.testInformation.isServerSendsSDUs()){
				statsInformation.setServerTimeFirstSDUSent(this.epochTimeFirstSDUSent*1000L);
				statsInformation.setServerTimeLastSDUSent(this.epochTimeLastSDUSent*1000L);
			}
			printMessage(statsInformation.toString());
			ObjectValue objectValue = new ObjectValue();
			objectValue.setByteval(RINABandStatisticsMessageEncoder.encode(statsInformation));
			CDAPMessage responseMessage = CDAPMessage.getStopObjectResponseMessage(null, 0, null, cdapMessage.getInvokeID());
			responseMessage.setObjClass(cdapMessage.getObjClass());
			responseMessage.setObjName(cdapMessage.getObjName());
			responseMessage.setObjValue(objectValue);
			sendCDAPMessage(responseMessage);
			
			//Print aggregate statistics
			long averageClientServerDelay = 0L;
			long averageServerClientDelay = 0L;
			printMessage("Aggregate bandwidth:");
			if (this.testInformation.isClientSendsSDUs()){
				long aggregateReceivedSDUsPerSecond = 1000L*this.testInformation.getNumberOfFlows()*
					this.testInformation.getNumberOfSDUs()/(this.epochTimeLastSDUReceived-this.epochTimeFirstSDUReceived);
				printMessage("Aggregate received SDUs per second: "+aggregateReceivedSDUsPerSecond);
				averageClientServerDelay = ((this.epochTimeFirstSDUReceived - statsInformation.getClientTimeFirstSDUSent()/1000L) + 
						(this.epochTimeLastSDUReceived - statsInformation.getClientTimeLastSDUSent()/1000L))/2;
				printMessage("Aggregate received KiloBytes per second (KBps): "+ aggregateReceivedSDUsPerSecond*this.testInformation.getSduSize()/1024);
				printMessage("Aggregate received Megabits per second (Mbps): "+ aggregateReceivedSDUsPerSecond*this.testInformation.getSduSize()*8/(1024*1024));
			}
			if (this.testInformation.isServerSendsSDUs()){
				long aggregateSentSDUsPerSecond = 1000L*1000L*this.testInformation.getNumberOfFlows()*
					this.testInformation.getNumberOfSDUs()/(statsInformation.getClientTimeLastSDUReceived()-statsInformation.getClientTimeFirstSDUReceived());
				averageServerClientDelay = ((statsInformation.getClientTimeFirstSDUReceived()/1000L - this.epochTimeFirstSDUSent) + 
						(statsInformation.getClientTimeLastSDUReceived()/1000L - this.epochTimeLastSDUSent))/2;
				printMessage("Aggregate sent SDUs per second: "+aggregateSentSDUsPerSecond);
				printMessage("Aggregate sent KiloBytes per second (KBps): "+ aggregateSentSDUsPerSecond*this.testInformation.getSduSize()/1024);
				printMessage("Aggregate sent Megabits per second (Mbps): "+ aggregateSentSDUsPerSecond*this.testInformation.getSduSize()*8/(1024*1024));
			}
			long rttInMs = 0L;
			if (this.testInformation.isClientSendsSDUs() && this.testInformation.isServerSendsSDUs()){
				rttInMs = averageClientServerDelay + averageServerClientDelay;
			}else if (this.testInformation.isClientSendsSDUs()){
				rttInMs = averageClientServerDelay*2;
			}else{
				rttInMs = averageServerClientDelay*2;
			}
			printMessage("Estimated round-trip time (RTT) in ms: "+rttInMs);
		}catch(Exception ex){
			printMessage("Problems returning STOP RESPONSE message");
			ex.printStackTrace();
		}
		
		//2 Cancel the registration of the data AE
		this.state = State.COMPLETED;
		try{
			if (dataRegistration.isRegistered()){
				dataRegistration.unregister();
			}
		}catch(Exception ex){
			printMessage("Problems unregistering data AE");
			ex.printStackTrace();
		}
	}
	
	public synchronized void setLastSDUSent(long epochTime){
		this.completedSends++;
		if (this.completedSends == this.testInformation.getNumberOfFlows()){
			this.epochTimeLastSDUSent = epochTime;
			printMessage("Set last SDU sent time: "+epochTime);
		}
	}
	
	public synchronized void setLastSDUReceived(long epochTime){
		this.completedReceives++;
		if (this.completedReceives == this.testInformation.getNumberOfFlows()){
			this.epochTimeLastSDUReceived = epochTime;
			printMessage("Set last SDU received time: "+epochTime);
		}
	}
	
	public synchronized void setFirstSDUSent(long epochTime){
		if (this.epochTimeFirstSDUSent == 0){
			this.epochTimeFirstSDUSent = epochTime;
			printMessage("Set first SDU sent time: "+epochTime);
		}
	}
	
	public synchronized void setFirstSDUReveived(long epochTime){
		if (this.epochTimeFirstSDUReceived == 0){
			this.epochTimeFirstSDUReceived = epochTime;
			printMessage("Set first SDU received time: "+epochTime);
		}
	}

	/**
	 * Called when a new data flow has been allocated
	 */
	public synchronized void flowAllocated(Flow flow) {
		if (this.state != State.WAIT_START){
			printMessage("New flow allocated, but we're not in the WAIT_START state. Requesting deallocation.");
			try{
				flow.deallocate();
			}catch(Exception ex){
				ex.printStackTrace();
			}
			return;
		}
		
		TestWorker testWorker = new TestWorker(this.testInformation, flow, this);
		flow.setSDUListener(testWorker);
		this.allocatedFlows.put(new Integer(flow.getPortId()), testWorker);
		printMessage("Data flow with portId "+flow.getPortId()+ " allocated");
	}

	/**
	 * Called when an existing data flow has been deallocated
	 */
	public synchronized void flowDeallocated(Flow flow) {
		if (this.state == State.COMPLETED){
			this.allocatedFlows.remove(new Integer(flow.getPortId()));
			printMessage("Data flow with portId "+flow.getPortId()+ " deallocated");
		}
	}
	
	private void printMessage(String message){
		System.out.println("Test controller "+flow.getPortId()+": " + message);
	}
	
	private void sendCDAPMessage(CDAPMessage cdapMessage) throws Exception{
		byte[] sdu = cdapSessionManager.encodeCDAPMessage(cdapMessage);
		flow.write(sdu);
	}
	
}
