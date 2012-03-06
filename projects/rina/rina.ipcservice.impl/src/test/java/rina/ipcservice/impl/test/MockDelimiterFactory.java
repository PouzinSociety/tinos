package rina.ipcservice.impl.test;

import junit.framework.Assert;
import rina.delimiting.api.Delimiter;
import rina.delimiting.api.DelimiterFactory;

public class MockDelimiterFactory implements DelimiterFactory{

	private Delimiter mockDelimiter = null;
	
	public Delimiter createDelimiter(String delimiterType) {
		Assert.assertEquals(DelimiterFactory.DIF, delimiterType);
		mockDelimiter = new MockDelimiter();
		return mockDelimiter;
	}

}
