package rina.cdap.impl.test;

import org.junit.Before;

import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSessionFactory;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.AuthTypes;
import rina.cdap.impl.CDAPSessionFactoryImpl;
import rina.cdap.impl.CDAPSessionImpl;
import rina.cdap.impl.googleprotobuf.GoogleProtocolBufWireMessageProviderFactory;

public abstract class BaseCDAPTest {
	
	protected CDAPSessionImpl sendingCDAPSession = null;
	protected CDAPSessionImpl receivingCDAPSession = null;
	protected CDAPSessionFactory cdapSessionFactory = new CDAPSessionFactoryImpl();
	
	@Before
	public void setup(){
		((CDAPSessionFactoryImpl)cdapSessionFactory).setWireMessageProviderFactory(new GoogleProtocolBufWireMessageProviderFactory());
		sendingCDAPSession = (CDAPSessionImpl) cdapSessionFactory.createCDAPSession();
		receivingCDAPSession = (CDAPSessionImpl) cdapSessionFactory.createCDAPSession();
	}
	
	protected void connect() throws CDAPException{
		byte[] message = null;
		CDAPMessage cdapMessage = null;
		
		cdapMessage = CDAPMessage.getOpenConnectionRequestMessage(AuthTypes.AUTH_NONE, null, null, "mock", null, "B", 23, "234", "mock", "123", "A", 1);
		message = sendingCDAPSession.serializeNextMessageToBeSent(cdapMessage);
		sendingCDAPSession.messageSent(cdapMessage);
		
		cdapMessage = receivingCDAPSession.messageReceived(message);
		cdapMessage = CDAPMessage.getOpenConnectionResponseMessage(AuthTypes.AUTH_NONE, null, "234", "mock", "123", "A", 23, 0, null, "899", "mock", "677", "B", 1);
		message = receivingCDAPSession.serializeNextMessageToBeSent(cdapMessage);
		receivingCDAPSession.messageSent(cdapMessage);
		
		cdapMessage = sendingCDAPSession.messageReceived(message);
	}
	
	protected void disconnectWithResponse() throws CDAPException{
		byte[] message = null;
		CDAPMessage cdapMessage = null;
		
		cdapMessage = CDAPMessage.getReleaseConnectionRequestMessage(null, 1);
		message = sendingCDAPSession.serializeNextMessageToBeSent(cdapMessage);
		sendingCDAPSession.messageSent(cdapMessage);
		
		cdapMessage = receivingCDAPSession.messageReceived(message);
		cdapMessage = CDAPMessage.getReleaseConnectionResponseMessage(null, 1, 0, null);
		message = receivingCDAPSession.serializeNextMessageToBeSent(cdapMessage);
		receivingCDAPSession.messageSent(cdapMessage);
		
		cdapMessage = sendingCDAPSession.messageReceived(message);
	}
	
	protected void disconnectWithoutResponse() throws CDAPException{
		byte[] message = null;
		CDAPMessage cdapMessage = null;
		
		cdapMessage = CDAPMessage.getReleaseConnectionRequestMessage(null, 0);
		message = sendingCDAPSession.serializeNextMessageToBeSent(cdapMessage);
		sendingCDAPSession.messageSent(cdapMessage);
		
		cdapMessage = receivingCDAPSession.messageReceived(message);
	}

}
