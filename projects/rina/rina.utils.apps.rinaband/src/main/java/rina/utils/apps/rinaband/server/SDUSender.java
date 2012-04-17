package rina.utils.apps.rinaband.server;

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
		
		int numberOfSdus = testInformation.getNumberOfSDUs();
		for(generatedSDUs=0; generatedSDUs<numberOfSdus; generatedSDUs++){
			try{
				flow.write(getNextSDU());
			}catch(Exception ex){
				System.out.println("SDU Sender of flow "+flow.getPortId()+": Error writing SDU. Canceling operation");
				ex.printStackTrace();
				try{
					if (flow.isAllocated()){
						flow.deallocate();
					}
				}catch(Exception e){
				}
				break;
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

}
