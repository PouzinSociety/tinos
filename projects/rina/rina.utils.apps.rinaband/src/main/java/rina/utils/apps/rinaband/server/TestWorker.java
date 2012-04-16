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
	}

}
