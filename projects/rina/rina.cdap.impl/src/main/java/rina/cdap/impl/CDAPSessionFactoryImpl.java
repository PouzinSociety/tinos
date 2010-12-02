package rina.cdap.impl;

import rina.cdap.api.CDAPSession;
import rina.cdap.api.CDAPSessionFactory;

public class CDAPSessionFactoryImpl implements CDAPSessionFactory{
	
	/**
	 * Injected by Spring
	 */
	private WireMessageProviderFactory wireMessageProviderFactory = null;

	public CDAPSession createCDAPSession() {
		CDAPSessionImpl cdapSession = new CDAPSessionImpl();
		cdapSession.setWireMessageProvider(wireMessageProviderFactory.createWireMessageProvider());
		return cdapSession;
	}
	
	public void setWireMessageProviderFactory(WireMessageProviderFactory wireMessageProviderFactory){
		this.wireMessageProviderFactory = wireMessageProviderFactory;
	}
	
}
