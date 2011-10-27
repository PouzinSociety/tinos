package rina.cdap.echotarget;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.ObjectValue;
import rina.cdap.impl.CDAPSessionManagerImpl;
import rina.cdap.impl.WireMessageProviderFactory;
import rina.cdap.impl.googleprotobuf.GoogleProtocolBufWireMessageProviderFactory;
import rina.delimiting.api.DelimiterFactory;
import rina.delimiting.impl.DelimiterFactoryImpl;
import rina.encoding.api.EncoderFactory;

/**
 * Client of the CDAP Echo Server
 * @author eduardgrasa
 *
 */
public class CDAPEchoClient extends CDAPClient{
	
	private static final Log log = LogFactory.getLog(CDAPEchoClient.class);
	private static final int DEFAULTPORT = 32767;
	private static final String DEFAULTHOST = "84.88.41.36";
	
	public CDAPEchoClient(CDAPSessionManager cdapSessionManager, DelimiterFactory delimiterFactory, 
			EncoderFactory encoderFactory, String host, int port){
		super(cdapSessionManager, delimiterFactory, encoderFactory, host, port);
	}
	
	/**
	 * It will cause the following flow of messages to be exchanged:
	 * M_CONNECT, M_CONNECT_R, M_CREATE, M_CREATE_R, M_START, M_START_R, M_WRITE, M_WRITE_R, M_READ, M_READ_R, M_CANCELREAD,
	 * M_CANCELREAD_R, M_STOP, M_STOP_R, M_DELETE, M_DELETE_R, M_RELEASE, M_RELEASE_R
	 * @param cdapMessage
	 */
	protected void processCDAPMessage(byte[] serializedCDAPMessage){
		log.info("Processing serialized CDAP message. This is the serialized message: ");
		log.info(printBytes(serializedCDAPMessage));
		CDAPMessage incomingCDAPMessage = null;
		CDAPMessage outgoingCDAPMessage = null;
		
		try {
			incomingCDAPMessage = cdapSessionManager.messageReceived(serializedCDAPMessage, clientSocket.getLocalPort());
			log.info("Received CDAP message: "+incomingCDAPMessage.toString());
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
				return;
			default:
				log.info("Received a response message, it is not for me");
				end = true;
				return;
			}
			
			sendCDAPMessage(outgoingCDAPMessage);
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
		return cdapSessionManager.getCreateObjectRequestMessage(clientSocket.getLocalPort(), null, null, "rina.flowallocator.api.message.Flow", 0, "1234", objectValue, 0, true);
	}
	
	private CDAPMessage getMStartMessage(CDAPMessage cdapMessage) throws CDAPException{
		return cdapSessionManager.getStartObjectRequestMessage(clientSocket.getLocalPort(), null, null, cdapMessage.getObjClass(), 
														null, cdapMessage.getObjInst(), cdapMessage.getObjName(), 0, true);
	}
	
	private CDAPMessage getMWriteMessage(CDAPMessage cdapMessage) throws CDAPException{
		ObjectValue objectValue = new ObjectValue();
		objectValue.setStrval("Overwriting the value of the fake Flow message");
		return cdapSessionManager.getWriteObjectRequestMessage(clientSocket.getLocalPort(), null, null, cdapMessage.getObjClass(), 
														cdapMessage.getObjInst(), objectValue, cdapMessage.getObjName(), 0, true);
	}
	
	private CDAPMessage getMReadMessage(CDAPMessage cdapMessage) throws CDAPException{
		return cdapSessionManager.getReadObjectRequestMessage(clientSocket.getLocalPort(), null, null, "Enrollment", 
				cdapMessage.getObjInst(), "daf.management.enrollment", 0, true);
	}
	
	private CDAPMessage getMStopMessage(CDAPMessage cdapMessage) throws CDAPException{
		return cdapSessionManager.getStopObjectRequestMessage(clientSocket.getLocalPort(), null, null, cdapMessage.getObjClass(), null, 
					cdapMessage.getObjInst(), cdapMessage.getObjName(), 0, true);
	}
	
	private CDAPMessage getMDeleteMessage(CDAPMessage cdapMessage) throws CDAPException{
		return cdapSessionManager.getDeleteObjectRequestMessage(clientSocket.getLocalPort(), null, null, cdapMessage.getObjClass(), 
				cdapMessage.getObjInst(), cdapMessage.getObjName(), 0, true);
	}
	
	private CDAPMessage getMReleaseMessage(CDAPMessage cdapMessage) throws CDAPException{
		return cdapSessionManager.getReleaseConnectionRequestMessage(clientSocket.getLocalPort(), null, true);
	}
	
	public static void main(String[] args){
		CDAPSessionManagerImpl cdapSessionManager = new CDAPSessionManagerImpl();
		WireMessageProviderFactory wmpFactory = new GoogleProtocolBufWireMessageProviderFactory();
		cdapSessionManager.setWireMessageProviderFactory(wmpFactory);
		DelimiterFactory delimiterFactory = new DelimiterFactoryImpl();
		CDAPEchoClient cdapEchoClient = new CDAPEchoClient(cdapSessionManager, delimiterFactory, null, DEFAULTHOST, DEFAULTPORT);
		cdapEchoClient.run();
	}
}
