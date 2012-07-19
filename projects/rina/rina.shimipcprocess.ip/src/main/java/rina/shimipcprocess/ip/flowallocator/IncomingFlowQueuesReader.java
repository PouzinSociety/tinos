package rina.shimipcprocess.ip.flowallocator;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.aux.BlockingQueueSet;
import rina.delimiting.api.Delimiter;

/**
 * Reads the incoming flow queues, gets SDUs from them, 
 * delimits the SDUs and writes it to the right output socket
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
	 * The flow state, including the socket to write to
	 */
	private Map<Integer, FlowState> flows = null;
	
	/**
	 * The Delimiter instance
	 */
	private Delimiter delimiter = null;
	
	private boolean end = false;
	
	public IncomingFlowQueuesReader(BlockingQueueSet incomingFlowQueues, 
			Map<Integer, FlowState> flows, Delimiter delimiter){
		this.incomingFlowQueues = incomingFlowQueues;
		this.flows = flows;
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
				if (portId < 0){
					break;
				}
				sdu = this.incomingFlowQueues.getDataQueue(portId).poll();
				this.processSDU(sdu, flows.get(portId));
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
	private void processSDU(byte[] sdu, FlowState flowState){
		DatagramPacket datagramPacket = null;
		
		try{
			if (flowState.getSocket() != null){
				flowState.getSocket().getOutputStream().write(delimiter.getDelimitedSdu(sdu));
			}else if (flowState.getBlockingQueueReader() != null){
				//Reusing the UDP server socket
				datagramPacket = new DatagramPacket(sdu, sdu.length);
				datagramPacket.setAddress(InetAddress.getByName(
						flowState.getFlowService().getSourceAPNamingInfo().getApplicationProcessName()));
				datagramPacket.setPort(Integer.parseInt(
						flowState.getFlowService().getSourceAPNamingInfo().getApplicationProcessInstance()));
				flowState.getDatagramSocket().send(datagramPacket);
			}else{
				//Dedicated and already connected UDP socket
				datagramPacket = new DatagramPacket(sdu, sdu.length);
				flowState.getDatagramSocket().send(datagramPacket);
			}
		}catch(Exception ex){
			log.error("Error processing SDU. ", ex);
		}
	}

}
