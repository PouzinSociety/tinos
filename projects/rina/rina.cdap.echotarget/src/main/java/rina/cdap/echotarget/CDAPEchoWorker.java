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
import rina.serialization.api.Serializer;

/**
 * 
 * @author eduardgrasa
 *
 */
public class CDAPEchoWorker extends CDAPWorker {
	
	private static final Log log = LogFactory.getLog(CDAPEchoWorker.class);
	
	public CDAPEchoWorker(Socket socket, CDAPSessionManager cdapSessionManager, Delimiter delimiter, Serializer serializer) {
		super(socket, cdapSessionManager, delimiter, serializer);
	}
	
	protected void processCDAPMessage(byte[] serializedCDAPMessage){
		log.info("Processing serialized CDAP message. This is the serialized message: ");
		log.info(printBytes(serializedCDAPMessage));
		CDAPMessage incomingCDAPMessage = null;
		CDAPMessage outgoingCDAPMessage = null;
		byte[] serializedCDAPMessageToBeSend = null;
		byte[] delimitedSdu = null;
		
		try {
			incomingCDAPMessage = cdapSessionManager.messageReceived(serializedCDAPMessage, socket.getLocalPort());
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
			
			serializedCDAPMessageToBeSend = cdapSessionManager.encodeNextMessageToBeSent(outgoingCDAPMessage,socket.getLocalPort());
			log.info("Replying with CDAP message: "+outgoingCDAPMessage.toString());
			delimitedSdu = delimiter.getDelimitedSdu(serializedCDAPMessageToBeSend);
			socket.getOutputStream().write(delimitedSdu);
			cdapSessionManager.messageSent(outgoingCDAPMessage, socket.getLocalPort());
		} catch (CDAPException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			end = true;
		}
	}
	
	private CDAPMessage getMConnectResponse(CDAPMessage cdapMessage) throws CDAPException{
		return CDAPMessage.getOpenConnectionResponseMessage(cdapMessage.getAuthMech(), cdapMessage.getAuthValue(), cdapMessage.getSrcAEInst(), cdapMessage.getSrcAEName(), 
				cdapMessage.getSrcApInst(), cdapMessage.getSrcApName(), cdapMessage.getInvokeID(), 0, null, cdapMessage.getDestAEInst(), 
				cdapMessage.getDestAEName(), cdapMessage.getDestApInst(), cdapMessage.getDestApName(), (int)cdapMessage.getVersion());
	}
	
	private CDAPMessage getMCreateResponse(CDAPMessage cdapMessage) throws CDAPException{
		return CDAPMessage.getCreateObjectResponseMessage(cdapMessage.getFlags(), 
				cdapMessage.getInvokeID(), cdapMessage.getObjClass(), 12345, cdapMessage.getObjName(), cdapMessage.getObjValue(), 0, "");
	}
	
	private CDAPMessage getMStartResponse(CDAPMessage cdapMessage) throws CDAPException{
		return CDAPMessage.getStartObjectResponseMessage(cdapMessage.getFlags(), cdapMessage.getInvokeID(), 0, "");
	}
	
	private CDAPMessage getMWriteResponse(CDAPMessage cdapMessage) throws CDAPException{
		return CDAPMessage.getWriteObjectResponseMessage(cdapMessage.getFlags(), cdapMessage.getInvokeID(), 0, "");
	}
	
	private CDAPMessage getMDeleteResponse(CDAPMessage cdapMessage) throws CDAPException{
		return CDAPMessage.getDeleteObjectResponseMessage(cdapMessage.getFlags(), cdapMessage.getInvokeID(), cdapMessage.getObjClass(), 
				cdapMessage.getObjInst(), cdapMessage.getObjName(), 0, "");
	}
	
	private CDAPMessage getMCancelReadResponse(CDAPMessage cdapMessage) throws CDAPException{
		return CDAPMessage.getCancelReadResponseMessage(cdapMessage.getFlags(), cdapMessage.getInvokeID(), 0, "");
	}
	
	private CDAPMessage getMStopResponse(CDAPMessage cdapMessage) throws CDAPException{
		return CDAPMessage.getStopObjectResponseMessage(cdapMessage.getFlags(), cdapMessage.getInvokeID(), 0, "");
	}
	
	private CDAPMessage getMReleaseResponse(CDAPMessage cdapMessage) throws CDAPException{
		return CDAPMessage.getReleaseConnectionResponseMessage(cdapMessage.getFlags(), cdapMessage.getInvokeID(), 0, "");
	}
	
	private CDAPMessage getMReadResponse(CDAPMessage cdapMessage) throws CDAPException{
		ObjectValue objectValue = new ObjectValue();
		objectValue.setStrval("Overwriting the value of the fake Flow message");
		return CDAPMessage.getReadObjectResponseMessage(cdapMessage.getFlags(), cdapMessage.getInvokeID(), cdapMessage.getObjClass(), 
							cdapMessage.getObjInst(), cdapMessage.getObjName(), objectValue, 0, "");
	}
}
