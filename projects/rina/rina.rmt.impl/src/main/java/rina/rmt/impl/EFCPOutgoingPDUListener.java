package rina.rmt.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.aux.QueueReadyToBeReadSubscriptor;
import rina.efcp.api.DataTransferAE;
import rina.efcp.api.PDU;
import rina.ipcmanager.api.IPCManager;
import rina.ipcservice.api.IPCException;
import rina.resourceallocator.api.NMinus1FlowManager;
import rina.resourceallocator.api.PDUForwardingTable;

/**
 * Runnable that reads the 
 * @author eduardgrasa
 *
 */
public class EFCPOutgoingPDUListener implements QueueReadyToBeReadSubscriptor, Runnable{
	
	private static final Log log = LogFactory.getLog(EFCPOutgoingPDUListener.class);
	
	/**
	 * The IPC Manager
	 */
	private IPCManager ipcManager = null;
	
	/**
	 * The RIB Daemon
	 */
	private DataTransferAE dataTransferAE = null;
	
	/**
	 * The N-1 Flow Manager
	 */
	private NMinus1FlowManager nMinus1FlowManager = null;
	
	/**
	 * The queues from N-1 flows with SDUs available to be read
	 */
	private BlockingQueue<Integer> queuesReadyToBeRead = null;
	
	/**
	 * The PDU Forwarding Table
	 */
	private PDUForwardingTable pduForwardingTable = null;
	
	public EFCPOutgoingPDUListener(IPCManager ipcManager, DataTransferAE dataTransferAE, 
			PDUForwardingTable pduForwardingTable, NMinus1FlowManager nMinus1FlowManager){
		this.ipcManager = ipcManager;
		this.dataTransferAE = dataTransferAE;
		this.nMinus1FlowManager = nMinus1FlowManager;
		this.queuesReadyToBeRead = new LinkedBlockingQueue<Integer>();
		this.pduForwardingTable = pduForwardingTable;
		log.debug("EFCP Outgoing PDU Listener executing!");
	}
	
	/**
	 * Causes the IncomingSDUListener to stop executing
	 */
	public void stop(){
		try {
			this.queuesReadyToBeRead.put(new Integer(-1));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		long connectionEndpointId = -1;
		
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
		while(true){
			try{
				connectionEndpointId = this.queuesReadyToBeRead.take().longValue();
				if (connectionEndpointId == -1){
					//Detect finalization condition
					break;
				}
				
				this.processEFCPPDU(this.dataTransferAE.getOutgoingConnectionQueue(connectionEndpointId).take());
			}catch(Exception ex){
				log.error(ex);
			}
		}
	}

	public void queueReadyToBeRead(int queueId, boolean inputOutput){
		try {
			this.queuesReadyToBeRead.put(new Integer(queueId));
		} catch (InterruptedException e) {
			log.error(e);
		}
	}
	
	/**
	 * Process the N-1 SDU
	 * @param sdu
	 */
	private void processEFCPPDU(PDU pdu){
		//1 Check forwarding table to see external portIds
		int[] outgoingPortIds = this.pduForwardingTable.getNMinusOnePortId(pdu.getDestinationAddress(), pdu.getConnectionId().getQosId());
		if (outgoingPortIds == null){
			log.warn("Dropping the PDU since I could not find an entry in the forwarding table for it."
					+pdu.toString());
			return;
		}
		
		byte[] sdu = null;
		for(int i=0; i<outgoingPortIds.length; i++){
			try {	
				//Apply protection
				sdu = this.nMinus1FlowManager.getNMinus1FlowDescriptor(
						outgoingPortIds[i]).getSduProtectionModule().protectPDU(pdu);
				//Send through N-1 flow
				this.ipcManager.getOutgoingFlowQueue(outgoingPortIds[i]).writeDataToQueue(sdu);
			} catch (IPCException ex) {
				log.warn("Dropping the PDU since an error happened while writing to the N-1 flow " +
						"identified by portId "+outgoingPortIds[i], ex);
			}
		}
		
	}

}
