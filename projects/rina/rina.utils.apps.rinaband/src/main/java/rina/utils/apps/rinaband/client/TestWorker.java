package rina.utils.apps.rinaband.client;

import rina.applibrary.api.Flow;
import rina.applibrary.api.SDUListener;
import rina.utils.apps.rinaband.TestInformation;
import rina.utils.apps.rinaband.generator.BoringSDUGenerator;
import rina.utils.apps.rinaband.generator.IncrementSDUGenerator;
import rina.utils.apps.rinaband.generator.SDUGenerator;

public class TestWorker implements Runnable, SDUListener{

	/**
	 * The data about the test to carry out
	 */
	private TestInformation testInformation = null;
	
	/**
	 * The flow
	 */
	private Flow flow = null;
	
	/**
	 * A pointer to the RINABandClient class
	 */
	private RINABandClient rinaBandClient = null;
	
	/**
	 * The statistics of this test of the flow
	 */
	private TestFlowStatistics statistics = null;
	
	/**
	 * The object lock
	 */
	private Object lock = null;
	
	/**
	 * The number of SDUs received
	 */
	private int receivedSDUs = 0;
	
	/**
	 * The time when the first SDU was received
	 */
	private long nanoTimeOfFirstSDUReceived = 0;
	
	/**
	 * The epoch time when the first SDU was received (in milliseconds)
	 */
	private long epochTimeOfFirstSDUReceived = 0;
	
	/**
	 * The time when the first SDU was received
	 */
	private long timeOfFirstSDUSent = 0;
	
	/**
	 * True if we have received all the SDUs
	 */
	private boolean receiveCompleted = false;
	
	/**
	 * True if we have sent all the SDUs
	 */
	private boolean sendCompleted = false;
	
	/**
	 * The class that generates the SDUs
	 */
	private SDUGenerator sduGenerator = null;
	
	public TestWorker(TestInformation testInformation, RINABandClient rinaBandClient){
		this.testInformation = testInformation;
		this.rinaBandClient = rinaBandClient;
		this.statistics = new TestFlowStatistics();
		this.lock = new Object();
		
		if (!this.testInformation.isClientSendsSDUs()){
			this.sendCompleted = true;
		}
		
		if (!this.testInformation.isServerSendsSDUs()){
			this.receiveCompleted = true;
		}
		
		if (this.testInformation.getPattern().equals(SDUGenerator.NONE_PATTERN)){
			sduGenerator = new BoringSDUGenerator(this.testInformation.getSduSize());
		}else if (this.testInformation.getPattern().equals(SDUGenerator.INCREMENT_PATTERN)){
			sduGenerator = new IncrementSDUGenerator(this.testInformation.getSduSize());
		}
	}

	public void setFlow(Flow flow, long flowSetupTimeInMillis){
		this.flow = flow;
		this.statistics.setFlowSetupTimeInMillis(flowSetupTimeInMillis);
	}
	
	public Flow getFlow(){
		return flow;
	}
	
	public TestFlowStatistics getStatistics(){
		return this.statistics;
	}
	
	public void abortTest(){
		if (this.flow.isAllocated()){
			try{
				flow.deallocate();
			}catch(Exception ex){
			}
		}
	}
	
	/**
	 * If this is called it is because this worker needs to send a number of SDUs through the flow.
	 */
	public void run() {
		if (!this.testInformation.isClientSendsSDUs()){
			return;
		}
		
		this.timeOfFirstSDUSent = System.currentTimeMillis();
		rinaBandClient.setFirstSDUSent(this.timeOfFirstSDUSent);
		for(int i=0; i<this.testInformation.getNumberOfSDUs(); i++){
			try{
				flow.write(this.sduGenerator.getNextSDU());
			}catch(Exception ex){
				synchronized(lock){
					this.sendCompleted = true;
					if (this.receiveCompleted){
						this.rinaBandClient.testCompleted(this);
					}
				}
				return;
			}
		}
		
		long currentTime = System.currentTimeMillis();
		long totalTimeInMilis = (currentTime - this.timeOfFirstSDUSent);
		this.rinaBandClient.setLastSDUSent(currentTime);
		synchronized(lock){
			this.statistics.setSentSDUsPerSecond(1000L*this.testInformation.getNumberOfSDUs()/totalTimeInMilis);
			this.sendCompleted = true;
			if (this.receiveCompleted){
				this.rinaBandClient.testCompleted(this);
			}
		}
	}

	/**
	 * Called when an sdu is received through the flow
	 */
	public void sduDelivered(byte[] sdu) {
		if (this.receivedSDUs == 0){
			this.nanoTimeOfFirstSDUReceived = System.nanoTime();
			this.epochTimeOfFirstSDUReceived = System.currentTimeMillis();
			this.rinaBandClient.setFirstSDUReveived(this.epochTimeOfFirstSDUReceived);
		}
		
		this.receivedSDUs++;
		if (this.receivedSDUs == this.testInformation.getNumberOfSDUs()){
			long currentTimeInNanos = System.nanoTime();
			long epochTime = System.currentTimeMillis();
			long totalTimeInNanos = (currentTimeInNanos - this.nanoTimeOfFirstSDUReceived);
			this.rinaBandClient.setLastSDUReceived(epochTime);
			synchronized(lock){
				this.statistics.setReceivedSDUsPerSecond(1000L*1000L*1000L*this.receivedSDUs/totalTimeInNanos);
				this.receiveCompleted = true;
				if (this.sendCompleted){
					this.rinaBandClient.testCompleted(this);
				}
			}
		}
	}
}
