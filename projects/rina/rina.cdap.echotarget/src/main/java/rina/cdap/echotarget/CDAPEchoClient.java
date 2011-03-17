package rina.cdap.echotarget;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSession;
import rina.cdap.api.CDAPSessionFactory;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.AuthTypes;
import rina.cdap.api.message.ObjectValue;
import rina.cdap.impl.CDAPSessionFactoryImpl;
import rina.cdap.impl.WireMessageProviderFactory;
import rina.cdap.impl.googleprotobuf.GoogleProtocolBufWireMessageProviderFactory;
import rina.delimiting.api.Delimiter;
import rina.delimiting.api.DelimiterFactory;
import rina.delimiting.impl.DelimiterFactoryImpl;

/**
 * Client of the CDAP Echo Server
 * @author eduardgrasa
 *
 */
public class CDAPEchoClient {
	
	private static final Log log = LogFactory.getLog(CDAPEchoClient.class);
	
	private static final int DEFAULTPORT = 32767;
	
	private static final String DEFAULTHOST = "localhost";
	
	/**
	 * The cdap session
	 */
	private CDAPSession cdapSession = null;
	
	/**
	 * The delimiter for the sessions
	 */
	private Delimiter delimiter = null;
	
	/**
	 * The TCP port where the CDAP Echo server is listening
	 */
	private int port = 0;
	
	/**
	 * The host where the CDAP Echo Server is running
	 */
	private String host = null;
	
	/**
	 * The socket to connect to the server
	 */
	private Socket clientSocket = null;
	
	/**
	 * Tells when to stop listening the socket 
	 * for incoming bytes
	 */
	private boolean end = false;
	
	public CDAPEchoClient(CDAPSessionFactory cdapSessionFactory, DelimiterFactory delimiterFactory, String host, int port){
		this.cdapSession = cdapSessionFactory.createCDAPSession();
		this.delimiter = delimiterFactory.createDelimiter(DelimiterFactory.DIF);
		this.host = host;
		this.port = port;
	}
	
	public void run(){
		try {
			clientSocket = new Socket(host, port);
			
			//1 Create an M_CONNECT message, delimit it and send it to the CDAP Echo Target
			CDAPMessage message = CDAPMessage.getOpenConnectionRequestMessage(0, AuthTypes.AUTH_NONE, null, null, "mock", null, "B", "234", "mock", "123", "A", 0);
			byte[] serializedCDAPMessage = cdapSession.serializeNextMessageToBeSent(message);
			byte[] delimitedSdu = delimiter.getDelimitedSdu(serializedCDAPMessage);
			clientSocket.getOutputStream().write(delimitedSdu);
			cdapSession.messageSent(message);
			
			//2 Enter the loop to wait for response messages, and continue the message exchange while possible
			byte nextByte = 0;
			boolean lookingForSduLength = true;
			byte[] lastSduLengthCandidate = new byte[0];
			byte[] currentSduLengthCandidate = null;
			int length = 0;
			int index = 0;
			while(!end){
				try{
					nextByte = (byte) clientSocket.getInputStream().read();
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
				}
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CDAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * It will cause the following flow of messages to be exchanged:
	 * M_CONNECT, M_CONNECT_R, M_CREATE, M_CREATE_R, M_START, M_START_R, M_WRITE, M_WRITE_R, M_READ, M_READ_R, M_CANCELREAD,
	 * M_CANCELREAD_R, M_STOP, M_STOP_R, M_DELETE, M_DELETE_R, M_RELEASE, M_RELEASE_R
	 * @param cdapMessage
	 */
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
			case M_CONNECT_R:
				outgoingCDAPMessage = getMCreateMessage(incomingCDAPMessage);
				break;
			case M_CREATE_R:
				outgoingCDAPMessage = getMStartMessage(incomingCDAPMessage);
				break;
			case M_START_R:
				outgoingCDAPMessage = getMWriteMessage(incomingCDAPMessage);
				break;
			case M_WRITE_R:
				outgoingCDAPMessage = getMReadMessage(incomingCDAPMessage);
				break;
			case M_READ_R:
				outgoingCDAPMessage = getMStopMessage(incomingCDAPMessage);
				break;
			case M_STOP_R:
				outgoingCDAPMessage = getMDeleteMessage(incomingCDAPMessage);
				break;
			case M_DELETE_R:
				outgoingCDAPMessage = getMReleaseMessage(incomingCDAPMessage);
				break;
			case M_RELEASE_R:
				log.info("CDAP Session terminated. Client stopping");
				end = true;
				break;
			default:
				log.info("Received a response message, it is not for me");
				end = true;
				break;
			}
			
			serializedCDAPMessageToBeSend = cdapSession.serializeNextMessageToBeSent(outgoingCDAPMessage);
			log.info("Sending CDAP message: "+outgoingCDAPMessage.toString());
			delimitedSdu = delimiter.getDelimitedSdu(serializedCDAPMessageToBeSend);
			clientSocket.getOutputStream().write(delimitedSdu);
			cdapSession.messageSent(outgoingCDAPMessage);
		} catch (CDAPException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private CDAPMessage getMCreateMessage(CDAPMessage cdapMessage) throws CDAPException{
		ObjectValue objectValue = new ObjectValue();
		objectValue.setStrval("This is a fake Flow message");
		return CDAPMessage.getCreateObjectRequestMessage(null, null, 25, "rina.flowallocator.api.message.Flow", 0, "1234", objectValue, 0);
	}
	
	private CDAPMessage getMStartMessage(CDAPMessage cdapMessage) throws CDAPException{
		return CDAPMessage.getStartObjectRequestMessage(null, null, 89, cdapMessage.getObjClass(), 
														null, cdapMessage.getObjInst(), cdapMessage.getObjName(), 0);
	}
	
	private CDAPMessage getMWriteMessage(CDAPMessage cdapMessage) throws CDAPException{
		ObjectValue objectValue = new ObjectValue();
		objectValue.setStrval("Overwriting the value of the fake Flow message");
		return CDAPMessage.getWriteObjectRequestMessage(null, null, 940, cdapMessage.getObjClass(), 
														cdapMessage.getObjInst(), objectValue, cdapMessage.getObjName(), 0);
	}
	
	private CDAPMessage getMReadMessage(CDAPMessage cdapMessage) throws CDAPException{
		return CDAPMessage.getReadObjectRequestMessage(null, null, 46, cdapMessage.getObjClass(), 
				cdapMessage.getObjInst(), cdapMessage.getObjName(), 0);
	}
	
	private CDAPMessage getMStopMessage(CDAPMessage cdapMessage) throws CDAPException{
		return CDAPMessage.getStopObjectRequestMessage(null, null, 365, cdapMessage.getObjClass(), null, 
					cdapMessage.getObjInst(), cdapMessage.getObjName(), 0);
	}
	
	private CDAPMessage getMDeleteMessage(CDAPMessage cdapMessage) throws CDAPException{
		return CDAPMessage.getDeleteObjectRequestMessage(null, null, 1231, cdapMessage.getObjClass(), 
				cdapMessage.getObjInst(), cdapMessage.getObjName(), 0);
	}
	
	private CDAPMessage getMReleaseMessage(CDAPMessage cdapMessage) throws CDAPException{
		return CDAPMessage.getReleaseConnectionRequestMessage(null, 42, "234", "mock");
	}
	
	public static void main(String[] args){
		CDAPSessionFactoryImpl cdapSessionFactory = new CDAPSessionFactoryImpl();
		WireMessageProviderFactory wmpFactory = new GoogleProtocolBufWireMessageProviderFactory();
		cdapSessionFactory.setWireMessageProviderFactory(wmpFactory);
		DelimiterFactory delimiterFactory = new DelimiterFactoryImpl();
		CDAPEchoClient cdapEchoClient = new CDAPEchoClient(cdapSessionFactory, delimiterFactory, DEFAULTHOST, DEFAULTPORT);
		cdapEchoClient.run();
	}
	
	private String printBytes(byte[] message){
		String result = "";
		for(int i=0; i<message.length; i++){
			result = result + message[i] + " ";
		}
		
		return result;
	}

}
