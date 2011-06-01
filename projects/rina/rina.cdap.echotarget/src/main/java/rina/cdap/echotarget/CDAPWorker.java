package rina.cdap.echotarget;

import java.io.IOException;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPSession;
import rina.delimiting.api.Delimiter;
import rina.serialization.api.Serializer;

/**
 * Gets a delimited CDAP message and processes it
 * @author eduardgrasa
 *
 */
public abstract class CDAPWorker implements Runnable{
	
	private static final Log log = LogFactory.getLog(CDAPWorker.class);
	
	/**
	 * The cdap session
	 */
	protected CDAPSession cdapSession = null;
	
	/**
	 * Used for delimiting the incoming and outgoing messages
	 */
	protected Delimiter delimiter = null;
	
	/**
	 * Used for converting data structures to bytes using a serializer- normally will be GPB
	 */
	protected Serializer serializer = null;
	
	protected Socket socket = null;
	
	protected boolean end = false;
	
	public CDAPWorker(Socket socket, CDAPSession cdapSession, Delimiter delimiter, Serializer serializer){
		this.socket = socket;
		this.cdapSession = cdapSession;
		this.delimiter = delimiter;
		this.serializer = serializer;
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
	
	protected abstract void processCDAPMessage(byte[] serializedCDAPMessage);
}
