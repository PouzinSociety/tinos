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
import rina.utils.apps.rinaband.TestInformation;
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
		
		//1 State is completed
		this.state = State.COMPLETED;
		
		//2 Cancel the registration of the data AE
		try{
			if (dataRegistration.isRegistered()){
				dataRegistration.unregister();
			}
		}catch(Exception ex){
			printMessage("Problems unregistering data AE");
			ex.printStackTrace();
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
		
		TestWorker testWorker = new TestWorker(this.testInformation, flow);
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
