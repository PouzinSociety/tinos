package rina.cdap.impl.utils;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSession;
import rina.cdap.api.message.CDAPMessage;

/**
 * 
 * @author eduardgrasa
 *
 */
public class CDAPEchoWorker implements Runnable {
	
	private int MaxBytesPerMessage = 2000;
	
	private static final Log log = LogFactory.getLog(CDAPEchoWorker.class);
	
	/**
	 * The cdap session
	 */
	private CDAPSession cdapSession = null;
	
	private Socket socket = null;
	
	private boolean end = false;
	
	public CDAPEchoWorker(Socket socket, CDAPSession cdapSession){
		this.cdapSession = cdapSession;
		this.socket = socket;
	}

	public void run() {
		ByteBuffer buffer = ByteBuffer.allocate(MaxBytesPerMessage);
		byte[] serializedCDAPMessage = null;
		byte nextByte = 0;
		
		while(!end){
			//TODO Delimit the byte array that contains a serialized CDAP message
			try{
				nextByte = (byte) socket.getInputStream().read();
				buffer.put(nextByte);
				serializedCDAPMessage = new byte[buffer.position()];
				buffer.get(serializedCDAPMessage);
				if (isDelimitedCDAPMessage(serializedCDAPMessage)){
					processCDAPMessage(serializedCDAPMessage);
					buffer.clear();
				}
			}catch(IOException ex){
				
			}
		}
	}
	
	private boolean isDelimitedCDAPMessage(byte[] serializedCDAPMessage){
		//TODO
		return true;
	}
	
	private void processCDAPMessage(byte[] serializedCDAPMessage){
		CDAPMessage incomingCDAPMessage = null;
		CDAPMessage outgoingCDAPMessage = null;
		byte[] serializedCDAPMessageToBeSend = null;
		try {
			incomingCDAPMessage = cdapSession.messageReceived(serializedCDAPMessage);
			log.info("Received CDAP message: "+incomingCDAPMessage.toString());
			//TODO
			switch (incomingCDAPMessage.getOpCode()){
			case M_CONNECT:
				break;
			case M_CREATE:
				break;
			case M_DELETE:
				break;
			case M_READ:
				break;
			case M_CANCELREAD:
				break;
			case M_WRITE:
				break;
			case M_START:
				break;
			case M_STOP:
				break;
			case M_RELEASE:
				break;
			default:
				break;
			}
			
			serializedCDAPMessageToBeSend = cdapSession.serializeNextMessageToBeSent(outgoingCDAPMessage);
			log.info("Replying with CDAP message: "+outgoingCDAPMessage.toString());
			socket.getOutputStream().write(serializedCDAPMessageToBeSend);
		} catch (CDAPException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
