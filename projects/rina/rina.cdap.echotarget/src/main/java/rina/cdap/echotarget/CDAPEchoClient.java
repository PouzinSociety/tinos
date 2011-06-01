package rina.cdap.echotarget;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSessionFactory;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.ObjectValue;
import rina.cdap.impl.CDAPSessionFactoryImpl;
import rina.cdap.impl.WireMessageProviderFactory;
import rina.cdap.impl.googleprotobuf.GoogleProtocolBufWireMessageProviderFactory;
import rina.delimiting.api.DelimiterFactory;
import rina.delimiting.impl.DelimiterFactoryImpl;
import rina.serialization.api.SerializationFactory;

/**
 * Client of the CDAP Echo Server
 * @author eduardgrasa
 *
 */
public class CDAPEchoClient extends CDAPClient{
	
	private static final Log log = LogFactory.getLog(CDAPEchoClient.class);
	private static final int DEFAULTPORT = 32767;
	private static final String DEFAULTHOST = "84.88.41.36";
	
	public CDAPEchoClient(CDAPSessionFactory cdapSessionFactory, DelimiterFactory delimiterFactory, 
			SerializationFactory serializationFactory, String host, int port){
		super(cdapSessionFactory, delimiterFactory, serializationFactory, host, port);
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
			incomingCDAPMessage = cdapSession.messageReceived(serializedCDAPMessage);
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
		return CDAPMessage.getReleaseConnectionRequestMessage(null, 42);
	}
	
	public static void main(String[] args){
		CDAPSessionFactoryImpl cdapSessionFactory = new CDAPSessionFactoryImpl();
		WireMessageProviderFactory wmpFactory = new GoogleProtocolBufWireMessageProviderFactory();
		cdapSessionFactory.setWireMessageProviderFactory(wmpFactory);
		DelimiterFactory delimiterFactory = new DelimiterFactoryImpl();
		CDAPEchoClient cdapEchoClient = new CDAPEchoClient(cdapSessionFactory, delimiterFactory, null, DEFAULTHOST, DEFAULTPORT);
		cdapEchoClient.run();
	}
}
