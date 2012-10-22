package rina.efcp.impl;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.aux.QueueSubscriptor;
import rina.efcp.api.DTPPDU;
import rina.efcp.api.DataTransferAE;
import rina.efcp.api.PDUParser;
import rina.efcp.impl.events.EFCPEvent;
import rina.efcp.impl.events.IPCProcessStoppedEvent;
import rina.efcp.impl.events.SDUDeliveredFromNPortEvent;
import rina.flowallocator.api.ConnectionId;
import rina.ipcmanager.api.IPCManager;
import rina.ipcservice.api.IPCException;

/**
 * Reads the outgoing flow queues, gets SDUs from them, 
 * applies DTP and posts to the right socket.
 * @author eduardgrasa
 *
 */
public class OutgoingFlowQueuesReader implements Runnable, QueueSubscriptor{
	
	private static final Log log = LogFactory.getLog(OutgoingFlowQueuesReader.class);
	
	/**
	 * The IPC Manager
	 */
	private IPCManager ipcManager = null;
	
	/**
	 * The queue of EFCP Events
	 */
	private BlockingQueue<EFCPEvent> efcpEvents = null;
	
	/**
	 * The mappings of portId to connection
	 */
	private Map<Integer, DTAEIState> portIdToConnectionMapping = null;
	
	/**
	 * the Data Transfer AE
	 */
	private DataTransferAE dataTransferAE = null;
	
	/**
	 * The PDU Parser
	 */
	private PDUParser pduParser = null;
	
	private boolean end = false;
	
	public OutgoingFlowQueuesReader(IPCManager ipcManager, Map<Integer, DTAEIState> portIdToConnectionMapping, 
			DataTransferAE dataTransferAE){
		this.ipcManager = ipcManager;
		this.dataTransferAE = dataTransferAE;
		this.pduParser = dataTransferAE.getPDUParser();
		this.efcpEvents = new LinkedBlockingQueue<EFCPEvent>();
		this.portIdToConnectionMapping = portIdToConnectionMapping;
	}
		
	public void stop(){
		this.end = true;
		try{
			this.efcpEvents.put(new IPCProcessStoppedEvent());
		}catch(Exception ex){
			log.error(ex);
		}
	}

	/**
	 * Process the events
	 */
	public void run() {
		EFCPEvent efcpEvent = null;
		SDUDeliveredFromNPortEvent sduEvent = null;
		
		while(!end){
			try{
				efcpEvent = this.efcpEvents.take();
				switch(efcpEvent.getId()){
				case EFCPEvent.IPC_PROCESS_STOPPED_EVENT:
					log.info("EFCP Outgoing Flow Queues Reader stopping");
					return;
				case EFCPEvent.SDU_DELIVERED_FROM_N_PORT:
					sduEvent = (SDUDeliveredFromNPortEvent) efcpEvent;
					this.processSDUDeliveredFromNPortEvent(sduEvent.getPortId());
					break;
				default:
					log.info("Unknown event delivered to the EFCP: "+efcpEvent.getId());
				}
			}catch(Exception ex){
				log.error("Problems reading the identity of the next queue to read. ", ex);
			}
		}
	}
	
	/**
	 * Process the event
	 * @param sdu
	 * @param flowState
	 */
	private void processSDUDeliveredFromNPortEvent(int portId) throws Exception{
		byte[] sdu = this.ipcManager.getOutgoingFlowQueue(portId).take();
		DTAEIState state = this.portIdToConnectionMapping.get(portId);
		
		//This connection is supporting a local flow
		if (state.isLocal()){
			DTAEIState state2 = this.portIdToConnectionMapping.get(new Integer(state.getRemotePortId()));
			if (state2 == null){
				log.error("Error processing SDU: Could not find state associated to local flow "+state.getRemotePortId());
				return;
			}
			
			try{
				this.ipcManager.getIncomingFlowQueue(state.getRemotePortId()).writeDataToQueue(sdu);
			}catch(IPCException ex){
				log.error(ex);
			}
			
			return;
		}
		
		//Convert the SDU into a PDU and post it to an RMT queue (right now posting it to the socket)
		ConnectionId connectionId = new ConnectionId();
		connectionId.setQosId(state.getQoSId());
		DTPPDU pdu = this.pduParser.generateDTPPDU(state.getPreComputedPCI(), 
				state.getNextSequenceToSend(), state.getDestinationAddress(), 
				connectionId, 0x00, sdu);
		
		/*log.debug("Encoded PDU: \n" + "Destination @: " + state.getDestinationAddress() + " CEPid: "+state.getSourceCEPid() + 
				" Source @: "+state.getSourceAddress() + " CEPid: "+state.getSourceCEPid() + "\n QoSid: "
				+ state.getQoSId() + " PDU type: 129 Flags: 00 Sequence Number: " +state.getNextSequenceToSend()); */
		try{
			this.dataTransferAE.getOutgoingConnectionQueue(state.getSourceCEPid()).writeDataToQueue(pdu);
		}catch(Exception ex){
			log.error("Problems writing PDU to outgoing EFCP queue belonging to CEP id "+state.getSourceCEPid() 
					+ ". Dropping PDU.", ex);
			return;
		}
		
		//Update DTAEI state
		synchronized(state){
			state.incrementNextSequenceToSend();
		}
	}

	public void queueReadyToBeRead(int queueId) {
		try {
			this.efcpEvents.put(new SDUDeliveredFromNPortEvent(queueId));
		} catch (InterruptedException e) {
			log.error(e);
		}
	}

}
