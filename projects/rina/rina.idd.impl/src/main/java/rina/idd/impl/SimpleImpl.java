package rina.idd.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.pouzinsociety.bootstrap.api.BootStrapCompleteAPI;
//import org.pouzinsociety.bootstrap.api.BootstrapException;
import rina.idd.api.IDDConfigDao;
import rina.idd.api.IDDConsumer;
import rina.idd.api.IDDProvider;
import rina.idd.api.IDDRecordRequest;

public class SimpleImpl {
//` implements Runnable, BootStrapCompleteAPI, IDDProvider {
	private static final Log log = LogFactory.getLog(SimpleImpl.class);
	boolean readyToProcessIPCRequests = false;
	List<String> forwardingTable = new ArrayList<String>();
	

	/**
	 * Need to fix up the bootstrap process a little more
 	 */
//	public void bootstrapComplete(Object arg0) throws BootstrapException {
//		log.info("IDD BootStrapping..");
//		String jsonConfig = (String)arg0;
//		
//		log.info("IDD BootStrap Complete");
//	}

	public String getConfigDaoClassName() {
		return IDDConfigDao.class.toString();
	}

	public void run() {
		log.info("IDD Initializing..");
		readyToProcessIPCRequests = true;
		
		log.info("IDD Ready for IPC Requests");
	}

	// Incoming from network / other IDDs
	public void process(Object event) throws Exception {
		// decode data object
		// handleObjects
	}
	
	public void handleReadDirectoryRequest(IDDRecordRequest request) {
		// 
		// result = rib.lookup(request.getRequestedApplicationProcessName(), request.getRequestedApplicationProcessInstanceId());
		// if (result == true)
		//     return IDD
	}
	

	// Incoming from IPC processes
	public void Allocate_Request(Object arg0) {
		// TODO Auto-generated method stub
		if (readyToProcessIPCRequests == false) {
			log.info("ipcProcess Allocate_Request - Initialization incomplete");
			return;
		}
		
		// Lookup  application-process-name in the local RIB
		boolean localProcess = false;
	//	localProcess = rib.lookup(apcName);
		if (localProcess == true) {
			// forward to Allocate_Request to the DIF/IPC Process Name
		} else {
			// use search rules
			for(String tableEntry : forwardingTable) {
				// if (matchCriteria(ipcProcessName)) {
				//   forwardAllocateRequest(arg0);
				// }
			}
		}
	}

	public void addEventListener(IDDConsumer arg0) {
		// TODO Auto-generated method stub
	}

	public void removeEventListener(IDDConsumer arg0) {
		// TODO Auto-generated method stub	
	}
}
