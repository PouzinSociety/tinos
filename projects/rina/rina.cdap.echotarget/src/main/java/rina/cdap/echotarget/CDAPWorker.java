package rina.cdap.echotarget;

import java.io.IOException;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.delimiting.api.Delimiter;
import rina.encoding.api.Encoder;

/**
 * Gets a delimited CDAP message and processes it
 * @author eduardgrasa
 *
 */
public abstract class CDAPWorker implements Runnable{
	
	private static final Log log = LogFactory.getLog(CDAPWorker.class);
	
	/**
	 * The cdap session manager
	 */
	protected CDAPSessionManager cdapSessionManager = null;
	
	/**
	 * Used for delimiting the incoming and outgoing messages
	 */
	protected Delimiter delimiter = null;
	
	/**
	 * Used for converting data structures to bytes using a serializer- normally will be GPB
	 */
	protected Encoder encoder = null;
	
	protected Socket socket = null;
	
	protected boolean end = false;
	
	public CDAPWorker(Socket socket, CDAPSessionManager cdapSessionManager, Delimiter delimiter, Encoder encoder){
		this.socket = socket;
		this.cdapSessionManager = cdapSessionManager;
		this.delimiter = delimiter;
		this.encoder = encoder;
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
	
	protected String printBytes(byte[] message){
		String result = "";
		for(int i=0; i<message.length; i++){
			result = result + String.format("%02X", message[i]) + " ";
		}
		
		return result;
	}
	
	protected synchronized void sendErrorMessage(CDAPException cdapException){
		CDAPMessage wrongMessage = cdapException.getCDAPMessage();
		CDAPMessage returnMessage = null;

		switch(wrongMessage.getOpCode()){
		case M_CONNECT:
			try{
				returnMessage = CDAPMessage.getOpenConnectionResponseMessage(wrongMessage.getAuthMech(), wrongMessage.getAuthValue(), wrongMessage.getSrcAEInst(), 
						wrongMessage.getSrcAEName(), wrongMessage.getSrcApInst(), wrongMessage.getSrcApName(), wrongMessage.getInvokeID(), cdapException.getResult(), 
						cdapException.getResultReason(), wrongMessage.getDestAEInst(), wrongMessage.getDestAEName(), wrongMessage.getDestApInst(), 
						wrongMessage.getDestApName());
			}catch(CDAPException ex){
				ex.printStackTrace();
			}
			break;
		case M_CREATE:
			try{
				returnMessage = CDAPMessage.getCreateObjectResponseMessage(wrongMessage.getFlags(), 
						wrongMessage.getInvokeID(), wrongMessage.getObjClass(), wrongMessage.getObjInst(), wrongMessage.getObjName(), wrongMessage.getObjValue(), 
						cdapException.getResult(), cdapException.getResultReason());
			}catch(CDAPException ex){
				ex.printStackTrace();
			}
			break;
		case M_DELETE:
			try{
				returnMessage = CDAPMessage.getDeleteObjectResponseMessage(wrongMessage.getFlags(), wrongMessage.getInvokeID(), wrongMessage.getObjClass(), 
						wrongMessage.getObjInst(), wrongMessage.getObjName(), cdapException.getResult(), cdapException.getResultReason());
			}catch(CDAPException ex){
				ex.printStackTrace();
			}
			break;
		case M_READ:
			try{
				returnMessage = CDAPMessage.getReadObjectResponseMessage(wrongMessage.getFlags(), wrongMessage.getInvokeID(), wrongMessage.getObjClass(), 
						wrongMessage.getObjInst(), wrongMessage.getObjName(), wrongMessage.getObjValue(), cdapException.getResult(), cdapException.getResultReason());
			}catch(CDAPException ex){
				ex.printStackTrace();
			}
			break;
		case M_WRITE:
			try{
				returnMessage = CDAPMessage.getWriteObjectResponseMessage(wrongMessage.getFlags(), wrongMessage.getInvokeID(), cdapException.getResult(), 
						cdapException.getResultReason());
			}catch(CDAPException ex){
				ex.printStackTrace();
			}
			break;
		case M_CANCELREAD:
			try{
				returnMessage = CDAPMessage.getCancelReadResponseMessage(wrongMessage.getFlags(), wrongMessage.getInvokeID(), cdapException.getResult(), 
						cdapException.getResultReason());
			}catch(CDAPException ex){
				ex.printStackTrace();
			}
			break;
		case M_START:
			try{
				returnMessage = CDAPMessage.getStartObjectResponseMessage(wrongMessage.getFlags(), wrongMessage.getInvokeID(), cdapException.getResult(), 
						cdapException.getResultReason());
			}catch(CDAPException ex){
				ex.printStackTrace();
			}
			break;
		case M_STOP:
			try{
				returnMessage = CDAPMessage.getStopObjectResponseMessage(wrongMessage.getFlags(), wrongMessage.getInvokeID(), cdapException.getResult(), 
						cdapException.getResultReason());
			}catch(CDAPException ex){
				ex.printStackTrace();
			}
			break;
		case M_RELEASE:
			try{
				returnMessage = CDAPMessage.getReleaseConnectionResponseMessage(wrongMessage.getFlags(), wrongMessage.getInvokeID(), cdapException.getResult(), 
						cdapException.getResultReason());
			}catch(CDAPException ex){
				ex.printStackTrace();
			}
			break;
		}

		if (returnMessage != null){
			try{
				this.sendCDAPMessage(returnMessage);
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
	public synchronized void sendCDAPMessage(CDAPMessage cdapMessage) throws CDAPException, IOException{
		byte[] serializedCDAPMessageToBeSend = null;
		byte[] delimitedSdu = null;
		
		serializedCDAPMessageToBeSend = cdapSessionManager.encodeNextMessageToBeSent(cdapMessage, socket.getPort());
		delimitedSdu = delimiter.getDelimitedSdu(serializedCDAPMessageToBeSend);
		socket.getOutputStream().write(delimitedSdu);
		cdapSessionManager.messageSent(cdapMessage, socket.getPort());
		log.info("Sent CDAP Message: "+ cdapMessage.toString());
		log.info("Sent SDU:" + printBytes(delimitedSdu));
	}
	
	protected abstract void processCDAPMessage(byte[] serializedCDAPMessage);
}
