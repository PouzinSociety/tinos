package rina.applibrary.impl;

import rina.applibrary.api.FlowAcceptor;
import rina.ipcservice.api.ApplicationProcessNamingInfo;

/**
 * Accepts all the flows that are for this application process
 * (identified by applicationprocess)
 * @author eduardgrasa
 *
 */
public class DefaultFlowAcceptor implements FlowAcceptor{
	
	private ApplicationProcessNamingInfo myself = null;
	
	public DefaultFlowAcceptor(ApplicationProcessNamingInfo applicationProcess){
		this.myself = applicationProcess;
	}

	public String acceptFlow(ApplicationProcessNamingInfo sourceApplication, ApplicationProcessNamingInfo destinationApplication){
		if (myself.equals(destinationApplication)){
			return null;
		}else{
			return "This flow is not for me " + myself.toString() + "; but for application " + destinationApplication.toString();
		}
	}

}
