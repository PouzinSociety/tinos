package rina.efcp.impl;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.aux.QueueSubscriptor;
import rina.efcp.api.DTPPDU;
import rina.efcp.api.DataTransferAE;
import rina.efcp.api.FlowControlOnlyDTCPPDU;
import rina.efcp.api.PDU;
import rina.efcp.impl.events.EFCPEvent;
import rina.efcp.impl.events.IPCProcessStoppedEvent;
import rina.efcp.impl.events.PDUDeliveredFromRMTEvent;
import rina.flowallocator.api.ConnectionId;
import rina.ipcmanager.api.IPCManager;

/**
 * Reads the incoming flow queues, gets SDUs from them, 
 * applies DTP and posts to the right socket.
 * @author eduardgrasa
 *
 */
public class IncomingEFCPQueuesReader implements Runnable, QueueSubscriptor{
	
	private static final Log log = LogFactory.getLog(IncomingEFCPQueuesReader.class);
	
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
	private Map<Long, DTAEIState> connectionStatesByConnectionId = null;
	
	/**
	 * the Data Transfer AE
	 */
	private DataTransferAE dataTransferAE = null;
	
	/**
	 * The outgoing Flow queues reader
	 */
	private OutgoingFlowQueuesReader outgoingFlowQueuesReader = null;
	
	private boolean end = false;
	
	public IncomingEFCPQueuesReader(IPCManager ipcManager, Map<Long, DTAEIState> connectionStatesByConnectionId, 
			DataTransferAE dataTransferAE){
		this.ipcManager = ipcManager;
		this.dataTransferAE = dataTransferAE;
		this.efcpEvents = new LinkedBlockingQueue<EFCPEvent>();
		this.connectionStatesByConnectionId = connectionStatesByConnectionId;
	}
		
	public void setOutgoingFlowQueuesReader(
			OutgoingFlowQueuesReader outgoingFlowQueuesReader) {
		this.outgoingFlowQueuesReader = outgoingFlowQueuesReader;
	}
	
	public void stop(){
		this.end = true;
		try{
			this.efcpEvents.put(new IPCProcessStoppedEvent());
		}catch(Exception ex){
			log.error(ex);
		}
	}
	
	public void queueReadyToBeRead(int queueId) {
		try {
			this.efcpEvents.put(new PDUDeliveredFromRMTEvent(queueId));
		} catch (InterruptedException e) {
			log.error(e);
		}
	}
	
	/**
	 * Process the events
	 */
	public void run() {
		EFCPEvent efcpEvent = null;
		PDUDeliveredFromRMTEvent pduEvent = null;
		
		while(!end){
			try{
				efcpEvent = this.efcpEvents.take();
				switch(efcpEvent.getId()){
				case EFCPEvent.IPC_PROCESS_STOPPED_EVENT:
					log.info("EFCP Incoming Connection Queues Reader stopping");
					return;
				case EFCPEvent.PDU_DELIVERED_FROM_RMT:
					pduEvent = (PDUDeliveredFromRMTEvent) efcpEvent;
					this.processEFCPPDUDeliveredFromRMT(pduEvent.getConnectionEndpointId());
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
	 * Update DTAEI state and send SDU to N-portId
	 * @param sdu
	 * @param flowState
	 */
	private void processEFCPPDUDeliveredFromRMT(long connectionEndpointId) throws Exception{
		PDU pdu = this.dataTransferAE.getIncomingConnectionQueue(connectionEndpointId).take();
		DTAEIState state = connectionStatesByConnectionId.get(new Long(connectionEndpointId));
		
		if (state == null){
			log.error("Received a PDU with an unrecognized Connection ID: "+pdu.getConnectionId());
			return;
		}
		
		if (pdu instanceof DTPPDU){
			this.processDTPPDU((DTPPDU)pdu, state);
		}else if (pdu instanceof FlowControlOnlyDTCPPDU){
			this.processFlowControlOnlyDTCPPDU((FlowControlOnlyDTCPPDU)pdu, state);
		}else{
			log.error("Received a PDU with of an unrecognized type: "+pdu.toString());
		}
	}
	
	/**
	 * Update DTAEI state and send SDU to N-portId
	 * @param DTP PDU
	 * @param flowState
	 */
	private void processDTPPDU(DTPPDU pdu, DTAEIState state){
		byte[] sdu = pdu.getUserData();
		if (sdu.length == 0){
			//TODO do something special?
		}else{
			//Deliver the PDU to the portId
			try{
				this.ipcManager.getIncomingFlowQueue((int)state.getPortId()).writeDataToQueue(sdu);
			}catch(Exception ex){
				log.error("Problems delivering an SDU to N-portId "+state.getPortId()+". Dropping the SDU.",ex);
			}
		}
		
		//Update DTAEI state
		synchronized(state){
			state.incrementLastSequenceDelivered();
		}
		
		//Check if credit has to be extended
		DTCPStateVector dtcpStateVector = state.getDTCPStateVector();
		if (dtcpStateVector.isFlowControlEnabled()){
			if (state.getLastSequenceDelivered() == dtcpStateVector.getReceiveRightWindowEdge()){
				//The sender has no more credit, extend it
				ConnectionId connectionId = new ConnectionId();
				connectionId.setQosId(state.getQoSId());
				FlowControlOnlyDTCPPDU dtcpPDU = this.dataTransferAE.getPDUParser().generateFlowControlOnlyDTCPPDU(
						dtcpStateVector.getFlowControlOnlyPCI(), dtcpStateVector.getNextSequenceToSend(), 
						state.getDestinationAddress(), connectionId, dtcpStateVector.getReceiveRightWindowEdge() + 50);
				try{
					this.dataTransferAE.getOutgoingConnectionQueue(state.getSourceCEPid()).writeDataToQueue(dtcpPDU);
					synchronized(state){
						dtcpStateVector.incrementNextSequenceToSend();
						dtcpStateVector.setReceiveRightWindowEdge(dtcpStateVector.getReceiveRightWindowEdge() + 50);
					}
				}catch(Exception ex){
					log.error("Problems extending credit");
				}
			}
		}
	}
	
	/**
	 * When the PDU is received then: If Creditbased Then SndrCredit := PDU(RtWindEdge) SndLeftWindEdge := UpdateCredit(Ack, RtWindEdge) Fi 
	 * If Ratebased present Then SndrRate := PDU(Rate) TimePeriod ;= TimeUnit Fi 
	 * If WaitQ not Empty Then Send as many PDUs as possible, given the current allocation Fi
	 * @param pdu the Flow Control Only DTCP PDU
	 * @param state
	 */
	private void processFlowControlOnlyDTCPPDU(FlowControlOnlyDTCPPDU pdu, DTAEIState state){
		DTCPStateVector dtcpStateVector = state.getDTCPStateVector();
		if(dtcpStateVector.isFlowControlEnabled()){
			if (dtcpStateVector.getFlowControlType().equals(DTCPStateVector.CREDIT_BASED_FLOW_CONTROL)){
				//log.debug("Extending credit! New right window edge: "+pdu.getRightWindowEdge());
				synchronized(state){
					dtcpStateVector.setSendRightWindowEdge(pdu.getRightWindowEdge());
					this.outgoingFlowQueuesReader.creditExtended((int)state.getPortId());
				}
			}else{
				//TODO deal with RATE-based flow control
			}
		}else{
			log.error("Received a Flow Control Only DTCP PDU but Flow Control is not enabled for flow "+state.getPortId());
		}
	}
}
