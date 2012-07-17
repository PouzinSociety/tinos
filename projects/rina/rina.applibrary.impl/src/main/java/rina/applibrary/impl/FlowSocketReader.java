package rina.applibrary.impl;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.applibrary.api.FlowImpl;
import rina.applibrary.api.SDUListener;
import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.Opcode;
import rina.delimiting.api.BaseSocketReader;
import rina.delimiting.api.Delimiter;

/**
 * Reads the socket associated to a flow implementation
 * @author eduardgrasa
 *
 */
public class FlowSocketReader extends BaseSocketReader{

	private static final Log log = LogFactory.getLog(FlowSocketReader.class);
	public enum State {WAITING_ALLOCATION_CONFIRMATION, ALLOCATED};
	
	private CDAPSessionManager cdapSessionManager = null;
	private BlockingQueue<CDAPMessage> blockingQueue = null;
	private SDUListener sduListener = null;
	private FlowImpl flowImpl = null;
	private State state = State.WAITING_ALLOCATION_CONFIRMATION;
	
	public FlowSocketReader(Socket socket, Delimiter delimiter, CDAPSessionManager cdapSessionManager, BlockingQueue<CDAPMessage> blockingQueue, 
			FlowImpl flowImpl){
		super(socket, delimiter);
		this.cdapSessionManager = cdapSessionManager;
		this.blockingQueue = blockingQueue;
		this.flowImpl = flowImpl;
	}

	public SDUListener getSDUListener(){
		return this.sduListener;
	}
	
	public void setSDUListener(SDUListener sduListener){
		this.sduListener = sduListener;
	}
	
	@Override
	public void processPDU(byte[] pdu) {
		
		switch(this.state){
		case WAITING_ALLOCATION_CONFIRMATION:
			CDAPMessage cdapMessage = null;
			try{
				cdapMessage = cdapSessionManager.decodeCDAPMessage(pdu);
				log.debug(cdapMessage.toString());
				if (cdapMessage.getOpCode() == Opcode.M_CREATE_R){
					try{
						blockingQueue.put(cdapMessage);
						setAllocated();
					}catch(InterruptedException ex){
						log.error(ex);
					}
				}else{
					log.error("Received CDAP Message with wrong opcode, expected M_CREATE_R, got: "
							+cdapMessage.toString());
				}
			}catch(CDAPException ex){
				ex.printStackTrace();
				log.error("Could not parse received CDAP message.");
			}
			break;
		case ALLOCATED:
			sduListener.sduDelivered(pdu);
			break;
		}
	}
	
	public void setAllocated(){
		this.state = State.ALLOCATED;
	}

	@Override
	public void socketDisconnected() {
		log.debug("Socket closed");
		flowImpl.socketClosed();
	}

}
