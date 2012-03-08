package rina.encoding.impl.googleprotobuf.dafmember.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import rina.applicationprocess.api.DAFMember;
import rina.encoding.impl.googleprotobuf.dafmember.DafMemberArrayEncoder;
import rina.encoding.impl.googleprotobuf.dafmember.DafMemberEncoder;


/**
 * Test if the serialization/deserialization mechanisms for the WhatevercastName object work
 * @author eduardgrasa
 *
 */
public class DafMemberEncoderTest {
	
	private DAFMember dafMember = null;
	private DAFMember dafMember2 = null;
	private DAFMember[] dafMembers = null;
	private DafMemberEncoder dafMemberEncoder = null;
	private DafMemberArrayEncoder dafMemberArrayEncoder = null;
	
	@Before
	public void setup(){
		dafMember = new DAFMember();
		dafMember.setApplicationProcessName("B");
		dafMember.setApplicationProcessInstance("1234");
		dafMember.setSynonym(12);
		
		dafMember2 = new DAFMember();
		dafMember2.setApplicationProcessName("C");
		dafMember2.setApplicationProcessInstance("5678");
		dafMember2.setSynonym(242);
		
		dafMembers = new DAFMember[]{dafMember, dafMember2};
		
		dafMemberEncoder = new DafMemberEncoder();
		dafMemberArrayEncoder = new DafMemberArrayEncoder();
	}
	
	@Test
	public void testSingle() throws Exception{
		byte[] serializedApns = dafMemberEncoder.encode(dafMember);
		for(int i=0; i<serializedApns.length; i++){
			System.out.print(serializedApns[i] + " ");
		}
		System.out.println();
		
		DAFMember recoveredDm = (DAFMember) dafMemberEncoder.decode(serializedApns, DAFMember.class);
		Assert.assertEquals(dafMember.getApplicationProcessName(), recoveredDm.getApplicationProcessName());
		Assert.assertEquals(dafMember.getApplicationProcessInstance(), recoveredDm.getApplicationProcessInstance());
		Assert.assertEquals(dafMember.getSynonym(), recoveredDm.getSynonym());
	}
	
	@Test
	public void testArray() throws Exception{
		byte[] encodedArray = dafMemberArrayEncoder.encode(dafMembers);
		for(int i=0; i<encodedArray.length; i++){
			System.out.print(encodedArray[i] + " ");
		}
		System.out.println();
		
		DAFMember[] recoveredDms = (DAFMember[]) dafMemberArrayEncoder.decode(encodedArray, DAFMember[].class);
		Assert.assertEquals(dafMembers[0].getApplicationProcessName(), recoveredDms[0].getApplicationProcessName());
		Assert.assertEquals(dafMembers[0].getApplicationProcessInstance(), recoveredDms[0].getApplicationProcessInstance());
		Assert.assertEquals(dafMembers[0].getSynonym(), recoveredDms[0].getSynonym());
		
		Assert.assertEquals(dafMembers[1].getApplicationProcessName(), recoveredDms[1].getApplicationProcessName());
		Assert.assertEquals(dafMembers[1].getApplicationProcessInstance(), recoveredDms[1].getApplicationProcessInstance());
		Assert.assertEquals(dafMembers[1].getSynonym(), recoveredDms[1].getSynonym());
	}	
}