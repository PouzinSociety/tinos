package rina.utils.apps.rinaband.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rina.applibrary.api.Flow;
import rina.applibrary.api.SDUListener;
import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.ObjectValue;
import rina.cdap.impl.CDAPSessionManagerImpl;
import rina.cdap.impl.googleprotobuf.GoogleProtocolBufWireMessageProviderFactory;
import rina.utils.apps.rinaband.StatisticsInformation;
import rina.utils.apps.rinaband.TestInformation;
import rina.utils.apps.rinaband.protobuf.RINABandStatisticsMessageEncoder;
import rina.utils.apps.rinaband.protobuf.RINABandTestMessageEncoder;

/**
 * Implements the behavior of a RINABand Client
 * @author eduardgrasa
 *
 */
public class RINABandClient implements SDUListener{
	
	public static final String TEST_OBJECT_CLASS = "RINAband test";
	public static final String TEST_OBJECT_NAME = "/rina/utils/apps/rinaband/test";
	public static final String STATISTICS_OBJECT_CLASS = "RINAband statistics";
	public static final String STATISTICS_OBJECT_NAME = "/rina/utils/apps/rinaband/statistics";

	private enum State {NULL, WAIT_CREATE_R, EXECUTING, STOP_REQUESTED, COMPLETED};
	
	private enum Type{FLOW_ALLOCATION, FLOW_DEALLOCATION, SENT_SDUS, RECEIVED_SDUS};
	
	/**
	 * The state of the client
	 */
	private State state = State.NULL;
	
	/**
	 * The data about the test to carry out
	 */
	private TestInformation testInformation = null;
	
	/**
	 * The APNamingInfo associated to the control AE of the RINABand application
	 */
	private ApplicationProcessNamingInfo controlApNamingInfo = null;
	
	/**
	 * The APNamingInfo associated to the data AE of the RINABand application
	 */
	private ApplicationProcessNamingInfo dataApNamingInfo = null;
	
	/**
	 * The client AP Naming Information
	 */
	private ApplicationProcessNamingInfo clientApNamingInfo = null;
	
	/**
	 * The flow to the RINABand server control AE
	 */
	private Flow controlFlow = null;
	
	/**
	 * The CDAP Parser
	 */
	private CDAPSessionManager cdapSessionManager = null;
	
	/**
	 * The test workers
	 */
	private List<TestWorker> testWorkers = null;
	
	/**
	 * The thread pool
	 */
	private ExecutorService executorService = null;
	
	/**
	 * The number of test workers that have completed their individual flow test
	 */
	private int completedTestWorkers = 0;
	
	/**
	 * Epoch times are in miliseconds
	 */
	private long epochTimeFirstSDUReceived = 0;
	private long epochTimeLastSDUReceived = 0;
	private int completedSends = 0;
	
	private long epochTimeFirstSDUSent = 0;
	private long epochTimeLastSDUSent = 0;
	private int completedReceives = 0;
	
	public RINABandClient(TestInformation testInformation, ApplicationProcessNamingInfo controlApNamingInfo, 
			ApplicationProcessNamingInfo dataApNamingInfo){
		this.testInformation = testInformation;
		this.controlApNamingInfo = controlApNamingInfo;
		this.dataApNamingInfo = dataApNamingInfo;
		this.clientApNamingInfo = new ApplicationProcessNamingInfo("rina.utils.apps.rinabandclient", null, null, null);
		this.cdapSessionManager = new CDAPSessionManagerImpl(new GoogleProtocolBufWireMessageProviderFactory());
		this.testWorkers = new ArrayList<TestWorker>();
		this.executorService = Executors.newCachedThreadPool();
	}
	
	public void execute(){
		try{
			//1 Allocate a flow to the RINABand Server control AE
			this.controlFlow = new Flow(this.clientApNamingInfo, this.controlApNamingInfo, null, this);
			
			//2 Send the create test message
			ObjectValue objectValue = new ObjectValue();
			objectValue.setByteval(RINABandTestMessageEncoder.encode(this.testInformation));
			CDAPMessage cdapMessage = CDAPMessage.getCreateObjectRequestMessage(
					null, null, TEST_OBJECT_CLASS, 0, TEST_OBJECT_NAME, objectValue, 0);
			cdapMessage.setInvokeID(1);
			this.controlFlow.write(this.cdapSessionManager.encodeCDAPMessage(cdapMessage));
			System.out.println("Requested a new test with the following parameters:");
			System.out.println(this.testInformation.toString());
			
			//3 Update the state
			this.state = State.WAIT_CREATE_R;
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	/**
	 * Called when an SDU is available on the control flow
	 */
	public void sduDelivered(byte[] sdu){
		try{
			CDAPMessage cdapMessage = this.cdapSessionManager.decodeCDAPMessage(sdu);
			switch(cdapMessage.getOpCode()){
			case M_CREATE_R:
				handleCreateResponseReceived(cdapMessage);
				break;
			case M_STOP_R:
				handleStopResponseReceived(cdapMessage);
				break;
			default:
				abortTest("Received wrong CDAP Message");
			}
		}catch(Exception ex){
			ex.printStackTrace();
			abortTest("Problems decoding CDAP Message");
		}
	}
	
	/**
	 * Called when a create response message has been received
	 * @param cdapMessage
	 */
	private void handleCreateResponseReceived(CDAPMessage cdapMessage){
		if (this.state != State.WAIT_CREATE_R){
			abortTest("Received CREATE Response message while not in WAIT_CREATE_R state");
		}
		
		if (cdapMessage.getResult() != 0){
			abortTest("Received unsuccessful CREATE Response message. Reason: "+cdapMessage.getResultReason());
		}
		
		ObjectValue objectValue = cdapMessage.getObjValue();
		if (objectValue == null || objectValue.getByteval() == null){
			abortTest("Object value is null");
		}
		
		try{
			//1 Update the testInformation and dataApNamingInfo
			this.testInformation = RINABandTestMessageEncoder.decode(objectValue.getByteval());
			this.dataApNamingInfo.setApplicationEntityInstance(this.testInformation.getAei());
			
			System.out.println("Starting a new test with the following parameters:");
			System.out.println(this.testInformation.toString());
			
			//Setup all the flows
			Flow flow = null;
			TestWorker testWorker = null;
			long before = 0;
			for(int i=0; i<this.testInformation.getNumberOfFlows(); i++){
				try{
					testWorker = new TestWorker(this.testInformation, this);
					before = System.currentTimeMillis();
					flow =  new Flow(this.clientApNamingInfo, this.dataApNamingInfo, null, testWorker);
					testWorker.setFlow(flow, System.currentTimeMillis() - before);
					this.testWorkers.add(testWorker);
				}catch(Exception ex){
					System.out.println("Flow setup failed");
					ex.printStackTrace();
				}
			}
			this.state = State.EXECUTING;
			
			System.out.println("Allocated "+this.testWorkers.size()+" flows. Executing the test ...");
			
			//2 Tell the server to start the test
			CDAPMessage startTestMessage = CDAPMessage.getStartObjectRequestMessage(
					null, null, TEST_OBJECT_CLASS, null, 0, TEST_OBJECT_NAME, 0);
			this.controlFlow.write(this.cdapSessionManager.encodeCDAPMessage(startTestMessage));
			
			//3 If the client has to send SDUs, execute the TestWorkers in separate threads
			if (this.testInformation.isClientSendsSDUs()){
				for(int i=0; i<this.testWorkers.size(); i++){
					this.executorService.execute(this.testWorkers.get(i));
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
			abortTest("Problems processing M_CREATE Test response");
		}
	}
	
	public synchronized void setLastSDUSent(long epochTime){
		this.completedSends++;
		if (this.completedSends == this.testInformation.getNumberOfFlows()){
			this.epochTimeLastSDUSent = epochTime;
		}
	}
	
	public synchronized void setLastSDUReceived(long epochTime){
		this.completedReceives++;
		if (this.completedReceives == this.testInformation.getNumberOfFlows()){
			this.epochTimeLastSDUReceived = epochTime;
		}
	}
	
	public synchronized void setFirstSDUSent(long epochTime){
		if (this.epochTimeFirstSDUSent == 0){
			this.epochTimeFirstSDUSent = epochTime;
		}
	}
	
	public synchronized void setFirstSDUReveived(long epochTime){
		if (this.epochTimeFirstSDUReceived == 0){
			this.epochTimeFirstSDUReceived = epochTime;
		}
	}
	
	/**
	 * Called by the test worker when he has finished sending/receiving SDUs
	 * @param testWorker
	 */
	public synchronized void testCompleted(TestWorker testWorker){
		this.completedTestWorkers++;
		if (this.completedTestWorkers < this.testWorkers.size()){
			return;
		}
		
		//All the tests have completed
		try{
			//1 Unallocate the data flows
			TestWorker currentWorker = null;
			long before = 0;
			for(int i=0; i<this.testWorkers.size(); i++){
				currentWorker = this.testWorkers.get(i);
				before = System.currentTimeMillis();
				currentWorker.abortTest();
				currentWorker.getStatistics().setFlowTearDownTimeInMillis(System.currentTimeMillis()-before);
			}
			
			//2 Tell the server to STOP the test
			StatisticsInformation statisticsInformation = new StatisticsInformation();
			if (this.testInformation.isServerSendsSDUs()){
				statisticsInformation.setClientTimeLastSDUSent(this.epochTimeLastSDUSent*1000L);
				statisticsInformation.setClientTimeLastSDUReceived(this.epochTimeLastSDUReceived*1000L);
			}
			if (this.testInformation.isClientSendsSDUs()){
				statisticsInformation.setClientTimeFirstSDUSent(this.epochTimeFirstSDUSent*1000L);
				statisticsInformation.setClientTimeFirstSDUReceived(this.epochTimeFirstSDUReceived*1000L);
			}
			ObjectValue objectValue = new ObjectValue();
			objectValue.setByteval(RINABandStatisticsMessageEncoder.encode(statisticsInformation));
			CDAPMessage stopTestMessage = CDAPMessage.getStopObjectRequestMessage(
					null, null, STATISTICS_OBJECT_CLASS, objectValue, 0, STATISTICS_OBJECT_NAME, 0);
			stopTestMessage.setInvokeID(1);
			this.controlFlow.write(this.cdapSessionManager.encodeCDAPMessage(stopTestMessage));
			System.out.println("Requested the RINABand server to stop the test and to get statistics back");
			this.state = State.STOP_REQUESTED;
		}catch(Exception ex){
			ex.printStackTrace();
			abortTest("Problems cleaning up the test resources");
		}
	}
		
	private void handleStopResponseReceived(CDAPMessage stopResponse){
		try{
			System.out.println("Test successfully completed!");
			StatisticsInformation statsInformation = RINABandStatisticsMessageEncoder.decode(stopResponse.getObjValue().getByteval());
			
			//1 Print the individual statistics
			TestWorker currentWorker = null;
			for(int i=0; i<this.testWorkers.size(); i++){
				currentWorker = this.testWorkers.get(i);
				System.out.println("Statistics of flow "+currentWorker.getFlow().getPortId());
				System.out.println("Flow allocation time (ms): "+currentWorker.getStatistics().getFlowSetupTimeInMillis());
				System.out.println("Flow deallocation time (ms): "+currentWorker.getStatistics().getFlowTearDownTimeInMillis());
				System.out.println("Sent SDUs per second: "+currentWorker.getStatistics().getSentSDUsPerSecond());
				System.out.println("Sent KiloBytes per second (KBps): "+
						currentWorker.getStatistics().getSentSDUsPerSecond()*this.testInformation.getSduSize()/1024);
				System.out.println("Received SDUs per second: "+currentWorker.getStatistics().getReceivedSDUsPerSecond());
				System.out.println("Received KiloBytes per second (KBps): "+
						currentWorker.getStatistics().getReceivedSDUsPerSecond()*this.testInformation.getSduSize()/1024);
				System.out.println();
			}
			
			//2 Print the aggregated statistics
			System.out.println("Mean statistics");
			System.out.println("Mean flow allocation time (ms): "+ this.getMean(Type.FLOW_ALLOCATION));
			System.out.println("Mean flow deallocation time (ms): "+ this.getMean(Type.FLOW_DEALLOCATION));
			long averageClientServerDelay = 0L;
			long averageServerClientDelay = 0L;
			if (this.testInformation.isClientSendsSDUs()){
				long meanSentSdus = this.getMean(Type.SENT_SDUS);
				System.out.println("Mean sent SDUs per second: "+ meanSentSdus);
				System.out.println("Mean sent KiloBytes per second (KBps): "+ meanSentSdus*this.testInformation.getSduSize()/1024);
				averageClientServerDelay = ((statsInformation.getServerTimeFirstSDUReceived()/1000L - this.epochTimeFirstSDUSent) + 
						(statsInformation.getServerTimeLastSDUReceived()/1000L - this.epochTimeLastSDUSent))/2;
			}
			if (this.testInformation.isServerSendsSDUs()){
				long meanReceivedSdus = this.getMean(Type.RECEIVED_SDUS);
				System.out.println("Mean received SDUs per second: "+ meanReceivedSdus);
				System.out.println("Mean received KiloBytes per second (KBps): "+ meanReceivedSdus*this.testInformation.getSduSize()/1024);
				averageServerClientDelay = ((this.epochTimeFirstSDUReceived - statsInformation.getServerTimeFirstSDUSent()/1000L) + 
						(this.epochTimeLastSDUReceived - statsInformation.getServerTimeLastSDUSent()/1000L))/2;
			}
			System.out.println();
			System.out.println("Aggregate bandwidth:");
			if (this.testInformation.isClientSendsSDUs()){
				long aggregateSentSDUsPerSecond = 1000L*1000L*this.testInformation.getNumberOfFlows()*
					this.testInformation.getNumberOfSDUs()/(statsInformation.getServerTimeLastSDUReceived()-statsInformation.getServerTimeFirstSDUReceived());
				System.out.println("Aggregate sent SDUs per second: "+aggregateSentSDUsPerSecond);
				System.out.println("Aggregate sent KiloBytes per second (KBps): "+ aggregateSentSDUsPerSecond*this.testInformation.getSduSize()/1024);
				System.out.println("Aggregate sent Megabits per second (Mbps): "+ aggregateSentSDUsPerSecond*this.testInformation.getSduSize()*8/(1024*1024));
			}
			if (this.testInformation.isServerSendsSDUs()){
				long aggregateReceivedSDUsPerSecond = 1000L*this.testInformation.getNumberOfFlows()*
					this.testInformation.getNumberOfSDUs()/(this.epochTimeLastSDUReceived-this.epochTimeFirstSDUReceived);
				System.out.println("Aggregate received SDUs per second: "+aggregateReceivedSDUsPerSecond);
				System.out.println("Aggregate received KiloBytes per second (KBps): "+ aggregateReceivedSDUsPerSecond*this.testInformation.getSduSize()/1024);
				System.out.println("Aggregate received Megabits per second (Mbps): "+ aggregateReceivedSDUsPerSecond*this.testInformation.getSduSize()*8/(1024*1024));
			}
			
			long rttInMs = 0L;
			if (this.testInformation.isClientSendsSDUs() && this.testInformation.isServerSendsSDUs()){
				rttInMs = averageClientServerDelay + averageServerClientDelay;
			}else if (this.testInformation.isClientSendsSDUs()){
				rttInMs = averageClientServerDelay*2;
			}else{
				rttInMs = averageServerClientDelay*2;
			}
			System.out.println("Estimated round-trip time (RTT) in ms: "+rttInMs);
			
			//3 Deallocate the control flow
			this.controlFlow.deallocate();
			this.state = State.COMPLETED;
			
			//4 Exit
			System.exit(0);
		}catch(Exception ex){
			ex.printStackTrace();
			abortTest("Problems cleaning up the test resources");
		}
	}
	
	private long getMean(Type type){
		long accumulatedValue = 0;
		for(int i=0; i<this.testWorkers.size(); i++){
			switch(type){
			case FLOW_ALLOCATION: 
				accumulatedValue = accumulatedValue + 
				this.testWorkers.get(i).getStatistics().getFlowSetupTimeInMillis();
				break;
			case FLOW_DEALLOCATION: 
				accumulatedValue = accumulatedValue + 
				this.testWorkers.get(i).getStatistics().getFlowTearDownTimeInMillis();
				break;
			case SENT_SDUS:
				accumulatedValue = accumulatedValue + 
				this.testWorkers.get(i).getStatistics().getSentSDUsPerSecond();
				break;
			case RECEIVED_SDUS:
				accumulatedValue = accumulatedValue + 
				this.testWorkers.get(i).getStatistics().getReceivedSDUsPerSecond();
				break;
			}
			
		}
		
		return accumulatedValue/this.testWorkers.size();
	}
	
	
	private void abortTest(String message){
		if (this.state == State.EXECUTING){
			for(int i=0; i<testWorkers.size(); i++){
				this.testWorkers.get(i).abortTest();
			}
		}
		
		System.out.println(message + ". Aborting the test.");
		System.exit(-1);
	}
	
}
