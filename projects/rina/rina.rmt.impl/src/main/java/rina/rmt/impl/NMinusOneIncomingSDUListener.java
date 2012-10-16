package rina.rmt.impl;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.aux.QueueSubscriptor;
import rina.efcp.api.DataTransferAE;
import rina.efcp.api.PDU;
import rina.efcp.api.PDUParser;
import rina.enrollment.api.Neighbor;
import rina.ipcmanager.api.IPCManager;
import rina.ipcprocess.api.IPCProcess;
import rina.ipcservice.api.IPCException;
import rina.resourceallocator.api.NMinus1FlowManager;
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
	 * The data transfer AE
	 */
	private DataTransferAE dataTransferAE = null;
	
	/**
	 * The N-1 Flow Manager
	 */
	private NMinus1FlowManager nMinus1FlowManager = null;
	
	/**
	 * PDUPaser
	 */
	private PDUParser pduParser = null;
	
	/**
	 * The IPC Process Address
	 */
	private long myAddress = -1;
	
	public NMinusOneIncomingSDUListener(IPCManager ipcManager, RIBDaemon ribDaemon, 
			PDUForwardingTable pduForwardingTable, IPCProcess ipcProcess, 
			DataTransferAE dataTransferAE, NMinus1FlowManager nMinus1FlowManager){
		this.ipcManager = ipcManager;
		this.ribDaemon = ribDaemon;
		this.queuesReadyToBeRead = new LinkedBlockingQueue<Integer>();
		this.pduForwardingTable = pduForwardingTable;
		this.ipcProcess = ipcProcess;
		this.dataTransferAE = dataTransferAE;
		this.pduParser = dataTransferAE.getPDUParser();
		this.nMinus1FlowManager = nMinus1FlowManager;
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
		//1 Remove protection if any
		PDU pdu = null;
		try{
			pdu = this.nMinus1FlowManager.getNMinus1FlowDescriptor(portId).getSduProtectionModule().unprotectPDU(sdu);
		}catch(IPCException ex){
			log.error("Problems unprotecting SDU, dropping it. "+ex.getMessage());
			return;
		}
		
		//2 Parse PDU 
		this.pduParser.parsePCIForRMT(pdu);
		
		//3 Check if the PDU is a management PDU, if it is, send to RIB Daemon
		if(pdu.getPduType() == PDU.MANAGEMENT_PDU_TYPE){
			this.pduParser.parsePCIForEFCP(pdu);
			this.ribDaemon.managementSDUDelivered(pdu.getUserData(), portId);
			return;
		}
		
		//4 Check if the PDU is a "identify sender" PDU, if it is, process it and either update
		//forwarding table or tear down N-1 flow
		if (pdu.getPduType() == PDU.IDENTIFY_SENDER_PDU_TYPE){
			//See if we have a management flow with the source address
			List<Neighbor> neighbors = this.ipcProcess.getNeighbors();
			for(int i=0; i<neighbors.size(); i++){
				if(neighbors.get(i).getAddress() == pdu.getSourceAddress()){
					//Add entry to PDU forwarding table, so that we can start using the flow
					try{
						int qosId = this.nMinus1FlowManager.getNMinus1FlowDescriptor(portId).getFlowService().getQoSSpecification().getQosCubeId();
						this.pduForwardingTable.addEntry(pdu.getSourceAddress(), qosId, new int[]{portId});
					}catch(Exception ex){
						ex.printStackTrace();
						log.error("Error while looking for the properties of N-1 Flow identified by portId "+portId, ex);
					}
					return;
				}
			}
			
			//The N-1 flow is not coming from a neighbor, deallocate the N-1 flow
			try {
				this.nMinus1FlowManager.deallocateNMinus1Flow(portId);
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Errow while deallocating N-1 flow.", e);
			}
		}
		
		//5 Check the address to see if this IPC Process is the destination of this PDU
		if (pdu.getDestinationAddress() == getMyAddress()){
			try{
				this.pduParser.parsePCIForEFCP(pdu);
				this.dataTransferAE.getIncomingConnectionQueue(pdu.getConnectionId().getDestinationCEPId()).
					writeDataToQueue(pdu);
			}catch(Exception ex){
				log.warn("Problems writing PDU to incoming EFCP queue identified by CEP id " 
						+ pdu.getConnectionId().getDestinationCEPId() +". Dropping the PDU.", ex);
			}
		}else{
			//If it is not, check forwarding table, reapply protection if required
			//and deliver to right N-1 outgoing queue
			int[] outgoingPortIds = this.pduForwardingTable.getNMinusOnePortId(pdu.getDestinationAddress(), 
					pdu.getConnectionId().getQosId());
			if (outgoingPortIds == null){
				log.warn("Dropping the PDU since I could not find an entry in the forwarding table for it."
						+pdu.toString());
				return;
			}
			
			for(int i=0; i<outgoingPortIds.length; i++){
				try {	
					//TODO reapply protection
					//Send through N-1 flow
					this.ipcManager.getOutgoingFlowQueue(outgoingPortIds[i]).writeDataToQueue(sdu);
				} catch (IPCException ex) {
					log.warn("Dropping the PDU since an error happened while writing to the N-1 flow " +
							"identified by portId "+outgoingPortIds[i], ex);
				}
			}

		}
	}

}
