package rina.efcp.impl;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.aux.BlockingQueueSet;
import rina.delimiting.api.Delimiter;

/**
 * Reads the incoming flow queues, gets SDUs from them, 
 * applies DTP and posts to the right socket.
 * @author eduardgrasa
 *
 */
public class IncomingFlowQueuesReader implements Runnable{
	
	private static final Log log = LogFactory.getLog(IncomingFlowQueuesReader.class);
	
	/**
	 * The queues from incoming flows
	 */
	private BlockingQueueSet incomingFlowQueues = null;
	
	/**
	 * The mappings of portId to connection
	 */
	private Map<Integer, DTAEIState> portIdToConnectionMapping = null;
	
	/**
	 * The Delimiter instance
	 */
	private Delimiter delimiter = null;
	
	private boolean end = false;
	
	public IncomingFlowQueuesReader(BlockingQueueSet incomingFlowQueues, 
			Map<Integer, DTAEIState> portIdToConnectionMapping, Delimiter delimiter){
		this.incomingFlowQueues = incomingFlowQueues;
		this.portIdToConnectionMapping = portIdToConnectionMapping;
		this.delimiter = delimiter;
	}
		
	public void stop(){
		this.end = true;
		try{
			this.incomingFlowQueues.getDataReadyQueue().put(new Integer(-1));
		}catch(Exception ex){
			log.error(ex);
		}
	}

	/**
	 * Read the data from the queues and process it
	 */
	public void run() {
		Integer portId = null;
		byte[] sdu = null;
		
		while(!end){
			try{
				portId = this.incomingFlowQueues.select();
				if (portId.intValue() < 0){
					break;
				}
				sdu = this.incomingFlowQueues.getDataQueue(portId).poll();
				this.processSDU(sdu, portIdToConnectionMapping.get(portId));
			}catch(Exception ex){
				log.error("Problems reading the identity of the next queue to read. ", ex);
			}
		}
	}
	
	/**
	 * Delimit the sdu if required, and send it through the right socket
	 * @param sdu
	 * @param flowState
	 */
	private void processSDU(byte[] sdu, DTAEIState state){
		//This connection is supporting a local flow
		if (state.isLocal()){
			DTAEIState state2 = this.portIdToConnectionMapping.get(new Integer(state.getRemotePortId()));
			if (state2 == null){
				log.error("Error processing SDU: Could not find state associated to local flow "+state.getRemotePortId());
				return;
			}
			state2.getApplicationCallback().deliverTransfer(state.getRemotePortId(), sdu);
			return;
		}
		
		//Convert the SDU into a PDU and post it to an RMT queue (right now posting it to the socket)
		byte[] pdu = PDUParser.generatePDU(state.getPreComputedPCI(), 
				state.getNextSequenceToSend(), 0x81, 0x00, sdu);
		
		/*log.debug("Encoded PDU: \n" + "Destination @: " + state.getDestinationAddress() + " CEPid: "+state.getSourceCEPid() + 
				" Source @: "+state.getSourceAddress() + " CEPid: "+state.getSourceCEPid() + "\n QoSid: "
				+ state.getQoSId() + " PDU type: 129 Flags: 00 Sequence Number: " +state.getNextSequenceToSend()); 
		log.debug("Sending PDU " + printBytes(pdu)+"\n");*/
		try{
			state.getSocket().getOutputStream().write(this.delimiter.getDelimitedSdu(pdu));
		}catch(IOException ex){
			log.error(ex);
			return;
		}
		
		//Update DTAEI state
		synchronized(state){
			state.incrementNextSequenceToSend();
		}
	}
	
	private String printBytes(byte[] pdu){
		String result = "";
		for(int i=0; i<pdu.length; i++){
			result = result + String.format("%02X ", pdu[i]);
		}
		
		return result;
	}

}
