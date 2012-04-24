package rina.ipcservice.impl.test;

import rina.encoding.api.Encoder;
import rina.encoding.api.EncoderFactory;

public class MockEncoderFactory implements EncoderFactory{

	private Encoder mockEncoder = null;
	
	public Encoder createEncoderInstance() {
		mockEncoder = new MockEncoder();
		return mockEncoder;
	}

}
