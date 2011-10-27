package rina.ipcmanager.impl.server;

import java.io.IOException;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.delimiting.api.Delimiter;
import rina.ipcmanager.impl.IPCManagerImpl;
import rina.ipcservice.api.IPCException;
import rina.ipcservice.api.IPCService;
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
	
	private IPCManagerImpl ipcManager = null;
	
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
	
	/**
	 * Controls when the reader will finish the execution
	 */
	private boolean end = false;

	public TCPSocketReader(Socket socket, Delimiter delimiter, CDAPSessionManager cdapSessionManager, IPCManagerImpl ipcManager){
		this.socket = socket;
		this.delimiter = delimiter;
		this.ipcManager = ipcManager;
	}
	
	public void setPortId(int portId){
		this.portId = portId;
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
		
		log.info("Reading socket from remote interface: "+socket.getInetAddress().getHostAddress() + "\n" 
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
							log.debug("Found a delimited CDAP message, of length " + length);
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
							log.debug("Received PDU through flow "+socket.getPort()+": "+printBytes(pdu));
							processPDU(pdu);
							index = 0;
							length = 0;
							lookingForSduLength = true;
							pdu = null;
						}
					}
				}
			}catch(IOException ex){
				ex.printStackTrace();
				end = true;
			}
		}
		
		try{
			socket.close();
		}catch(IOException ex){
			ex.printStackTrace();
		}
		
		log.debug("The remote endpoint of flow "+socket.getPort()+" has disconnected. Notifying the RMT and the RIB Daemon");
		//TODO notify the IPC Manager Implementation
	}
	
	/**
	 * If we are on the connection establishment phase the PDU will be a delimited CDAP message,
	 * if not it will be a delimited sdu that we have to send over the flow
	 * @param pdu
	 */
	private void processPDU(byte[] sdu){
		if (!connected){
			ipcService = ipcManager.processAllocationRequest(sdu);
		}else{
			try {
				ipcService.submitTransfer(portId, sdu);
			} catch (IPCException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private String printBytes(byte[] message){
		String result = "";
		for(int i=0; i<message.length; i++){
			result = result + String.format("%02X", message[i]) + " ";
		}
		
		return result;
	}
}
