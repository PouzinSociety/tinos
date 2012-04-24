package rina.utils.apps.rinaband.client;

import rina.applibrary.api.Flow;
import rina.applibrary.api.SDUListener;
import rina.utils.apps.rinaband.TestInformation;

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
	private long timeOfFirstSDUReceived = 0;
	
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
	
	public TestWorker(TestInformation testInformation, RINABandClient rinaBandClient){
		this.testInformation = testInformation;
		this.rinaBandClient = rinaBandClient;
		this.statistics = new TestFlowStatistics();
		this.lock = new Object();
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
		
		this.timeOfFirstSDUSent = System.nanoTime();
		for(int i=0; i<this.testInformation.getNumberOfSDUs(); i++){
			try{
				flow.write(getNextSDU());
			}catch(Exception ex){
				synchronized(lock){
					this.sendCompleted = true;
					if (this.receiveCompleted){
						rinaBandClient.testCompleted(this);
					}
				}
				return;
			}
		}
		
		synchronized(lock){
			long totalTimeInNanos = (System.nanoTime() - this.timeOfFirstSDUSent);
			this.statistics.setSentSDUsPerSecond(1000L*1000L*1000L*this.testInformation.getNumberOfSDUs()/totalTimeInNanos);
			this.sendCompleted = true;
			if (this.receiveCompleted){
				rinaBandClient.testCompleted(this);
			}
		}
		
	}
	
	private byte[] getNextSDU(){
		byte[] result = new byte[this.testInformation.getSduSize()];
		for(int i=0; i<result.length; i++){
			result[i] = 0x01;
		}
		
		return result;
	}

	/**
	 * Called when an sdu is received through the flow
	 */
	public void sduDelivered(byte[] sdu) {
		if (this.receivedSDUs == 0){
			this.timeOfFirstSDUReceived = System.nanoTime();
		}
		
		this.receivedSDUs++;
		if (this.receivedSDUs == this.testInformation.getNumberOfSDUs()){
			long totalTimeInNanos = (System.nanoTime() - this.timeOfFirstSDUReceived);
			synchronized(lock){
				this.statistics.setReceivedSDUsPerSecond(1000L*1000L*1000L*this.receivedSDUs/totalTimeInNanos);
				this.receiveCompleted = true;
				if (this.sendCompleted){
					rinaBandClient.testCompleted(this);
				}
			}
		}
	}
}
