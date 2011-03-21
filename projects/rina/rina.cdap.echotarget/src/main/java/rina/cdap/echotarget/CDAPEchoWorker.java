package rina.cdap.echotarget;

import java.io.IOException;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSession;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.ObjectValue;
import rina.delimiting.api.Delimiter;

/**
 * 
 * @author eduardgrasa
 *
 */
public class CDAPEchoWorker implements Runnable {
	
	private static final Log log = LogFactory.getLog(CDAPEchoWorker.class);
	
	/**
	 * The cdap session
	 */
	private CDAPSession cdapSession = null;
	
	/**
	 * Used for delimiting the incoming and outgoing messages
	 */
	private Delimiter delimiter = null;
	
	private Socket socket = null;
	
	private boolean end = false;
	
	public CDAPEchoWorker(Socket socket, CDAPSession cdapSession, Delimiter delimiter){
		this.socket = socket;
		this.cdapSession = cdapSession;
		this.delimiter = delimiter;
	}

	public void run() {
		boolean lookingForSduLength = true;
		int length = 0;
		int index = 0;
		byte[] lastSduLengthCandidate = new byte[0];
		byte[] currentSduLengthCandidate = null;
		byte[] serializedCDAPMessage = null;
		byte nextByte = 0;
		
		while(!end){
			//Delimit the byte array that contains a serialized CDAP message
			try{
				nextByte = (byte) socket.getInputStream().read();
				if (lookingForSduLength){
					currentSduLengthCandidate = new byte[lastSduLengthCandidate.length + 1];
					for(int i=0; i<lastSduLengthCandidate.length; i++){
						currentSduLengthCandidate[i] = lastSduLengthCandidate[i];
					}
					currentSduLengthCandidate[lastSduLengthCandidate.length] = nextByte;
					length = delimiter.readVarint32(currentSduLengthCandidate);
					if (length == -2){
						lastSduLengthCandidate = currentSduLengthCandidate;
					}else{
						lastSduLengthCandidate = new byte[0];
						if (length > 0){
							log.info("Found a delimited CDAP message, of length " + length);
							lookingForSduLength = false;
						}
					}
				}else{
					if (index < length){
						if (serializedCDAPMessage == null){
							serializedCDAPMessage = new byte[length];
						}
						serializedCDAPMessage[index] = nextByte;
						index ++;
						if (index == length){
							processCDAPMessage(serializedCDAPMessage);
							index = 0;
							length = 0;
							lookingForSduLength = true;
							serializedCDAPMessage = null;
						}
					}
				}
			}catch(IOException ex){
				ex.printStackTrace();
				end = true;
			}
		}
	}
	
	private void processCDAPMessage(byte[] serializedCDAPMessage){
		log.info("Processing serialized CDAP message. This is the serialized message: ");
		log.info(printBytes(serializedCDAPMessage));
		CDAPMessage incomingCDAPMessage = null;
		CDAPMessage outgoingCDAPMessage = null;
		byte[] serializedCDAPMessageToBeSend = null;
		byte[] delimitedSdu = null;
		
		try {
			incomingCDAPMessage = cdapSession.messageReceived(serializedCDAPMessage);
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
			
			serializedCDAPMessageToBeSend = cdapSession.serializeNextMessageToBeSent(outgoingCDAPMessage);
			log.info("Replying with CDAP message: "+outgoingCDAPMessage.toString());
			delimitedSdu = delimiter.getDelimitedSdu(serializedCDAPMessageToBeSend);
			socket.getOutputStream().write(delimitedSdu);
			cdapSession.messageSent(outgoingCDAPMessage);
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
				cdapMessage.getSrcApInst(), cdapMessage.getSrcApName(), 0, null, cdapMessage.getDestAEInst(), 
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
		return CDAPMessage.getReleaseConnectionResponseMessage(cdapMessage.getFlags(), cdapMessage.getInvokeID(), 0, "", null, null);
	}
	
	private CDAPMessage getMReadResponse(CDAPMessage cdapMessage) throws CDAPException{
		ObjectValue objectValue = new ObjectValue();
		objectValue.setStrval("Overwriting the value of the fake Flow message");
		return CDAPMessage.getReadObjectResponseMessage(cdapMessage.getFlags(), cdapMessage.getInvokeID(), cdapMessage.getObjClass(), 
							cdapMessage.getObjInst(), cdapMessage.getObjName(), objectValue, 0, "");
	}
	
	private String printBytes(byte[] message){
		String result = "";
		for(int i=0; i<message.length; i++){
			result = result + String.format("%02X", message[i]) + " ";
		}
		
		return result;
	}
}
