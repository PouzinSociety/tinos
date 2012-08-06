package rina.rmt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.events.api.Event;
import rina.events.api.EventListener;
import rina.events.api.events.NMinusOneFlowAllocatedEvent;
import rina.ipcmanager.api.IPCManager;
import rina.ipcprocess.api.IPCProcess;
import rina.ribdaemon.api.BaseRIBDaemon;
import rina.ribdaemon.api.RIBDaemon;
import rina.rmt.api.BaseRMT;

/**
 * Specifies the interface of the Relaying and Multiplexing task. Mediates the access to one or more (N-1) DIFs 
 * or physical media
 * @author eduardgrasa
 */
public class RMTImpl extends BaseRMT implements EventListener{
	private static final Log log = LogFactory.getLog(RMTImpl.class);
	
	/**
	 * The class that will execute in a separate thread to read the 
	 * N-1 incoming SDUs
	 */
	private NMinusOneIncomingSDUListener nMinusOneIncomingSDUListener = null;
	
	/**
	 * The IPC Manager
	 */
	private IPCManager ipcManager = null;
	
	/**
	 * The RIB Daemon
	 */
	private RIBDaemon ribDaemon = null;
	
	public RMTImpl(){
	}
	
	@Override
	public void setIPCProcess(IPCProcess ipcProcess){
		super.setIPCProcess(ipcProcess);
		this.ipcManager = ipcProcess.getIPCManager();
		
		//Subscribe to N-1 Flow deallocated events
		this.ribDaemon = (RIBDaemon) this.getIPCProcess().getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		ribDaemon.subscribeToEvent(Event.N_MINUS_1_FLOW_ALLOCATED, this);
		
		//Initialize and execute the N-1 Outgoing SDU Listener
		this.nMinusOneIncomingSDUListener = new NMinusOneIncomingSDUListener(ipcManager, ribDaemon);
		this.ipcManager.execute(this.nMinusOneIncomingSDUListener);
	}
	
	/**
	 * Close all the sockets and stop
	 */
	@Override
	public void stop(){
		this.nMinusOneIncomingSDUListener.stop();
	}

	/**
	 * When the RMT receives an EFCP PDU via a send primitive, it inspects the destination 
	 * address field and the connection-id field of the PDU. Using the FIB, it determines 
	 * which queue, the PDU should be placed on
	 * @param pdu
	 */
	public void sendEFCPPDU(byte[] pdu) {
		//It will never be called by this implementation since DTP is not implemented yet and 
		//each flow allocation triggers a new TCP connection
	}
	
	/**
	 * Called when a new event has happened
	 * @param event
	 */
	public void eventHappened(Event event) {
		if (event.getId().equals(Event.N_MINUS_1_FLOW_ALLOCATED)){
			NMinusOneFlowAllocatedEvent flowEvent = (NMinusOneFlowAllocatedEvent) event;
			try{
				this.ipcManager.getIncomingFlowQueue(flowEvent.getPortId()).subscribeToQueue(this.nMinusOneIncomingSDUListener);
			}catch(Exception ex){
				log.error("Problems subscribing to N-1 incoming flow queue.", ex);
			}
		}
	}
}
