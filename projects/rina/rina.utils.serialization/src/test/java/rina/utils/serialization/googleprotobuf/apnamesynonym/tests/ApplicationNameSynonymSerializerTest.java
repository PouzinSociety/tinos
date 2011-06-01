package rina.utils.serialization.googleprotobuf.apnamesynonym.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import rina.applicationprocess.api.ApplicationProcessNameSynonym;
import rina.utils.serialization.googleprotobuf.apnamesynonim.ApplicationProcessNameSynonymSerializer;


/**
 * Test if the serialization/deserialization mechanisms for the WhatevercastName object work
 * @author eduardgrasa
 *
 */
public class ApplicationNameSynonymSerializerTest {
	
	private ApplicationProcessNameSynonym apnSynonym = null;
	private ApplicationProcessNameSynonymSerializer apNameSerializer = null;
	
	@Before
	public void setup(){
		apnSynonym = new ApplicationProcessNameSynonym();
		apnSynonym.setApplicationProcessName("B");
		apnSynonym.setSynonym(new byte[]{0x02});
		apNameSerializer = new ApplicationProcessNameSynonymSerializer();
	}
	
	@Test
	public void testSerilalization() throws Exception{
		byte[] serializedApns = apNameSerializer.serialize(apnSynonym);
		for(int i=0; i<serializedApns.length; i++){
			System.out.print(serializedApns[i] + " ");
		}
		
		ApplicationProcessNameSynonym recoveredApns = (ApplicationProcessNameSynonym) apNameSerializer.deserialize(serializedApns, ApplicationProcessNameSynonym.class.toString());
		Assert.assertEquals(apnSynonym.getApplicationProcessName(), recoveredApns.getApplicationProcessName());
		Assert.assertArrayEquals(apnSynonym.getSynonym(), recoveredApns.getSynonym());
	}
}