package rina.utils.apps.rinaband.server;

import java.util.Random;

import rina.applibrary.api.Flow;
import rina.utils.apps.rinaband.TestInformation;

/**
 * Sends a number of SDUs through a flow
 * @author eduardgrasa
 *
 */
public class SDUSender implements Runnable {
	
	/**
	 * The information of this test
	 */
	private TestInformation testInformation = null;
	
	/**
	 * The flow from the RINABand client
	 */
	private Flow flow = null;
	
	/**
	 * The number of SDUs generated
	 */
	private int generatedSDUs = 0;
	
	public SDUSender(TestInformation testInformation, Flow flow){
		this.testInformation = testInformation;
		this.flow = flow;
	}

	public void run() {
		byte[] sdu = new byte[testInformation.getSduSize()];
		Random random = new Random();
		
		int numberOfSdus = testInformation.getNumberOfSDUs();
		for(generatedSDUs=0; generatedSDUs<numberOfSdus; generatedSDUs++){
			random.nextBytes(sdu);
			try{
				flow.write(sdu);
			}catch(Exception ex){
				System.out.println("SDU Sender of flow "+flow.getPortId()+": Error writing SDU. Canceling operation");
				ex.printStackTrace();
			}finally{
				try{
					if (flow.isAllocated()){
						flow.deallocate();
					}
				}catch(Exception ex){
				}
			}
		}
	}

}
