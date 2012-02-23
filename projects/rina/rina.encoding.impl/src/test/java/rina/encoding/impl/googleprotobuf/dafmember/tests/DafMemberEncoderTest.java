package rina.encoding.impl.googleprotobuf.dafmember.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import rina.applicationprocess.api.DAFMember;
import rina.encoding.impl.googleprotobuf.dafmember.DafMemberEncoder;


/**
 * Test if the serialization/deserialization mechanisms for the WhatevercastName object work
 * @author eduardgrasa
 *
 */
public class DafMemberEncoderTest {
	
	private DAFMember dafMember = null;
	private DafMemberEncoder dafMemberEncoder = null;
	
	@Before
	public void setup(){
		dafMember = new DAFMember();
		dafMember.setApplicationProcessName("B");
		dafMember.setApplicationProcessInstance("1234");
		dafMember.setSynonym(12);
		dafMemberEncoder = new DafMemberEncoder();
	}
	
	@Test
	public void testSerilalization() throws Exception{
		byte[] serializedApns = dafMemberEncoder.encode(dafMember);
		for(int i=0; i<serializedApns.length; i++){
			System.out.print(serializedApns[i] + " ");
		}
		
		DAFMember recoveredDm = (DAFMember) dafMemberEncoder.decode(serializedApns, DAFMember.class);
		Assert.assertEquals(dafMember.getApplicationProcessName(), recoveredDm.getApplicationProcessName());
		Assert.assertEquals(dafMember.getApplicationProcessInstance(), recoveredDm.getApplicationProcessInstance());
		Assert.assertEquals(dafMember.getSynonym(), recoveredDm.getSynonym());
	}
}