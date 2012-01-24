package rina.ipcmanager.impl.apservice;

import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.delimiting.api.BaseSocketReader;
import rina.delimiting.api.Delimiter;
import rina.encoding.api.Encoder;
import rina.ipcservice.api.FlowService;
import rina.ipcservice.api.IPCService;

/**
 * Reads a TCP socket that comes from the "RINA Library" side. 6 CDAP messages can be received:
 * ALLOCATE REQUEST (CDAP M_CREATE) = Start an allocation request.
 * ALLOCATE RESPONSE (CDAP M_CREATE_R) = Submit an allocate response.
 * DEALLOCATE REQUEST (CDAP M_DELETE) = Start a deallocation of a flow.
 * DEALLOCATE RESPONSE (CDAP M_DELETE_R) = Finish the deallocation of a flow.
 * REGISTER APP (CDAP M_START) = Make an application able to accept RINA flows.
 * UNREGISTER APP (CADP M_STOP) = Stop making an application able to accept RINA flows (could be made implicit by just closing the socket, but maybe not a good idea)
 * 
 * Appart from this, if there is a flow established the "RINA library" side will issue delimited PDUs, which are to be sent to
 * the remote endpoint of the flow.
 * @author eduardgrasa
 *
 */
public class TCPSocketReader extends BaseSocketReader{
	
	private static final Log log = LogFactory.getLog(TCPSocketReader.class);

	private CDAPSessionManager cdapSessionManager = null;
	
	private Encoder encoder = null;
	
	private APServiceImpl apService = null;
	
	/**
	 * The local IPC process the data has to be sent to
	 */
	private IPCService ipcService = null;
	
	/**
	 * The portId associated to the flow established by application process 
	 * that is sending the PDUs to the IPC Manager
	 */
	private int portId = 0;
	
	/**
	 * Controls if we are in the flow establishment phase or we're already 
	 * sending/receiving PDUs
	 */
	private boolean connected = false;

	public TCPSocketReader(Socket socket, Delimiter delimiter, Encoder encoder, CDAPSessionManager cdapSessionManager, APServiceImpl apService){
		super(socket, delimiter);
		this.cdapSessionManager = cdapSessionManager;
		this.apService = apService;
		this.encoder = encoder;
	}
	
	public void setPortId(int portId){
		this.portId = portId;
	}
	
	/**
	 * If we are on the connection establishment phase the PDU will be a delimited CDAP message,
	 * if not it will be a delimited sdu that we have to send over the flow
	 * @param pdu
	 */
	public void processPDU(byte[] pdu){
		CDAPMessage cdapMessage = null;
		
		try{
			cdapMessage = cdapSessionManager.decodeCDAPMessage(pdu);
			log.debug(cdapMessage.toString());
			
			switch(cdapMessage.getOpCode()){
			case M_CREATE:
				handleMCreateReceived(cdapMessage);
				break;
			case M_DELETE:
				break;
			case M_WRITE:
				break;
			case M_START:
				break;
			case M_STOP:
				break;
			default:
				
			}
			
		}catch(CDAPException ex){
			ex.printStackTrace();
			//TODO, what else to do?, send error message? close socket? both?
		}
		
	}
	
	private void handleMCreateReceived(CDAPMessage cdapMessage){
		if (connected){
			//Error, we cannot receive further allocation requests through this socket.
			//TODO, what to do, ignore?, send error message? close the flow? 2 and 3?
		}
		
		try{
			FlowService flowService = (FlowService) encoder.decode(cdapMessage.getObjValue().getByteval(), FlowService.class.toString());
			apService.processAllocateRequest(flowService, getSocket());
		}catch(Exception ex){
			log.error(ex.getMessage());
			//TODO, what else to do?, send error message? close socket? both?
		}
	}
	
	/**
	 * Invoked when the socket is disconnected
	 */
	public void socketDisconnected(){
		log.debug("Notifying the IPC Manager");
		//TODO notify the IPC Manager Implementation
	}
}
