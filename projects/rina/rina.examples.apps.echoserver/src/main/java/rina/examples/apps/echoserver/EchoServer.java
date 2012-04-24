package rina.examples.apps.echoserver;

import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applibrary.api.ApplicationRegistration;
import rina.applibrary.api.Flow;
import rina.applibrary.api.FlowListener;
import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.IPCException;

/**
 * 
 * @author eduardgrasa
 *
 */
public class EchoServer implements FlowListener{
	
	public static final String APPLICATION_PROCESS_NAME = "rina.examples.apps.echoServer";
	
	private static final Log log = LogFactory.getLog(EchoServer.class);
	
	private Map<Integer, EchoWorker> workers = null;
	private ApplicationRegistration registration = null;
	
	public EchoServer(){
		workers = new Hashtable<Integer, EchoWorker>();
	}
	
	/**
	 * Register the server to the local RINA software
	 * @throws IPCException
	 */
	public void start() throws IPCException{
		ApplicationProcessNamingInfo applicationProcess = new ApplicationProcessNamingInfo();
		applicationProcess.setApplicationProcessName(APPLICATION_PROCESS_NAME);
		registration = new ApplicationRegistration(applicationProcess, this);
		log.info("Echo Server registered! Now waiting for incoming flows");
	}
	
	/**
	 * Unregisters the server
	 * @throws IPCException
	 */
	public void stop() throws IPCException{
		registration.unregister();
		log.info("Echo Server unregistered!");
	}
	
	/**
	 * Positive if the server is connected
	 * @return
	 */
	public boolean isConnected(){
		return registration.isRegistered();
	}

	/**
	 * Called every time a new flow is allocated
	 */
	public void flowAllocated(Flow flow) {
		EchoWorker echoWorker = new EchoWorker(flow, this);
		flow.setSDUListener(echoWorker);
		workers.put(new Integer(flow.getPortId()), echoWorker);
		log.info("New flow allocated, with portId " + flow.getPortId());
	}

	/**
	 * Called every time a new flow is deallocated
	 */
	public void flowDeallocated(Flow flow) {
		workers.remove(new Integer(flow.getPortId()));
		log.info("Flow deallocated, with portId " + flow.getPortId());
	}
}
