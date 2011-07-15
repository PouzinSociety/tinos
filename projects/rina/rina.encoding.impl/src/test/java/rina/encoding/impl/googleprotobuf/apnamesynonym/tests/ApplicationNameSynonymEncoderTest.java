package rina.encoding.impl.googleprotobuf.apnamesynonym.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import rina.applicationprocess.api.ApplicationProcessNameSynonym;
import rina.encoding.impl.googleprotobuf.apnamesynonim.ApplicationProcessNameSynonymEncoder;


/**
 * Test if the serialization/deserialization mechanisms for the WhatevercastName object work
 * @author eduardgrasa
 *
 */
public class ApplicationNameSynonymEncoderTest {
	
	private ApplicationProcessNameSynonym apnSynonym = null;
	private ApplicationProcessNameSynonymEncoder apNameEncoder = null;
	
	@Before
	public void setup(){
		apnSynonym = new ApplicationProcessNameSynonym();
		apnSynonym.setApplicationProcessName("B");
		apnSynonym.setApplicationProcessInstance("1234");
		apnSynonym.setSynonym(new byte[]{0x02});
		apNameEncoder = new ApplicationProcessNameSynonymEncoder();
	}
	
	@Test
	public void testSerilalization() throws Exception{
		byte[] serializedApns = apNameEncoder.encode(apnSynonym);
		for(int i=0; i<serializedApns.length; i++){
			System.out.print(serializedApns[i] + " ");
		}
		
		ApplicationProcessNameSynonym recoveredApns = (ApplicationProcessNameSynonym) apNameEncoder.decode(serializedApns, ApplicationProcessNameSynonym.class.toString());
		Assert.assertEquals(apnSynonym.getApplicationProcessName(), recoveredApns.getApplicationProcessName());
		Assert.assertEquals(apnSynonym.getApplicationProcessInstance(), recoveredApns.getApplicationProcessInstance());
		Assert.assertArrayEquals(apnSynonym.getSynonym(), recoveredApns.getSynonym());
	}
}