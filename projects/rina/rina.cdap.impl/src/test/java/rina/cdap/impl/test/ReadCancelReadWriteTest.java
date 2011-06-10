package rina.cdap.impl.test;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import rina.cdap.api.CDAPException;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.ObjectValue;

public class ReadCancelReadWriteTest extends BaseCDAPTest{
	
	@Before
	public void setup(){
		super.setup();
		try{
			connect();
		}catch(CDAPException ex){
			ex.printStackTrace();
		}
	}
	
	@Test
	public void testSingleWriteWithResponse() throws CDAPException{
		CDAPMessage cdapMessage = null;
		byte[] message = null;
		
		cdapMessage = CDAPMessage.getWriteObjectRequestMessage(null, null, 25, "org.pouzinsociety.flow.Flow", 0, new ObjectValue(), "123", 0);
		message = sendingCDAPSession.encodeNextMessageToBeSent(cdapMessage);
		sendingCDAPSession.messageSent(cdapMessage);
		
		receivingCDAPSession.messageReceived(message);
		
		boolean failed = false;
		cdapMessage = CDAPMessage.getWriteObjectResponseMessage(null, 24, 0, null);
		try{
			message = receivingCDAPSession.encodeNextMessageToBeSent(cdapMessage);
		}catch(CDAPException ex){
			System.out.println(ex.getMessage());
			failed = true;
		}

		Assert.assertTrue(failed);
		
		cdapMessage = CDAPMessage.getWriteObjectResponseMessage(null, 25, 0, null);
		message = receivingCDAPSession.encodeNextMessageToBeSent(cdapMessage);
		receivingCDAPSession.messageSent(cdapMessage);
		
		sendingCDAPSession.messageReceived(message);
	}
	
	@Test
	public void testSingleWriteWithoutResponse() throws CDAPException{
		CDAPMessage cdapMessage = null;
		byte[] message = null;

		cdapMessage = CDAPMessage.getWriteObjectRequestMessage(null, null, 0, "org.pouzinsociety.flow.Flow", 0, new ObjectValue(), "123", 0);
		message = sendingCDAPSession.encodeNextMessageToBeSent(cdapMessage);
		sendingCDAPSession.messageSent(cdapMessage);

		receivingCDAPSession.messageReceived(message);

		boolean failed = false;
		cdapMessage = CDAPMessage.getWriteObjectResponseMessage(null, 25, 0, null);
		try{
			message = receivingCDAPSession.encodeNextMessageToBeSent(cdapMessage);
		}catch(CDAPException ex){
			System.out.println(ex.getMessage());
			failed = true;
		}

		Assert.assertTrue(failed);
	}
	
	@Test
	public void testMultipleWriteWithResponses() throws CDAPException{
		CDAPMessage cdapMessage = null;
		byte[] message = null;

		cdapMessage = CDAPMessage.getWriteObjectRequestMessage(null, null, 2, "org.pouzinsociety.flow.Flow", 0, new ObjectValue(), "123", 0);
		message = sendingCDAPSession.encodeNextMessageToBeSent(cdapMessage);
		sendingCDAPSession.messageSent(cdapMessage);
		
		receivingCDAPSession.messageReceived(message);
		
		cdapMessage = CDAPMessage.getWriteObjectRequestMessage(null, null, 3, "org.pouzinsociety.flow.Flow", 0, new ObjectValue(), "789", 0);
		message = sendingCDAPSession.encodeNextMessageToBeSent(cdapMessage);
		sendingCDAPSession.messageSent(cdapMessage);
		
		receivingCDAPSession.messageReceived(message);
		
		cdapMessage = CDAPMessage.getWriteObjectResponseMessage(null, 2, 0, null);
		message = receivingCDAPSession.encodeNextMessageToBeSent(cdapMessage);
		receivingCDAPSession.messageSent(cdapMessage);
		
		sendingCDAPSession.messageReceived(message);
		
		cdapMessage = CDAPMessage.getWriteObjectResponseMessage(null, 3, 0, null);
		message = receivingCDAPSession.encodeNextMessageToBeSent(cdapMessage);
		receivingCDAPSession.messageSent(cdapMessage);
		
		sendingCDAPSession.messageReceived(message);
	}
	
	@Test
	public void testWriteDisconnected() throws CDAPException{
		this.disconnectWithoutResponse();
		CDAPMessage cdapMessage = null;

		boolean failed = false;
		cdapMessage = CDAPMessage.getWriteObjectRequestMessage(null, null, 25, "org.pouzinsociety.flow.Flow", 0, new ObjectValue(), "123", 0);
		try{
			sendingCDAPSession.encodeNextMessageToBeSent(cdapMessage);
		}catch(CDAPException ex){
			System.out.println(ex.getMessage());
			failed = true;
		}

		Assert.assertTrue(failed);
	}

}
