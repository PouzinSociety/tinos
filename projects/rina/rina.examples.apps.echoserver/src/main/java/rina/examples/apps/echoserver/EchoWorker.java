package rina.examples.apps.echoserver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applibrary.api.Flow;
import rina.applibrary.api.SDUListener;
import rina.ipcservice.api.IPCException;

/**
 * Every time a new SDU is delivered, it logs it and echoes it back
 * @author eduardgrasa
 */
public class EchoWorker implements SDUListener{

	private static final Log log = LogFactory.getLog(EchoWorker.class);
	
	private Flow flow = null;
	private EchoServer echoServer = null;
	private String message = null;
	
	public EchoWorker(Flow flow, EchoServer echoServer){
		this.flow = flow;
		this.flow.setSDUListener(this);
		this.echoServer = echoServer;
	}

	public void sduDelivered(byte[] sdu) {
		try{
			message = new String(sdu);
			log.info("Received message from flow "+flow.getPortId()+" : "+message+". Echoing it back!");
			flow.write(sdu);
		}catch(IPCException ex){
			ex.printStackTrace();
			if (!flow.isAllocated()){
				echoServer.flowDeallocated(flow);
			}
		}
		
	}
}
