package rina.utils.types;

import junit.framework.Assert;

import org.junit.Test;

public class UnsignedTest {

	@Test
	public void test4Bytes(){
		Unsigned unsigned = new Unsigned(4);
		unsigned.setValue(new Integer(63210).longValue());
		byte[] encodedValue = unsigned.getBytes();
		
		for(int i=0; i<encodedValue.length; i++){
			System.out.print(String.format("0x%02X", encodedValue[i]) + " ");
		}
		System.out.println("");
		
		Assert.assertEquals(new Integer(63210).intValue(), new Long(new Unsigned(encodedValue).getValue()).intValue());
	}
}
