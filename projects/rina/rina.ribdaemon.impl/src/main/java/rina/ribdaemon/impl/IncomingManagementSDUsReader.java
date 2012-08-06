package rina.ribdaemon.impl;

import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IncomingManagementSDUsReader implements Runnable{
	
	private static final Log log = LogFactory.getLog(IncomingManagementSDUsReader.class);
	
	private BlockingQueue<IncomingManagementSDU> incomingManagementSDUsQueue = null;
	
	private RIBDaemonImpl ribDaemon = null;
	
	public IncomingManagementSDUsReader(BlockingQueue<IncomingManagementSDU> incomingManagementSDUs, RIBDaemonImpl ribDaemon){
		this.incomingManagementSDUsQueue = incomingManagementSDUs;
		this.ribDaemon = ribDaemon;
	}
	
	public void stop(){
		IncomingManagementSDU sdu = new IncomingManagementSDU();
		sdu.setPortId(-1);
		try {
			this.incomingManagementSDUsQueue.put(sdu);
		} catch (InterruptedException ex) {
			log.error(ex);
		}
	}
	
	public void run() {
		IncomingManagementSDU sdu = null;
		
		while(true){
			try {
				sdu = this.incomingManagementSDUsQueue.take();
				if (sdu.getPortId() < 0){
					break;
				}
				
				this.ribDaemon.cdapMessageDelivered(sdu.getManagementSDU(), sdu.getPortId());
			} catch (Exception ex) {
				log.error("Problems processing incoming management SDU.", ex);
			}
		}
		
	}

}
