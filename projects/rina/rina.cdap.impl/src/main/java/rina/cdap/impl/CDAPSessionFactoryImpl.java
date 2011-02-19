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

public class CDAPSessionFactoryImpl implements CDAPSessionFactory{
	
	/**
	 * Injected by Spring
	 */
	private WireMessageProviderFactory wireMessageProviderFactory = null;
	
	private Map<String, CDAPSession> cdapSessions = null;
	
	public CDAPSessionFactoryImpl(){
		cdapSessions = new HashMap<String, CDAPSession>();
	}

	public CDAPSession createCDAPSession() {
		CDAPSessionImpl cdapSession = new CDAPSessionImpl();
		cdapSession.setWireMessageProvider(wireMessageProviderFactory.createWireMessageProvider());
		CDAPSessionDescriptor descriptor = new CDAPSessionDescriptor();
		descriptor.setSessionID(UUID.randomUUID().toString());
		return cdapSession;
	}
	
	public void setWireMessageProviderFactory(WireMessageProviderFactory wireMessageProviderFactory){
		this.wireMessageProviderFactory = wireMessageProviderFactory;
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

	public void removeCDAPSession(String sessionID) throws CDAPException {
		CDAPSession cdapSession = cdapSessions.get(sessionID);
		if (cdapSession == null){
			throw new CDAPException("There is no CDAP session associated to this ID");
		}
		
		if (((CDAPSessionImpl)cdapSession).isConnected()){
			throw new CDAPException("Cannot remove this CDAP session because it is in connected state");
		}

		cdapSessions.remove(sessionID);
	}
}
