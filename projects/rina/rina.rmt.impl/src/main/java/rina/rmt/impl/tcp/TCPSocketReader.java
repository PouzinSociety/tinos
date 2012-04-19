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
	
	private Delimiter delimiter = null;
	private Socket socket = null;
	private TCPRMTImpl rmt = null;
	private RIBDaemon ribdaemon = null;
	private int portId = 0;
	/**
	 * Controls when the reader will finish the execution
	 */
	private boolean end = false;
	
	public TCPSocketReader(Socket socket, int portId, RIBDaemon ribdaemon, Delimiter delimiter, TCPRMTImpl rmt){
		this.delimiter = delimiter;
		this.socket = socket;
		this.portId = portId;
		this.ribdaemon = ribdaemon;
		this.rmt = rmt;
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
					log.debug("Reader of portId "+ portId+": The length candidate is "+length);
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
				ex.printStackTrace();
			}
		}
		
		try{
			socket.close();
		}catch(IOException ex){
			ex.printStackTrace();
		}
		
		log.info("The remote endpoint of socket "+socket.getPort()+" has disconnected");
		socketDisconnected();
	}
	
	/**
	 * process the pdu that has been found
	 * @param pdu
	 */
	public void processPDU(byte[] pdu){
		log.debug("Passing the PDU to the RIB Daemon");
		ribdaemon.cdapMessageDelivered(pdu, portId);
	}
	
	/**
	 * Invoked when the socket is disconnected
	 */
	public void socketDisconnected(){
		log.debug("Notifying the RMT and the RIB Daemon");
		this.rmt.connectionEnded(portId);
		this.ribdaemon.flowDeallocated(portId);
	}
	
	public String printBytes(byte[] message){
		String result = "";
		for(int i=0; i<message.length; i++){
			result = result + String.format("%02X", message[i]) + " ";
		}
		
		return result;
	}
}
