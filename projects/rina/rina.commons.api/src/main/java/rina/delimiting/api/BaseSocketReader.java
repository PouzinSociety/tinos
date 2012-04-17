package rina.delimiting.api;

import java.io.IOException;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is a base socket reader that is continuously reading a socket looking for 
 * delimited pdus. When it finds one it will call the abstract operation processPDU. If the socket 
 * ends or is disconnected, then the abstract operation socketClosed is invoked.
 * @author eduardgrasa
 *
 */
public abstract class BaseSocketReader implements Runnable{

	private static final Log log = LogFactory.getLog(BaseSocketReader.class);
	
	private Delimiter delimiter = null;
	private Socket socket = null;
	
	/**
	 * Controls when the reader will finish the execution
	 */
	private boolean end = false;

	public BaseSocketReader(Socket socket, Delimiter delimiter){
		this.socket = socket;
		this.delimiter = delimiter;
	}
	
	public Delimiter getDelimiter(){
		return delimiter;
	}

	public void run() {
		boolean lookingForSduLength = true;
		int length = 0;
		int index = 0;
		byte[] lastSduLengthCandidate = new byte[0];
		byte[] currentSduLengthCandidate = null;
		byte[] pdu = null;
		byte nextByte = 0;
		int value = 0;
		
		log.debug("Reading socket from remote interface: "+socket.getInetAddress().getHostAddress() + "\n" 
				+ "Local port_id: "+socket.getLocalPort() + "\n" 
				+ "Remote port_id: "+socket.getPort());
		
		while(!end){
			//Delimit the byte array that contains a serialized CDAP message
			try{
				value = socket.getInputStream().read();
				if (value == -1){
					break;
				}
	
				nextByte = (byte) value;
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
							lookingForSduLength = false;
						}
					}
				}else{
					if (index < length){
						if (pdu == null){
							pdu = new byte[length];
						}
						pdu[index] = nextByte;
						index ++;
						if (index == length){
							log.debug("Received PDU of length "+length+" through socket "+socket.getPort()+": "+printBytes(pdu));
							processPDU(pdu);
							index = 0;
							length = 0;
							lookingForSduLength = true;
							pdu = null;
						}
					}
				}
			}catch(IOException ex){
				end = true;
			}
		}
		
		try{
			socket.close();
		}catch(IOException ex){
		}
		
		log.info("The remote endpoint of socket "+socket.getPort()+" has disconnected");
		socketDisconnected();
	}
	
	public Socket getSocket(){
		return this.socket;
	}
	
	/**
	 * process the pdu that has been found
	 * @param pdu
	 */
	public abstract void processPDU(byte[] pdu);
	
	/**
	 * Invoked when the socket is disconnected
	 */
	public abstract void socketDisconnected();
	
	public String printBytes(byte[] message){
		String result = "";
		for(int i=0; i<message.length; i++){
			result = result + String.format("%02X", message[i]) + " ";
		}
		
		return result;
	}
}
