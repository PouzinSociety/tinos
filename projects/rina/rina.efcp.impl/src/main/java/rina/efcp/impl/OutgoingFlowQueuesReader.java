package rina.efcp.impl;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.aux.QueueSubscriptor;
import rina.delimiting.api.Delimiter;
import rina.ipcmanager.api.IPCManager;
import rina.ipcservice.api.IPCException;

/**
 * Reads the incoming flow queues, gets SDUs from them, 
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
	 * The queues from incoming flows
	 */
	private BlockingQueue<Integer> queuesReadyToBeRead = null;
	
	/**
	 * The mappings of portId to connection
	 */
	private Map<Integer, DTAEIState> portIdToConnectionMapping = null;
	
	/**
	 * The Delimiter instance
	 */
	private Delimiter delimiter = null;
	
	private boolean end = false;
	
	public OutgoingFlowQueuesReader(IPCManager ipcManager, 
			Map<Integer, DTAEIState> portIdToConnectionMapping, Delimiter delimiter){
		this.ipcManager = ipcManager;
		this.queuesReadyToBeRead = new LinkedBlockingQueue<Integer>();
		this.portIdToConnectionMapping = portIdToConnectionMapping;
		this.delimiter = delimiter;
	}
		
	public void stop(){
		this.end = true;
		try{
			this.queuesReadyToBeRead.put(new Integer(-1));
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
				portId = this.queuesReadyToBeRead.take().intValue();
				if (portId.intValue() < 0){
					break;
				}
				sdu = this.ipcManager.getOutgoingFlowQueue(portId).take();
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
			
			try{
				this.ipcManager.getOutgoingFlowQueue(state.getRemotePortId()).writeDataToQueue(sdu);
			}catch(IPCException ex){
				log.error(ex);
			}
			
			return;
		}
		
		//The last SDU has been written
		if (sdu.length == 0){
			try{
				state.getSocket().getOutputStream().write(0);
			}catch(IOException ex){
				log.error(ex);
				return;
			}
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

	public void queueReadyToBeRead(int queueId) {
		try {
			this.queuesReadyToBeRead.put(new Integer(queueId));
		} catch (InterruptedException e) {
			log.error(e);
		}
	}

}
