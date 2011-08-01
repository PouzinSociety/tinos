package rina.cdap.impl.test;

import org.junit.Before;

import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.AuthTypes;
import rina.cdap.impl.CDAPSessionManagerImpl;
import rina.cdap.impl.CDAPSessionImpl;
import rina.cdap.impl.googleprotobuf.GoogleProtocolBufWireMessageProviderFactory;

public abstract class BaseCDAPTest {
	
	protected CDAPSessionImpl sendingCDAPSession = null;
	protected CDAPSessionImpl receivingCDAPSession = null;
	protected CDAPSessionManager cdapSessionManager = new CDAPSessionManagerImpl();
	
	@Before
	public void setup(){
		((CDAPSessionManagerImpl)cdapSessionManager).setWireMessageProviderFactory(new GoogleProtocolBufWireMessageProviderFactory());
		sendingCDAPSession = (CDAPSessionImpl) ((CDAPSessionManagerImpl)cdapSessionManager).createCDAPSession(32768);
		receivingCDAPSession = (CDAPSessionImpl) ((CDAPSessionManagerImpl)cdapSessionManager).createCDAPSession(32769);
	}
	
	protected void connect() throws CDAPException{
		byte[] message = null;
		CDAPMessage cdapMessage = null;
		
		cdapMessage = CDAPMessage.getOpenConnectionRequestMessage(AuthTypes.AUTH_NONE, null, null, "mock", null, "B", 23, "234", "mock", "123", "A");
		message = sendingCDAPSession.encodeNextMessageToBeSent(cdapMessage);
		sendingCDAPSession.messageSent(cdapMessage);
		
		cdapMessage = receivingCDAPSession.messageReceived(message);
		cdapMessage = CDAPMessage.getOpenConnectionResponseMessage(AuthTypes.AUTH_NONE, null, "234", "mock", "123", "A", 23, 0, null, "899", "mock", "677", "B");
		message = receivingCDAPSession.encodeNextMessageToBeSent(cdapMessage);
		receivingCDAPSession.messageSent(cdapMessage);
		
		cdapMessage = sendingCDAPSession.messageReceived(message);
	}
	
	protected void disconnectWithResponse() throws CDAPException{
		byte[] message = null;
		CDAPMessage cdapMessage = null;
		
		cdapMessage = CDAPMessage.getReleaseConnectionRequestMessage(null, 1);
		message = sendingCDAPSession.encodeNextMessageToBeSent(cdapMessage);
		sendingCDAPSession.messageSent(cdapMessage);
		
		cdapMessage = receivingCDAPSession.messageReceived(message);
		cdapMessage = CDAPMessage.getReleaseConnectionResponseMessage(null, 1, 0, null);
		message = receivingCDAPSession.encodeNextMessageToBeSent(cdapMessage);
		receivingCDAPSession.messageSent(cdapMessage);
		
		cdapMessage = sendingCDAPSession.messageReceived(message);
	}
	
	protected void disconnectWithoutResponse() throws CDAPException{
		byte[] message = null;
		CDAPMessage cdapMessage = null;
		
		cdapMessage = CDAPMessage.getReleaseConnectionRequestMessage(null, 0);
		message = sendingCDAPSession.encodeNextMessageToBeSent(cdapMessage);
		sendingCDAPSession.messageSent(cdapMessage);
		
		cdapMessage = receivingCDAPSession.messageReceived(message);
	}

}
