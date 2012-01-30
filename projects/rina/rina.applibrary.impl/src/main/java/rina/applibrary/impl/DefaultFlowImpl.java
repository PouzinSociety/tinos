package rina.applibrary.impl;

import rina.applibrary.api.ApplicationProcess;
import rina.applibrary.api.FlowImpl;
import rina.applibrary.api.IPCException;
import rina.applibrary.api.QualityOfServiceSpecification;
import rina.applibrary.api.SDUListener;

/**
 * The default implementation for the FlowImpl interface.
 * @author eduardgrasa
 *
 */
public class DefaultFlowImpl implements FlowImpl{

	public void allocate(ApplicationProcess sourceApplication,
			ApplicationProcess destinationApplication,
			QualityOfServiceSpecification qosSpec, SDUListener sduListener)
			throws IPCException {
		// TODO Auto-generated method stub
		
	}

	public void write(byte[] sdu) throws IPCException {
		// TODO Auto-generated method stub
		
	}

	public void deallocate() throws IPCException {
		// TODO Auto-generated method stub
		
	}

}
