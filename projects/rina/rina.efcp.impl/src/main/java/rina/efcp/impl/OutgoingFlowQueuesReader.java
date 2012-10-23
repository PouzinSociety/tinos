package rina.efcp.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.aux.QueueSubscriptor;
import rina.efcp.api.DTPPDU;
import rina.efcp.api.DataTransferAE;
import rina.efcp.api.PDU;
import rina.efcp.api.PDUParser;
import rina.efcp.impl.events.CreditExtendedEvent;
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
	 * An SDU has been delivered to the N port
	 * @param portId
	 */
	public void queueReadyToBeRead(int portId) {
		try {
			this.efcpEvents.put(new SDUDeliveredFromNPortEvent(portId));
		} catch (InterruptedException e) {
			log.error(e);
		}
	}
	
	/**
	 * The credit of the connection associated to the flow 
	 * identified by portId has been extended
	 * @param portId
	 */
	public void creditExtended(int portId){
		try{
			this.efcpEvents.put(new CreditExtendedEvent(portId));
		}catch (InterruptedException e) {
			log.error(e);
		}
	}

	/**
	 * Process the events
	 */
	public void run() {
		EFCPEvent efcpEvent = null;
		SDUDeliveredFromNPortEvent sduEvent = null;
		CreditExtendedEvent creditEvent = null;
		
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
				case EFCPEvent.CREDIT_EXTENDED_EVENT:
					creditEvent = (CreditExtendedEvent) efcpEvent;
					this.processCreditExtendedEvent(creditEvent.getPortId());
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
				+ state.getQoSId() + " PDU type: 129 Flags: 00 Sequence Number: " +state.getNextSequenceToSend());*/
		DTCPStateVector dtcpStateVector = state.getDTCPStateVector();
		try{
			if (dtcpStateVector.isFlowControlEnabled()){
				if (pdu.getSequenceNumber() <= dtcpStateVector.getSendRightWindowEdge()){
					//log.debug("Sending PDU. Credit: "+ (state.getDTCPStateVector().getSendRightWindowEdge() - state.getNextSequenceToSend()));
					this.dataTransferAE.getOutgoingConnectionQueue(state.getSourceCEPid()).writeDataToQueue(pdu);
				}else{
					//log.debug("Storing PDU at the closed window queue");
					dtcpStateVector.getClosedWindowQueue().add(pdu);
				}
			}else{
				this.dataTransferAE.getOutgoingConnectionQueue(state.getSourceCEPid()).writeDataToQueue(pdu);
			}
			
			synchronized(state){
				state.incrementNextSequenceToSend();
			}
		}catch(Exception ex){
			log.error("Problems writing PDU to outgoing EFCP queue belonging to CEP id "+state.getSourceCEPid() 
					+ ". Dropping PDU.", ex);
			return;
		}
	}
	
	/**
	 * The credit has been extended, look at the closed window queue and transmit 
	 * all the PDUs that are there waiting, if any
	 * @param portId
	 * @throws Exception
	 */
	private void processCreditExtendedEvent(int portId) throws Exception{
		DTAEIState state = this.portIdToConnectionMapping.get(portId);
		long rightWindowEdge = state.getDTCPStateVector().getSendRightWindowEdge();
		List<PDU> closedWindowQueue = state.getDTCPStateVector().getClosedWindowQueue();
		while(closedWindowQueue.size() > 0){
			if (closedWindowQueue.get(0).getSequenceNumber() <= rightWindowEdge){
				this.dataTransferAE.getOutgoingConnectionQueue(state.getSourceCEPid()).writeDataToQueue(closedWindowQueue.remove(0));
			}else{
				return;
			}
		}
	}

}
