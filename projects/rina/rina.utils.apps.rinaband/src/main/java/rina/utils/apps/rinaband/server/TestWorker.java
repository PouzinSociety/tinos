package rina.utils.apps.rinaband.server;

import rina.applibrary.api.Flow;
import rina.applibrary.api.SDUListener;
import rina.utils.apps.rinaband.TestInformation;

/**
 * Deals with an individual data flow, keeping the statistics of 
 * the received SDUs.
 * @author eduardgrasa
 *
 */
public class TestWorker implements SDUListener{
	
	/**
	 * The information of this test
	 */
	private TestInformation testInformation = null;
	
	/**
	 * The flow from the RINABand client
	 */
	private Flow flow = null;
	
	/**
	 * True if the test has started
	 */
	private boolean started = false;
	
	/**
	 * The number of SDUs delivered
	 */
	private int deliveredSDUs = 0;
	
	/**
	 * The time of the first SDU Received
	 */
	private long timeOfFirstSDUReceived =0;
	
	public TestWorker(TestInformation testInformation, Flow flow){
		this.testInformation = testInformation;
		this.flow = flow;
	}
	
	/**
	 * If the server has to send SDUs, execute a new thread that does so
	 */
	public void execute(){
		this.started = true;
		
		if (this.testInformation.isServerSendsSDUs()){
			SDUSender sduSender = new SDUSender(this.testInformation, this.flow);
			RINABandServer.executeRunnable(sduSender);
		}
	}
	
	/**
	 * Stop the execution by deallocating the flow
	 */
	public void abort(){
		if (this.started){
			try{
				if (this.flow.isAllocated()){
					this.flow.deallocate();
				}
			}catch(Exception ex){
			}
		}
		
		this.started = false;
	}

	public void sduDelivered(byte[] sdu) {
		deliveredSDUs ++;
		if (timeOfFirstSDUReceived == 0){
			timeOfFirstSDUReceived = System.nanoTime();
		}
		if (deliveredSDUs == this.testInformation.getNumberOfSDUs()){
			long time = System.nanoTime() - timeOfFirstSDUReceived;
			long sentSDUsperSecond = 1000L*1000L*1000L*this.testInformation.getNumberOfSDUs()/time;
			System.out.println("Flow at portId "+flow.getPortId()+": Received SDUs per second: "+sentSDUsperSecond);
			System.out.println("Flow at portId "+flow.getPortId()+": Received KiloBytes per second (KBps): "
					+sentSDUsperSecond*this.testInformation.getSduSize()/1024);
		}
	}

}