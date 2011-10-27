package rina.cdap.echotarget;

import java.io.IOException;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.ObjectValue;
import rina.delimiting.api.Delimiter;
import rina.encoding.api.Encoder;

/**
 * 
 * @author eduardgrasa
 *
 */
public class CDAPEchoWorker extends CDAPWorker {
	
	private static final Log log = LogFactory.getLog(CDAPEchoWorker.class);
	
	public CDAPEchoWorker(Socket socket, CDAPSessionManager cdapSessionManager, Delimiter delimiter, Encoder encoder) {
		super(socket, cdapSessionManager, delimiter, encoder);
	}
	
	protected void processCDAPMessage(byte[] serializedCDAPMessage){
		log.info("Processing serialized CDAP message. This is the serialized message: ");
		log.info(printBytes(serializedCDAPMessage));
		CDAPMessage incomingCDAPMessage = null;
		CDAPMessage outgoingCDAPMessage = null;
		
		try {
			incomingCDAPMessage = cdapSessionManager.messageReceived(serializedCDAPMessage, socket.getPort());
			log.info("Received CDAP message: "+incomingCDAPMessage.toString());
			//TODO
			switch (incomingCDAPMessage.getOpCode()){
			case M_CONNECT:
				outgoingCDAPMessage = getMConnectResponse(incomingCDAPMessage);
				break;
			case M_CREATE:
				outgoingCDAPMessage = getMCreateResponse(incomingCDAPMessage);
				break;
			case M_DELETE:
				outgoingCDAPMessage = getMDeleteResponse(incomingCDAPMessage);
				break;
			case M_READ:
				outgoingCDAPMessage = getMReadResponse(incomingCDAPMessage);
				break;
			case M_CANCELREAD:
				outgoingCDAPMessage = getMCancelReadResponse(incomingCDAPMessage);
				break;
			case M_WRITE:
				outgoingCDAPMessage = getMWriteResponse(incomingCDAPMessage);
				break;
			case M_START:
				outgoingCDAPMessage = getMStartResponse(incomingCDAPMessage);
				break;
			case M_STOP:
				outgoingCDAPMessage = getMStopResponse(incomingCDAPMessage);
				break;
			case M_RELEASE:
				outgoingCDAPMessage = getMReleaseResponse(incomingCDAPMessage);
				end = true;
				log.info("Terminating CDAP Session");
				break;
			default:
				log.info("Received a response message, it is not for me");
				break;
			}
			
			if (outgoingCDAPMessage != null){
				this.sendCDAPMessage(outgoingCDAPMessage);
			}
		} catch (CDAPException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
			if (ex.getCDAPMessage().getInvokeID() != 0){
				this.sendErrorMessage(ex);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			end = true;
		}
	}
	
	private CDAPMessage getMConnectResponse(CDAPMessage cdapMessage) throws CDAPException{
		return cdapSessionManager.getOpenConnectionResponseMessage(socket.getPort(), cdapMessage.getAuthMech(), cdapMessage.getAuthValue(), cdapMessage.getSrcAEInst(), cdapMessage.getSrcAEName(), 
				cdapMessage.getSrcApInst(), cdapMessage.getSrcApName(), 0, null, cdapMessage.getDestAEInst(), 
				cdapMessage.getDestAEName(), cdapMessage.getDestApInst(), cdapMessage.getDestApName(), cdapMessage.getInvokeID());
	}
	
	private CDAPMessage getMCreateResponse(CDAPMessage cdapMessage) throws CDAPException{
		return cdapSessionManager.getCreateObjectResponseMessage(socket.getPort(), cdapMessage.getFlags(), 
				cdapMessage.getObjClass(), 12345, cdapMessage.getObjName(), cdapMessage.getObjValue(), 0, "", cdapMessage.getInvokeID());
	}
	
	private CDAPMessage getMStartResponse(CDAPMessage cdapMessage) throws CDAPException{
		return cdapSessionManager.getStartObjectResponseMessage(socket.getPort(), cdapMessage.getFlags(), 0, "", cdapMessage.getInvokeID());
	}
	
	private CDAPMessage getMWriteResponse(CDAPMessage cdapMessage) throws CDAPException{
		return cdapSessionManager.getWriteObjectResponseMessage(socket.getPort(), cdapMessage.getFlags(), 0, "", cdapMessage.getInvokeID());
	}
	
	private CDAPMessage getMDeleteResponse(CDAPMessage cdapMessage) throws CDAPException{
		return cdapSessionManager.getDeleteObjectResponseMessage(socket.getPort(), cdapMessage.getFlags(), cdapMessage.getObjClass(), 
				cdapMessage.getObjInst(), cdapMessage.getObjName(), 0, "", cdapMessage.getInvokeID());
	}
	
	private CDAPMessage getMCancelReadResponse(CDAPMessage cdapMessage) throws CDAPException{
		return cdapSessionManager.getCancelReadResponseMessage(socket.getPort(), cdapMessage.getFlags(), cdapMessage.getInvokeID(), 0, "");
	}
	
	private CDAPMessage getMStopResponse(CDAPMessage cdapMessage) throws CDAPException{
		return cdapSessionManager.getStopObjectResponseMessage(socket.getPort(), cdapMessage.getFlags(), 0, "", cdapMessage.getInvokeID());
	}
	
	private CDAPMessage getMReleaseResponse(CDAPMessage cdapMessage) throws CDAPException{
		return cdapSessionManager.getReleaseConnectionResponseMessage(socket.getPort(), cdapMessage.getFlags(), 0, "", cdapMessage.getInvokeID());
	}
	
	private CDAPMessage getMReadResponse(CDAPMessage cdapMessage) throws CDAPException{
		ObjectValue objectValue = new ObjectValue();
		objectValue.setStrval("Overwriting the value of the fake Flow message");
		return cdapSessionManager.getReadObjectResponseMessage(socket.getPort(), cdapMessage.getFlags(), cdapMessage.getObjClass(), 
							cdapMessage.getObjInst(), cdapMessage.getObjName(), objectValue, 0, "", cdapMessage.getInvokeID());
	}
}
