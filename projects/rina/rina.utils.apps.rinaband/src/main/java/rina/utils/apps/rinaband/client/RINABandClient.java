package rina.utils.apps.rinaband.client;

import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.utils.apps.rinaband.TestInformation;

/**
 * Implements the behavior of a RINABand Client
 * @author eduardgrasa
 *
 */
public class RINABandClient {

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
	
	public RINABandClient(TestInformation testInformation, ApplicationProcessNamingInfo controlApNamingInfo, 
			ApplicationProcessNamingInfo dataApNamingInfo){
		this.testInformation = testInformation;
		this.controlApNamingInfo = controlApNamingInfo;
		this.dataApNamingInfo = dataApNamingInfo;
	}
	
	public void execute(){
		
	}
	
}
