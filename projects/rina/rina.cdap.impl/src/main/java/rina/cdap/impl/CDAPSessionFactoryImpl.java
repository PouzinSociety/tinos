package rina.cdap.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import rina.cdap.api.CDAPException;
import rina.cdap.api.CDAPSession;
import rina.cdap.api.CDAPSessionDescriptor;
import rina.cdap.api.CDAPSessionFactory;
import rina.cdap.api.message.CDAPMessage;

public class CDAPSessionFactoryImpl implements CDAPSessionFactory{
	
	/**
	 * Injected by Spring
	 */
	private WireMessageProviderFactory wireMessageProviderFactory = null;
	
	private Map<String, CDAPSession> cdapSessions = null;
	
	/**
	 * Used by the serialize and unserialize operations
	 */
	private WireMessageProvider wireMessageProvider =  null;
	
	public CDAPSessionFactoryImpl(){
		cdapSessions = new HashMap<String, CDAPSession>();
	}

	public synchronized CDAPSession createCDAPSession() {
		CDAPSessionImpl cdapSession = new CDAPSessionImpl();
		cdapSession.setWireMessageProvider(wireMessageProviderFactory.createWireMessageProvider());
		CDAPSessionDescriptor descriptor = new CDAPSessionDescriptor();
		descriptor.setSessionID(UUID.randomUUID().toString());
		cdapSession.setSessionDescriptor(descriptor);
		cdapSessions.put(descriptor.getSessionID(), cdapSession);
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
		Iterator<Entry<String, CDAPSession>> iterator =  cdapSessions.entrySet().iterator();
		List<CDAPSession> result = new ArrayList<CDAPSession>();
		while(iterator.hasNext()){
			result.add(iterator.next().getValue());
		}
		
		return result;
	}

	public CDAPSession getCDAPSession(String sessionID) {
		return cdapSessions.get(sessionID);
	}

	public synchronized void removeCDAPSession(String sessionID) throws CDAPException {
		CDAPSession cdapSession = cdapSessions.get(sessionID);
		if (cdapSession == null){
			throw new CDAPException("There is no CDAP session associated to this ID");
		}
		
		if (((CDAPSessionImpl)cdapSession).isConnected()){
			throw new CDAPException("Cannot remove this CDAP session because it is in connected state");
		}

		cdapSessions.remove(sessionID);
	}
	
	/**
	 * Serializes a CDAP message. It just converts a CDAP message into a byte 
	 * array, without caring about what session this CDAP message belongs to (and 
	 * therefore it doesn't update any CDAP session state machine). Called by 
	 * functions that have to relay CDAP messages, and need to serialize/
	 * deserialize its contents to make the relay decision and maybe modify some 
	 * message values.
	 * @param cdapMessage
	 * @return
	 * @throws CDAPException
	 */
	public synchronized byte[] serializeCDAPMessage(CDAPMessage cdapMessage) throws CDAPException{
		return getWireMessageProvider().serializeMessage(cdapMessage);
	}
	
	/**
	 * Deserializes a CDAP message. It just converts the byte array into a CDAP 
	 * message, without caring about what session this CDAP message belongs to (and 
	 * therefore it doesn't update any CDAP session state machine). Called by 
	 * functions that have to relay CDAP messages, and need to serialize/
	 * deserialize its contents to make the relay decision and maybe modify some 
	 * @param cdapMessage
	 * @return
	 * @throws CDAPException
	 */
	public synchronized CDAPMessage deserializeCDAPMessage(byte[] cdapMessage) throws CDAPException{
		return getWireMessageProvider().deserializeMessage(cdapMessage);
	}
}
