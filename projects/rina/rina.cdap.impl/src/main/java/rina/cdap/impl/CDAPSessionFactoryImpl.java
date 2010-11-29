package rina.cdap.impl;

import rina.cdap.api.CDAPSession;
import rina.cdap.api.CDAPSessionFactory;

public class CDAPSessionFactoryImpl implements CDAPSessionFactory{

	@Override
	public CDAPSession createCDAPSession() {
		CDAPSession cdapSession = new CDAPSessionImpl();
		return cdapSession;
	}
	
}
