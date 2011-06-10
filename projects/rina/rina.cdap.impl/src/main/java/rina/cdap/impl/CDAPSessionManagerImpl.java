package rina.cdap.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSession;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.CDAPSessionManager;
import rina.cdap.api.message.CDAPMessage;
import rina.cdap.api.message.CDAPMessage.Opcode;

public class CDAPSessionManagerImpl implements CDAPSessionManager{
	private WireMessageProviderFactory wireMessageProviderFactory = null;
	
	private Map<Integer, CDAPSession> cdapSessions = null;
	
	/**
	 * Used by the serialize and unserialize operations
	 */
	private WireMessageProvider wireMessageProvider =  null;
	
	public CDAPSessionManagerImpl(){
		cdapSessions = new HashMap<Integer, CDAPSession>();
	}

	public synchronized CDAPSession createCDAPSession(int portId) {
		CDAPSessionImpl cdapSession = new CDAPSessionImpl(this);
		cdapSession.setWireMessageProvider(wireMessageProviderFactory.createWireMessageProvider());
		CDAPSessionDescriptor descriptor = new CDAPSessionDescriptor();
		descriptor.setPortId(portId);
		cdapSession.setSessionDescriptor(descriptor);
		cdapSessions.put(new Integer(descriptor.getPortId()), cdapSession);
		return cdapSession;
	}
	
	public void setWireMessageProviderFactory(WireMessageProviderFactory wireMessageProviderFactory){
		this.wireMessageProviderFactory = wireMessageProviderFactory;
	}
	
	private WireMessageProvider getWireMessageProvider(){
		if (this.wireMessageProvider == null){
			this.wireMessageProvider = this.wireMessageProviderFactory.createWireMessageProvider();
		}
		
		return this.wireMessageProvider;
	}

	public List<CDAPSession> getAllCDAPSessions() {
		Iterator<Entry<Integer, CDAPSession>> iterator =  cdapSessions.entrySet().iterator();
		List<CDAPSession> result = new ArrayList<CDAPSession>();
		while(iterator.hasNext()){
			result.add(iterator.next().getValue());
		}
		
		return result;
	}

	public CDAPSession getCDAPSession(int portId) {
		return cdapSessions.get(new Integer(portId));
	}
	
	/**
	 * Encodes a CDAP message. It just converts a CDAP message into a byte 
	 * array, without caring about what session this CDAP message belongs to (and 
	 * therefore it doesn't update any CDAP session state machine). Called by 
	 * functions that have to relay CDAP messages, and need to encode/
	 * decode its contents to make the relay decision and maybe modify some 
	 * message values.
	 * @param cdapMessage
	 * @return
	 * @throws CDAPException
	 */
	public synchronized byte[] encodeCDAPMessage(CDAPMessage cdapMessage) throws CDAPException{
		return getWireMessageProvider().serializeMessage(cdapMessage);
	}
	
	/**
	 * Decodes a CDAP message. It just converts the byte array into a CDAP 
	 * message, without caring about what session this CDAP message belongs to (and 
	 * therefore it doesn't update any CDAP session state machine). Called by 
	 * functions that have to relay CDAP messages, and need to encode/
	 * decode its contents to make the relay decision and maybe modify some 
	 * @param cdapMessage
	 * @return
	 * @throws CDAPException
	 */
	public synchronized CDAPMessage decodeCDAPMessage(byte[] cdapMessage) throws CDAPException{
		return getWireMessageProvider().deserializeMessage(cdapMessage);
	}
	
	/**
	 * Called by the CDAPSession state machine when the cdap session is terminated
	 * @param portId
	 */
	public synchronized void removeCDAPSession(int portId){
		cdapSessions.remove(new Integer(portId));
	}


	/**
	 * Encodes the next CDAP message to be sent, and checks against the 
	 * CDAP state machine that this is a valid message to be sent
	 * @param cdapMessage The cdap message to be serialized
	 * @param portId 
	 * @return encoded version of the CDAP Message
	 * @throws CDAPException
	 */
	public byte[] encodeNextMessageToBeSent(CDAPMessage cdapMessage, int portId) throws CDAPException {
		CDAPSession cdapSession = this.getCDAPSession(portId);
		if (cdapSession == null && cdapMessage.getOpCode() == Opcode.M_CONNECT){
			cdapSession = this.createCDAPSession(portId);
		}else if (cdapSession == null && cdapMessage.getOpCode() != Opcode.M_CONNECT){
			throw new CDAPException("There are no open CDAP sessions associated to the flow identified by "+portId+" right now");
		}
			
		return cdapSession.encodeNextMessageToBeSent(cdapMessage);
	}

	/**
	 * Depending on the message received, it will create a new CDAP state machine (CDAP Session), or update 
	 * an existing one, or terminate one.
	 * @param encodedCDAPMessage
	 * @param portId
	 * @return Decoded CDAP Message
	 * @throws CDAPException if the message is not consistent with the appropriate CDAP state machine
	 */
	public synchronized CDAPMessage messageReceived(byte[] encodedCDAPMessage, int portId) throws CDAPException {
		CDAPMessage cdapMessage = this.decodeCDAPMessage(encodedCDAPMessage);
		CDAPSession cdapSession = this.getCDAPSession(portId);
		
		switch(cdapMessage.getOpCode()){
		case M_CONNECT:
			if (cdapSession == null){
				cdapSession = this.createCDAPSession(portId);
				cdapSession.messageReceived(cdapMessage);
				this.cdapSessions.put(new Integer(portId), cdapSession);
			}else{
				throw new CDAPException("M_CONNECT received on an already open CDAP Session, over flow " + portId);
			}
			break;
		default:
			if (cdapSession != null){
				cdapSession.messageReceived(cdapMessage);
			}else{
				throw new CDAPException("Receive a "+cdapMessage.getOpCode()+" CDAP message on a CDAP session that is not open, over flow "+portId);
			}
			break;
		}
		
		return cdapMessage;
	}

	/**
	 * Update the CDAP state machine because we've sent a message through the
	 * flow identified by portId
	 * @param cdapMessage The CDAP message to be serialized
	 * @param portId 
	 * @return encoded version of the CDAP Message
	 * @throws CDAPException
	 */
	public void messageSent(CDAPMessage cdapMessage, int portId) throws CDAPException {
		CDAPSession cdapSession = this.getCDAPSession(portId);
		if (cdapSession == null && cdapMessage.getOpCode() == Opcode.M_CONNECT){
			cdapSession = this.createCDAPSession(portId);
		}else if (cdapSession == null && cdapMessage.getOpCode() != Opcode.M_CONNECT){
			throw new CDAPException("There are no open CDAP sessions associated to the flow identified by "+portId+" right now");
		}
			
		cdapSession.messageSent(cdapMessage);
	}
}
