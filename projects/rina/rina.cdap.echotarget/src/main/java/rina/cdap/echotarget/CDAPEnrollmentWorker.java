package rina.cdap.echotarget;

import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSession;
import rina.cdap.api.message.CDAPMessage;
import rina.delimiting.api.Delimiter;
import rina.serialization.api.Serializer;

public class CDAPEnrollmentWorker extends CDAPWorker {

	private static final Log log = LogFactory.getLog(CDAPEnrollmentWorker.class);
	
	private enum State {NULL, AUTHENTICATE_AND_AUTHORIZE, READ_ADDRESS, INITIALIZE_NEW_MEMBER, INITIALIZE_NEW_MEMBER_SEND_RESPONSE, 
		INITIALIZATION_COMPLETE, WAITING_FOR_STARTUP, ENROLLED, DISCONNECTING};
		
	private State state = State.NULL;

	public CDAPEnrollmentWorker(Socket socket, CDAPSession cdapSession, Delimiter delimiter, Serializer serializer) {
		super(socket, cdapSession, delimiter, serializer);
	}

	@Override
	protected void processCDAPMessage(byte[] serializedCDAPMessage) {
		log.info("Processing serialized CDAP message. This is the serialized message: ");
		log.info(printBytes(serializedCDAPMessage));
		CDAPMessage incomingCDAPMessage = null;

		try {
			incomingCDAPMessage = cdapSession.messageReceived(serializedCDAPMessage);
			log.info("Received CDAP message: "+incomingCDAPMessage.toString());
			
			switch(state){
			case NULL:
				break;
			case AUTHENTICATE_AND_AUTHORIZE:
				break;
			case READ_ADDRESS:
				break;
			case INITIALIZE_NEW_MEMBER:
				break;
			case INITIALIZE_NEW_MEMBER_SEND_RESPONSE:
				break;
			case INITIALIZATION_COMPLETE:
				break;
			case WAITING_FOR_STARTUP:
				break;
			case ENROLLED:
				break;
			case DISCONNECTING:
				break;
			default:
				break;
			}
		}catch(CDAPException ex){
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}

}
