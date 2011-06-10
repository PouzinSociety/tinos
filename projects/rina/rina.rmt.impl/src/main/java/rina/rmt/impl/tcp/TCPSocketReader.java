package rina.rmt.impl.tcp;

import java.io.IOException;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.delimiting.api.Delimiter;
import rina.ribdaemon.api.RIBDaemon;

/**
 * Reads a TCP socket, and gets delimited messages out of it.
 * Then it calls the RIB Daemon and delivers the message to it
 * @author eduardgrasa
 *
 */
public class TCPSocketReader implements Runnable{
	
	private static final Log log = LogFactory.getLog(TCPSocketReader.class);
	
	private RIBDaemon ribdaemon = null;
	
	private Delimiter delimiter = null;
	
	private Socket socket = null;
	
	/**
	 * Controls when the reader will finish the execution
	 */
	private boolean end = false;
	
	public TCPSocketReader(Socket socket, RIBDaemon ribdaemon, Delimiter delimiter){
		this.socket = socket;
		this.ribdaemon = ribdaemon;
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
		
		log.info("Reading socket from remote interface: "+socket.getInetAddress().getHostAddress() + "\n" 
				+ "Local port_id: "+socket.getLocalPort() + "\n" 
				+ "Remote port_id: "+socket.getPort());
		
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
							ribdaemon.cdapMessageDelivered(serializedCDAPMessage, socket.getLocalPort());
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
}
