package rina.cdap.echotarget.enrollment;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSessionFactory;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.Flags;
import rina.cdap.api.message.CDAPMessage.Opcode;
import rina.cdap.echotarget.CDAPClient;
import rina.cdap.impl.CDAPSessionFactoryImpl;
import rina.cdap.impl.WireMessageProviderFactory;
import rina.cdap.impl.googleprotobuf.GoogleProtocolBufWireMessageProviderFactory;
import rina.delimiting.api.DelimiterFactory;
import rina.delimiting.impl.DelimiterFactoryImpl;
import rina.serialization.api.SerializationFactory;
import rina.utils.serialization.googleprotobuf.GPBSerializationFactory;

/**
 * Client of the CDAP Enrollment Server
 * @author eduardgrasa
 *
 */
public class CDAPEnrollmentClient extends CDAPClient{
	
	private static final Log log = LogFactory.getLog(CDAPEnrollmentClient.class);
	private static final int DEFAULTPORT = 32768;
	private static final String DEFAULTHOST = "localhost";
	
	private enum State {NULL, WAITING_CONNECTION, WAITING_READ_ADDRESS, INITIALIZING_DATA, 
		WAITING_FOR_STARTUP, ENROLLED};
		
	private State state = State.NULL;
	
	public CDAPEnrollmentClient(CDAPSessionFactory cdapSessionFactory, DelimiterFactory delimiterFactory, 
			SerializationFactory serializationFactory, String host, int port){
		super(cdapSessionFactory, delimiterFactory, serializationFactory, host, port);
		
		state = State.WAITING_CONNECTION;
	}
	
	/**
	 * Will request to be enrolled to the DIF
	 * @param cdapMessage
	 */
	protected void processCDAPMessage(byte[] serializedCDAPMessage){
		CDAPMessage incomingCDAPMessage = null;
		CDAPMessage outgoingCDAPMessage = null;
		
		try {
			if (serializedCDAPMessage != null){
				incomingCDAPMessage = cdapMessageReceived(serializedCDAPMessage);
			}
			
			if (incomingCDAPMessage != null && 
					incomingCDAPMessage.getOpCode().equals(Opcode.M_RELEASE)){
				end = true;
				return;
			}
			
			switch (state){
			case WAITING_CONNECTION:
				outgoingCDAPMessage = processWaitingConnectionState(incomingCDAPMessage);
				break;
			case WAITING_READ_ADDRESS:
				outgoingCDAPMessage = processWaitingReadAddressState(incomingCDAPMessage);
				break;
			case INITIALIZING_DATA:
				outgoingCDAPMessage = processWaitingInitializingDataState(incomingCDAPMessage);
				break;
			case WAITING_FOR_STARTUP:
				outgoingCDAPMessage = processWaitingForStartupState(incomingCDAPMessage);
				break;
			case ENROLLED:
				break;
			default:
				log.info("Received a response message, it is not for me");
				end = true;
				return;
			}
			
			if (outgoingCDAPMessage != null){
				sendCDAPMessage(outgoingCDAPMessage);
			}
		} catch (CDAPException ex) {
			ex.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			end = true;
		}
	}
	
	private CDAPMessage processWaitingConnectionState(CDAPMessage cdapMessage) throws CDAPException{
		if (!cdapMessage.getOpCode().equals(Opcode.M_CONNECT_R) || cdapMessage.getResult() != 0){
			end = true;
			return CDAPMessage.getReleaseConnectionRequestMessage(null, 0);
		}
		
		//TODO set timer
		
		state = State.WAITING_READ_ADDRESS;
		return null;
	}
	
	private CDAPMessage processWaitingReadAddressState(CDAPMessage cdapMessage) throws CDAPException, IOException{
		//TODO cancel timer
		
		if (!cdapMessage.getOpCode().equals(Opcode.M_READ) || !cdapMessage.getObjName().equals("daf.management.currentSynonym")){
			end = true;
			return CDAPMessage.getReleaseConnectionRequestMessage(null, 0);
		}
		
		CDAPMessage outgoingCDAPMessage = CDAPMessage.getReadObjectResponseMessage(null, cdapMessage.getInvokeID(), 
				"rina.messages.ApplicationProcessNameSynonym", 0, "daf.management.currentSynonym", null, 0, null);
		
		sendCDAPMessage(outgoingCDAPMessage);
		
		outgoingCDAPMessage = CDAPMessage.getReadObjectRequestMessage(null, null, 49, 
				"rina.messages.DIFEnrollmentInformation", 0, "daf.management.enrollment", 0);
		
		//TODO set timer
		
		state = State.INITIALIZING_DATA;
		
		return outgoingCDAPMessage;
	}
	
	private CDAPMessage processWaitingInitializingDataState(CDAPMessage cdapMessage) throws CDAPException{
		//TODO cancel timer
		if (!cdapMessage.getOpCode().equals(Opcode.M_READ_R) || cdapMessage.getResult() != 0){
			end = true;
			return CDAPMessage.getReleaseConnectionRequestMessage(null, 0);
		}
		
		if(cdapMessage.getFlags() != null && cdapMessage.getFlags().equals(Flags.F_RD_INCOMPLETE)){
			//TODO set timer, more read responses coming
		}else{
			//TODO set timer, an START request should be coming
			state = State.WAITING_FOR_STARTUP;
		}
		
		return null;
	}
	
	private CDAPMessage processWaitingForStartupState(CDAPMessage cdapMessage) throws CDAPException{
		//TODO canncel timer
		if (!cdapMessage.getOpCode().equals(Opcode.M_START)){
			end = true;
			return CDAPMessage.getReleaseConnectionRequestMessage(null, 0);
		}
		
		CDAPMessage outgoingCDAPMessage = CDAPMessage.getStartObjectResponseMessage(null, cdapMessage.getInvokeID(), 0, null);
		state = State.ENROLLED;
		
		return outgoingCDAPMessage;
	}
	
	private CDAPMessage cdapMessageReceived(byte[] serializedCDAPMessage) throws CDAPException{
		log.info("Processing serialized CDAP message. This is the serialized message: ");
		log.info(printBytes(serializedCDAPMessage));
		CDAPMessage incomingCDAPMessage = cdapSession.messageReceived(serializedCDAPMessage);
		log.info("Received CDAP message: "+incomingCDAPMessage.toString());
		
		return incomingCDAPMessage;
	}
	
	public static void main(String[] args){
		CDAPSessionFactoryImpl cdapSessionFactory = new CDAPSessionFactoryImpl();
		WireMessageProviderFactory wmpFactory = new GoogleProtocolBufWireMessageProviderFactory();
		cdapSessionFactory.setWireMessageProviderFactory(wmpFactory);
		DelimiterFactory delimiterFactory = new DelimiterFactoryImpl();
		SerializationFactory serializationFactory = new GPBSerializationFactory();
		CDAPEnrollmentClient cdapEchoClient = new CDAPEnrollmentClient(cdapSessionFactory, delimiterFactory, serializationFactory, DEFAULTHOST, DEFAULTPORT);
		cdapEchoClient.run();
	}
}
