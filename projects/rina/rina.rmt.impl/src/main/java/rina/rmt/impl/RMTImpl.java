package rina.rmt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.efcp.api.BaseDataTransferAE;
import rina.efcp.api.DataTransferAE;
import rina.events.api.Event;
import rina.events.api.EventListener;
import rina.events.api.events.EFCPConnectionCreatedEvent;
import rina.events.api.events.NMinusOneFlowAllocatedEvent;
import rina.ipcmanager.api.IPCManager;
import rina.ipcprocess.api.IPCProcess;
import rina.resourceallocator.api.BaseResourceAllocator;
import rina.resourceallocator.api.NMinus1FlowDescriptor;
import rina.resourceallocator.api.ResourceAllocator;
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
	 * The class that will execute in a separate thread to read the 
	 * outgoing EFCP PDUs
	 */
	private EFCPOutgoingPDUListener efcpOutgoingPDUListener = null;
	
	/**
	 * The IPC Manager
	 */
	private IPCManager ipcManager = null;
	
	/**
	 * The RIB Daemon
	 */
	private RIBDaemon ribDaemon = null;
	
	/**
	 * The Data Transfer AE
	 */
	private DataTransferAE dataTransferAE = null;
	
	public RMTImpl(){
	}
	
	@Override
	public void setIPCProcess(IPCProcess ipcProcess){
		super.setIPCProcess(ipcProcess);
		this.ipcManager = ipcProcess.getIPCManager();
		
		//Subscribe to N-1 Flow deallocated events
		this.ribDaemon = (RIBDaemon) this.getIPCProcess().getIPCProcessComponent(BaseRIBDaemon.getComponentName());
		ribDaemon.subscribeToEvent(Event.N_MINUS_1_FLOW_ALLOCATED, this);
		ribDaemon.subscribeToEvent(Event.EFCP_CONNECTION_CREATED, this);
		
		ResourceAllocator resourceAllocator = (ResourceAllocator) this.getIPCProcess().getIPCProcessComponent(BaseResourceAllocator.getComponentName());
		this.dataTransferAE = (DataTransferAE) this.getIPCProcess().getIPCProcessComponent(BaseDataTransferAE.getComponentName());
		
		//Initialize and execute the N-1 Incoming SDU Listener
		this.nMinusOneIncomingSDUListener = new NMinusOneIncomingSDUListener(ipcManager, ribDaemon, 
				resourceAllocator.getPDUForwardingTable(), ipcProcess, dataTransferAE, 
				resourceAllocator.getNMinus1FlowManager());
		this.ipcManager.execute(this.nMinusOneIncomingSDUListener);
		
		//Initialize and execute the EFCP Outgoing PDU Listener
		this.efcpOutgoingPDUListener = new EFCPOutgoingPDUListener(ipcManager, dataTransferAE, 
				resourceAllocator.getPDUForwardingTable(), resourceAllocator.getNMinus1FlowManager());
		this.ipcManager.execute(this.efcpOutgoingPDUListener);
	}
	
	/**
	 * Close all the sockets and stop
	 */
	@Override
	public void stop(){
		this.nMinusOneIncomingSDUListener.stop();
		this.efcpOutgoingPDUListener.stop();
	}
	
	/**
	 * Called when a new event has happened
	 * @param event
	 */
	public void eventHappened(Event event) {
		if (event.getId().equals(Event.N_MINUS_1_FLOW_ALLOCATED)){
			NMinusOneFlowAllocatedEvent flowEvent = (NMinusOneFlowAllocatedEvent) event;
			this.processNMinus1FlowAllocatedEvent(flowEvent.getNMinusOneFlowDescriptor());
		}else if (event.getId().equals(Event.EFCP_CONNECTION_CREATED)){
			EFCPConnectionCreatedEvent efcpEvent = (EFCPConnectionCreatedEvent) event;
			try{
				this.dataTransferAE.getOutgoingConnectionQueue(efcpEvent.getConnectionEndpointId()).subscribeToQueueReadyToBeReadEvents(this.efcpOutgoingPDUListener);
			}catch(Exception ex){
				log.error("Problems subscribing to outgoing EFCP queue.", ex);
			}
		}
	}
	
	private void processNMinus1FlowAllocatedEvent(NMinus1FlowDescriptor nMinus1FlowDescriptor){
		try{
			this.ipcManager.getIncomingFlowQueue(nMinus1FlowDescriptor.getPortId()).subscribeToQueueReadyToBeReadEvents(this.nMinusOneIncomingSDUListener);
		}catch(Exception ex){
			log.error("Problems subscribing to N-1 incoming flow queue.", ex);
		}
	}
}
