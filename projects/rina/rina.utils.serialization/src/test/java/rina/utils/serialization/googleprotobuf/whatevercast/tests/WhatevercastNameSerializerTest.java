package rina.utils.serialization.googleprotobuf.whatevercast.tests;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import rina.applicationprocess.api.WhatevercastName;
import rina.utils.serialization.googleprotobuf.whatevercast.WhatevercastNameSerializer;


/**
 * Test if the serialization/deserialization mechanisms for the WhatevercastName object work
 * @author eduardgrasa
 *
 */
public class WhatevercastNameSerializerTest {
	
	private WhatevercastName whatevercastName = null;
	private WhatevercastNameSerializer whatevercastNameSerializer = null;
	
	@Before
	public void setup(){
		whatevercastName = new WhatevercastName();
		whatevercastName.setName("RINA-Demo-all.DIF");
		whatevercastName.setRule("all members");
		List<byte[]> setMembers = new ArrayList<byte[]>();
		setMembers.add(new byte[]{0x01});
		setMembers.add(new byte[]{0x02});
		whatevercastName.setSetMembers(setMembers);
		whatevercastNameSerializer = new WhatevercastNameSerializer();
	}
	
	@Test
	public void testSerilalization() throws Exception{
		byte[] serializedWN = whatevercastNameSerializer.serialize(whatevercastName);
		for(int i=0; i<serializedWN.length; i++){
			System.out.print(serializedWN[i] + " ");
		}
		
		WhatevercastName recoveredWhatevercastName = (WhatevercastName) whatevercastNameSerializer.deserialize(serializedWN, WhatevercastName.class.toString());
		Assert.assertEquals(whatevercastName.getName(), recoveredWhatevercastName.getName());
		Assert.assertEquals(whatevercastName.getRule(), recoveredWhatevercastName.getRule());
		Assert.assertEquals(whatevercastName.getSetMembers().size(), recoveredWhatevercastName.getSetMembers().size());
		for(int i=0; i<whatevercastName.getSetMembers().size(); i++){
			Assert.assertArrayEquals(whatevercastName.getSetMembers().get(i), recoveredWhatevercastName.getSetMembers().get(i));
		}
	}
}