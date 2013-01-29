package rina.efcp.impl;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.aux.QueueReadyToBeReadSubscriptor;
import rina.efcp.api.DataTransferAE;
import rina.efcp.api.EFCPPolicyConstants;
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
public class IncomingEFCPQueuesReader implements Runnable, QueueReadyToBeReadSubscriptor{
	
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
	
	public void queueReadyToBeRead(int queueId, boolean inputOutput) {
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
		
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
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
		
		switch(pdu.getPduType()){
		case PDU.DTP_PDU_TYPE:
			this.processDTPPDU(pdu, state);
			break;
		case PDU.FLOW_CONTROL_ONLY_DTCP_PDU:
			this.processFlowControlOnlyDTCPPDU((FlowControlOnlyDTCPPDU)pdu, state);
			break;
		default:
			log.error("Received a PDU with of an unrecognized type: "+pdu.toString());
		}
	}
	
	/**
	 * Update DTAEI state and send SDU to N-portId
	 * @param DTP PDU
	 * @param flowState
	 */
	private void processDTPPDU(PDU pdu, DTAEIState state){
		byte[] sdu = pdu.getUserData();
		if (sdu.length == 0){
			//TODO do something special?
		}else{
			if (state.getDTCPStateVector().isFlowControlEnabled()){
				if (this.flowControlOverrunPolicy(pdu, state)){
					//Discard PDU, since the sender has exceeded the credit or rate that was granted, 
					//and the current policy says it has to be discarded
					log.warn("Discarding PDU since the sender has exceeded the credit or rate that was granted. "+pdu.toString());
					return;
				}
			}
			
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
		
		if (state.getDTCPStateVector().isFlowControlEnabled()){
			updateFlowControlState(state);
		}
	}
	
	/**
	 * Checks if the sender has exceeded the credit or rate that had been granted
	 * @param pdu
	 * @param state
	 * @return
	 */
	private boolean flowControlOverrunPolicy(PDU pdu, DTAEIState state){
		if (state.getDTCPStateVector().getFlowControlType().equals(EFCPPolicyConstants.CREDIT)){
			if (state.getLastSequenceDelivered() > state.getDTCPStateVector().getReceiveRightWindowEdge()){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Causes the flow control state to be updated (if present), and any associated 
	 * actions to be carried out
	 * @param state
	 */
	private void updateFlowControlState(DTAEIState state){
		if (state.getDTCPStateVector().getFlowControlType().equals(EFCPPolicyConstants.CREDIT)){
			if (creditHasToBeExtended(state)){
				long newRightWindowEdge =  this.computeNewRightWindowEdge(state);
				this.sendFlowControlOnlyDTCPPDUAndUpdateState(state, newRightWindowEdge);
			}
		}
	}
	
	/**
	 * Check if the credit has to be extended. This operation will vary depending on the applied policy
	 * @param state
	 * @return
	 */
	private boolean creditHasToBeExtended(DTAEIState state){
		return state.getLastSequenceDelivered() == state.getDTCPStateVector().getReceiveRightWindowEdge();
	}
	
	/**
	 * Returns the new right window edge, increasing the credit. This operation will vary 
	 * depending on the applied policy
	 * @param state
	 * @return
	 */
	private long computeNewRightWindowEdge(DTAEIState state){
		return state.getDTCPStateVector().getReceiveRightWindowEdge() + 50;
	}
	
	/**
	 * Send the FlowControlOnly DTCP PDU with the new RightWindowEdge and update the state of 
	 * this EFCP connection
	 * @param state
	 * @param newRightWindowEdge
	 */
	private void sendFlowControlOnlyDTCPPDUAndUpdateState(DTAEIState state, long newRightWindowEdge){
		ConnectionId connectionId = new ConnectionId();
		connectionId.setQosId(state.getQoSId());
		FlowControlOnlyDTCPPDU dtcpPDU = this.dataTransferAE.getPDUParser().generateFlowControlOnlyDTCPPDU(
				state.getDTCPStateVector().getFlowControlOnlyPCI(), state.getDTCPStateVector().getNextSequenceToSend(), 
				state.getDestinationAddress(), connectionId, newRightWindowEdge, 0, 0);
		try{
			this.dataTransferAE.getOutgoingConnectionQueue(state.getSourceCEPid()).writeDataToQueue(dtcpPDU);
			synchronized(state){
				state.getDTCPStateVector().incrementNextSequenceToSend();
				state.getDTCPStateVector().setReceiveRightWindowEdge(newRightWindowEdge);
			}
		}catch(Exception ex){
			log.error("Problems extending credit");
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
			if (dtcpStateVector.getFlowControlType().equals(EFCPPolicyConstants.CREDIT)){
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
