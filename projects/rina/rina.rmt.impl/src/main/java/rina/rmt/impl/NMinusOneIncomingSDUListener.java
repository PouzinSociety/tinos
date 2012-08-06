package rina.rmt.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.aux.QueueSubscriptor;
import rina.ipcmanager.api.IPCManager;
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
	
	public NMinusOneIncomingSDUListener(IPCManager ipcManager, RIBDaemon ribDaemon){
		this.ipcManager = ipcManager;
		this.ribDaemon = ribDaemon;
		this.queuesReadyToBeRead = new LinkedBlockingQueue<Integer>();
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
		byte[] sdu = null;
		
		while(true){
			try{
				portId = this.queuesReadyToBeRead.take().intValue();
				if (portId == -1){
					//Detect finalization condition
					break;
				}
				
				sdu = this.ipcManager.getIncomingFlowQueue(portId).take();
				this.ribDaemon.managementSDUDelivered(sdu, portId);
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

}
