package rina.applibrary.impl;

import rina.applibrary.api.ApplicationProcess;
import rina.applibrary.api.FlowAcceptor;

/**
 * Accepts all the flows that are for this application process
 * (identified by applicationprocess)
 * @author eduardgrasa
 *
 */
public class DefaultFlowAcceptor implements FlowAcceptor{
	
	private ApplicationProcess myself = null;
	
	public DefaultFlowAcceptor(ApplicationProcess applicationProcess){
		this.myself = applicationProcess;
	}

	public String acceptFlow(ApplicationProcess sourceApplication, ApplicationProcess destinationApplication){
		if (myself.equals(destinationApplication)){
			return null;
		}else{
			return "This flow is not for me " + myself.toString() + "; but for application " + destinationApplication.toString();
		}
	}

}
