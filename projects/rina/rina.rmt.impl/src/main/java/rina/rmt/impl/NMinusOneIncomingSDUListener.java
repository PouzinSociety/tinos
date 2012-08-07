package rina.rmt.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.aux.QueueSubscriptor;
import rina.efcp.api.PDU;
import rina.efcp.api.PDUParser;
import rina.ipcmanager.api.IPCManager;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.IPCException;
import rina.resourceallocator.api.PDUForwardingTable;
import rina.ribdaemon.api.RIBDaemon;

/**
 * Runnable that reads the 
 * @author eduardgrasa
 *
 */
public class NMinusOneIncomingSDUListener implements QueueSubscriptor, Runnable{
	
	private static final Log log = LogFactory.getLog(NMinusOneIncomingSDUListener.class);
	
	/**
	 * The IPC Manager
	 */
	private IPCManager ipcManager = null;
	
	/**
	 * The RIB Daemon
	 */
	private RIBDaemon ribDaemon = null;
	
	/**
	 * The queues from N-1 flows with SDUs available to be read
	 */
	private BlockingQueue<Integer> queuesReadyToBeRead = null;
	
	/**
	 * The PDU Forwarding Table
	 */
	private PDUForwardingTable pduForwardingTable = null;
	
	/**
	 * The IPC Process
	 */
	private IPCProcess ipcProcess = null;
	
	/**
	 * The IPC Process Address
	 */
	private long myAddress = -1;
	
	public NMinusOneIncomingSDUListener(IPCManager ipcManager, RIBDaemon ribDaemon, 
			PDUForwardingTable pduForwardingTable, IPCProcess ipcProcess){
		this.ipcManager = ipcManager;
		this.ribDaemon = ribDaemon;
		this.queuesReadyToBeRead = new LinkedBlockingQueue<Integer>();
		this.pduForwardingTable = pduForwardingTable;
		this.ipcProcess = ipcProcess;
		log.debug("N-1 Incoming SDU Listener executing!");
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
		int portId = -1;
		
		while(true){
			try{
				portId = this.queuesReadyToBeRead.take().intValue();
				if (portId == -1){
					//Detect finalization condition
					break;
				}
				
				this.processNMinusOneSDU(this.ipcManager.getIncomingFlowQueue(portId).take(), portId);
			}catch(Exception ex){
				log.error(ex);
			}
		}
	}

	public void queueReadyToBeRead(int queueId){
		try {
			this.queuesReadyToBeRead.put(new Integer(queueId));
		} catch (InterruptedException e) {
			log.error(e);
		}
	}
	
	private long getMyAddress(){
		if (this.myAddress == -1){
			this.myAddress = this.ipcProcess.getAddress().longValue();
		}
		
		return this.myAddress;
	}
	
	/**
	 * Process the N-1 SDU
	 * @param sdu
	 */
	private void processNMinusOneSDU(byte[] sdu, int portId){
		//1 TODO Remove protection if any
		
		//2 Parse PDU (Optimization: only parse what is relevant)
		PDU decodedPDU = PDUParser.parsePDU(sdu);
		
		//3 Check if the PDU is a management PDU, if it is, send to RIB Daemon
		if(decodedPDU.getPduType() == PDUParser.MANAGEMENT_PDU_TYPE){
			this.ribDaemon.managementSDUDelivered(decodedPDU.getUserData().get(0), portId);
			return;
		}
		
		//4 Check the address to see if this IPC Process is the destination of this PDU
		if (decodedPDU.getDestinationAddress() == getMyAddress()){
			//TODO If it is deliver to right EFCP processing queue
		}else{
			//If it is not, check forwarding table, reapply protection if required
			//and deliver to right N-1 outgoing queue
			int outgoingPortId = this.pduForwardingTable.getNMinusOnePortId(decodedPDU);
			if (outgoingPortId == -1){
				log.warn("Dropping the PDU since I could not find an entry in the forwarding table for it."
						+decodedPDU.toString());
				return;
			}
			try {
				//TODO reapply protection
				//Send through N-1 flow
				this.ipcManager.getOutgoingFlowQueue(outgoingPortId).writeDataToQueue(sdu);
			} catch (IPCException ex) {
				log.warn("Dropping the PDU since an error happened while writing to the N-1 flow " +
						"identified by portId "+outgoingPortId, ex);
			}
		}
	}

}
