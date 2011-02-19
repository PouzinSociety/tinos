package rina.cdap.impl.test;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import rina.cdap.api.CDAPException;
import rina.cdap.api.message.CDAPMessage;

public class StartStopTest extends BaseCDAPTest{
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
	public void testSingleStartWithResponse() throws CDAPException{
		CDAPMessage cdapMessage = null;
		byte[] message = null;
		
		cdapMessage = CDAPMessage.getStartObjectRequestMessage(null, null, 25, "org.pouzinsociety.flow.Flow", null, 0, "123", 0);
		message = sendingCDAPSession.serializeNextMessageToBeSent(cdapMessage);
		sendingCDAPSession.messageSent(cdapMessage);
		
		receivingCDAPSession.messageReceived(message);
		
		boolean failed = false;
		cdapMessage = CDAPMessage.getStartObjectResponseMessage(null, 24, 0, null);
		try{
			message = receivingCDAPSession.serializeNextMessageToBeSent(cdapMessage);
		}catch(CDAPException ex){
			System.out.println(ex.getMessage());
			failed = true;
		}

		Assert.assertTrue(failed);
		
		cdapMessage = CDAPMessage.getStartObjectResponseMessage(null, 25, 0, null);
		message = receivingCDAPSession.serializeNextMessageToBeSent(cdapMessage);
		receivingCDAPSession.messageSent(cdapMessage);
		
		sendingCDAPSession.messageReceived(message);
	}
	
	@Test
	public void testSingleStartWithoutResponse() throws CDAPException{
		CDAPMessage cdapMessage = null;
		byte[] message = null;

		cdapMessage = CDAPMessage.getStartObjectRequestMessage(null, null, 0, "org.pouzinsociety.flow.Flow", null, 0, "123", 0);
		message = sendingCDAPSession.serializeNextMessageToBeSent(cdapMessage);
		sendingCDAPSession.messageSent(cdapMessage);

		receivingCDAPSession.messageReceived(message);

		boolean failed = false;
		cdapMessage = CDAPMessage.getStartObjectResponseMessage(null, 25, 0, null);
		try{
			message = receivingCDAPSession.serializeNextMessageToBeSent(cdapMessage);
		}catch(CDAPException ex){
			System.out.println(ex.getMessage());
			failed = true;
		}

		Assert.assertTrue(failed);
	}
	
	@Test
	public void testMultipleStartWithResponses() throws CDAPException{
		CDAPMessage cdapMessage = null;
		byte[] message = null;

		cdapMessage = CDAPMessage.getStartObjectRequestMessage(null, null, 2, "org.pouzinsociety.flow.Flow", null, 0, "123", 0);
		message = sendingCDAPSession.serializeNextMessageToBeSent(cdapMessage);
		sendingCDAPSession.messageSent(cdapMessage);
		
		receivingCDAPSession.messageReceived(message);
		
		cdapMessage = CDAPMessage.getStartObjectRequestMessage(null, null, 3, "org.pouzinsociety.flow.Flow", null, 0, "789", 0);
		message = sendingCDAPSession.serializeNextMessageToBeSent(cdapMessage);
		sendingCDAPSession.messageSent(cdapMessage);
		
		receivingCDAPSession.messageReceived(message);
		
		cdapMessage = CDAPMessage.getStartObjectResponseMessage(null, 2, 0, null);
		message = receivingCDAPSession.serializeNextMessageToBeSent(cdapMessage);
		receivingCDAPSession.messageSent(cdapMessage);
		
		sendingCDAPSession.messageReceived(message);
		
		cdapMessage = CDAPMessage.getStartObjectResponseMessage(null, 3, 0, null);
		message = receivingCDAPSession.serializeNextMessageToBeSent(cdapMessage);
		receivingCDAPSession.messageSent(cdapMessage);
		
		sendingCDAPSession.messageReceived(message);
	}
	
	@Test
	public void testStartDisconnected() throws CDAPException{
		this.disconnectWithoutResponse();
		CDAPMessage cdapMessage = null;

		boolean failed = false;
		cdapMessage = CDAPMessage.getStartObjectRequestMessage(null, null, 2, "org.pouzinsociety.flow.Flow", null, 0, "123", 0);
		try{
			sendingCDAPSession.serializeNextMessageToBeSent(cdapMessage);
		}catch(CDAPException ex){
			System.out.println(ex.getMessage());
			failed = true;
		}

		Assert.assertTrue(failed);
	}
	
	@Test
	public void testSingleStopWithResponse() throws CDAPException{
		CDAPMessage cdapMessage = null;
		byte[] message = null;
		
		cdapMessage = CDAPMessage.getStopObjectRequestMessage(null, null, 25, "org.pouzinsociety.flow.Flow", null, 0, "123", 0);
		message = sendingCDAPSession.serializeNextMessageToBeSent(cdapMessage);
		sendingCDAPSession.messageSent(cdapMessage);
		
		receivingCDAPSession.messageReceived(message);
		
		boolean failed = false;
		cdapMessage = CDAPMessage.getStopObjectResponseMessage(null, 24, 0, null);
		try{
			message = receivingCDAPSession.serializeNextMessageToBeSent(cdapMessage);
		}catch(CDAPException ex){
			System.out.println(ex.getMessage());
			failed = true;
		}

		Assert.assertTrue(failed);
		
		cdapMessage = CDAPMessage.getStopObjectResponseMessage(null, 25, 0, null);
		message = receivingCDAPSession.serializeNextMessageToBeSent(cdapMessage);
		receivingCDAPSession.messageSent(cdapMessage);
		
		sendingCDAPSession.messageReceived(message);
	}
	
	@Test
	public void testSingleStopWithoutResponse() throws CDAPException{
		CDAPMessage cdapMessage = null;
		byte[] message = null;

		cdapMessage = CDAPMessage.getStopObjectRequestMessage(null, null, 0, "org.pouzinsociety.flow.Flow", null, 0, "123", 0);
		message = sendingCDAPSession.serializeNextMessageToBeSent(cdapMessage);
		sendingCDAPSession.messageSent(cdapMessage);

		receivingCDAPSession.messageReceived(message);

		boolean failed = false;
		cdapMessage = CDAPMessage.getStopObjectResponseMessage(null, 25, 0, null);
		try{
			message = receivingCDAPSession.serializeNextMessageToBeSent(cdapMessage);
		}catch(CDAPException ex){
			System.out.println(ex.getMessage());
			failed = true;
		}

		Assert.assertTrue(failed);
	}
	
	@Test
	public void testMultipleStopWithResponses() throws CDAPException{
		CDAPMessage cdapMessage = null;
		byte[] message = null;

		cdapMessage = CDAPMessage.getStopObjectRequestMessage(null, null, 2, "org.pouzinsociety.flow.Flow", null, 0, "123", 0);
		message = sendingCDAPSession.serializeNextMessageToBeSent(cdapMessage);
		sendingCDAPSession.messageSent(cdapMessage);
		
		receivingCDAPSession.messageReceived(message);
		
		cdapMessage = CDAPMessage.getStopObjectRequestMessage(null, null, 3, "org.pouzinsociety.flow.Flow", null, 0, "789", 0);
		message = sendingCDAPSession.serializeNextMessageToBeSent(cdapMessage);
		sendingCDAPSession.messageSent(cdapMessage);
		
		receivingCDAPSession.messageReceived(message);
		
		cdapMessage = CDAPMessage.getStopObjectResponseMessage(null, 2, 0, null);
		message = receivingCDAPSession.serializeNextMessageToBeSent(cdapMessage);
		receivingCDAPSession.messageSent(cdapMessage);
		
		sendingCDAPSession.messageReceived(message);
		
		cdapMessage = CDAPMessage.getStopObjectResponseMessage(null, 3, 0, null);
		message = receivingCDAPSession.serializeNextMessageToBeSent(cdapMessage);
		receivingCDAPSession.messageSent(cdapMessage);
		
		sendingCDAPSession.messageReceived(message);
	}
	
	@Test
	public void testStopDisconnected() throws CDAPException{
		this.disconnectWithoutResponse();
		CDAPMessage cdapMessage = null;

		boolean failed = false;
		cdapMessage = CDAPMessage.getStopObjectRequestMessage(null, null, 2, "org.pouzinsociety.flow.Flow", null, 0, "123", 0);
		try{
			sendingCDAPSession.serializeNextMessageToBeSent(cdapMessage);
		}catch(CDAPException ex){
			System.out.println(ex.getMessage());
			failed = true;
		}

		Assert.assertTrue(failed);
	}
}
