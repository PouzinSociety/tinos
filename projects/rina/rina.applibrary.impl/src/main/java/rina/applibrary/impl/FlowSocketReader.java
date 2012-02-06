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
import rina.delimiting.api.BaseSocketReader;
import rina.delimiting.api.Delimiter;

/**
 * Reads the socket associated to a flow implementation
 * @author eduardgrasa
 *
 */
public class FlowSocketReader extends BaseSocketReader{

	private static final Log log = LogFactory.getLog(FlowSocketReader.class);
	
	private CDAPSessionManager cdapSessionManager = null;
	private BlockingQueue<CDAPMessage> blockingQueue = null;
	private SDUListener sduListener = null;
	private FlowImpl flowImpl = null;
	
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
		CDAPMessage cdapMessage = null;

		try{
			cdapMessage = cdapSessionManager.decodeCDAPMessage(pdu);
			log.debug(cdapMessage.toString());

			switch(cdapMessage.getOpCode()){
			case M_CREATE_R:
				try{
					blockingQueue.put(cdapMessage);
				}catch(InterruptedException ex){
					log.error(ex);
				}
				break;
			case M_DELETE:
				flowImpl.deallocateReceived(cdapMessage);
				break;
			case M_DELETE_R:
				try{
					blockingQueue.put(cdapMessage);
				}catch(InterruptedException ex){
					log.error(ex);
				}
				break;
			case M_WRITE:
				if (cdapMessage.getObjValue() != null){
					sduListener.sduDelivered(cdapMessage.getObjValue().getByteval());
				}else{
					log.error("Received CDAP M_WRITE message with null objectvalue.");
				}
				break;
			default:
				log.error("Received wrong CDAP message.");
			}
		}catch(CDAPException ex){
			ex.printStackTrace();
			log.error("Could not parse received CDAP message.");
		}
	}

	@Override
	public void socketDisconnected() {
		log.debug("Socket closed");
		flowImpl.socketClosed();
	}

}
