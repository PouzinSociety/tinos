package rina.utils.apps.rinaband;

import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.utils.apps.rinaband.client.RINABandClient;
import rina.utils.apps.rinaband.server.RINABandServer;

public class RINABand{
	
	public static final String DATA = "data";
	public static final String CONTROL = "control";
	
	private RINABandServer rinaBandServer = null;
	private RINABandClient rinaBandClient = null;
	
	public RINABand(TestInformation testInformation, boolean server, String apName, String apInstance){
		ApplicationProcessNamingInfo controlApNamingInfo = new ApplicationProcessNamingInfo(apName, apInstance, CONTROL, null);
		ApplicationProcessNamingInfo dataApNamingInfo = new ApplicationProcessNamingInfo(apName, apInstance, DATA, null);
		if (server){
			rinaBandServer = new RINABandServer(controlApNamingInfo, dataApNamingInfo);
		}else{
			rinaBandClient = new RINABandClient(testInformation, controlApNamingInfo, dataApNamingInfo);
		}
	}
	
	public void execute(){
		if (rinaBandServer != null){
			rinaBandServer.execute();
		}else{
			rinaBandClient.execute();
		}
	}
}
