package rina.utils.serialization.googleprotobuf.whatevercast;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.ByteString;

import rina.applicationprocess.api.WhatevercastName;
import rina.ipcprocess.api.IPCProcess;
import rina.serialization.api.Serializer;
import rina.utils.serialization.googleprotobuf.GPBUtils;

public class WhatevercastNameSerializer implements Serializer{
	
	public void setIPCProcess(IPCProcess ipcProcess) {
	}
	
	public Object deserialize(byte[] serializedObject, String objectClass) throws Exception {
		if (objectClass == null || !(objectClass.equals(WhatevercastName.class.toString()))){
			throw new Exception("This is not the serializer for objects of type "+objectClass);
		}
		
		WhatevercastNameMessage.whatevercastName_t gpbWhatevercastName = 
				WhatevercastNameMessage.whatevercastName_t.parseFrom(serializedObject);
		
		List<byte[]> setMembers = new ArrayList<byte[]>();
		for(int i=0; i<gpbWhatevercastName.getSetMembersList().size(); i++){
			setMembers.add(GPBUtils.getByteArray(gpbWhatevercastName.getSetMembersList().get(i)));
		}
		
		WhatevercastName whatevercastName = new WhatevercastName();
		whatevercastName.setName(gpbWhatevercastName.getName());
		whatevercastName.setRule(gpbWhatevercastName.getRule());
		whatevercastName.setSetMembers(setMembers);
		
		return whatevercastName;
	}
	
	public byte[] serialize(Object object) throws Exception {
		if (object == null || !(object instanceof WhatevercastName)){
			throw new Exception("This is not the serializer for objects of type " + WhatevercastName.class.toString());
		}
		
		WhatevercastName whatevercastName = (WhatevercastName) object;
		
		List<ByteString> gpbSetMembers = new ArrayList<ByteString>();
		for(int i=0; i<whatevercastName.getSetMembers().size(); i++){
			gpbSetMembers.add(GPBUtils.getByteString(whatevercastName.getSetMembers().get(i)));
		}
		
		WhatevercastNameMessage.whatevercastName_t gpbWhatevercastName = WhatevercastNameMessage.whatevercastName_t.newBuilder().
													setName(whatevercastName.getName()).
													setRule(whatevercastName.getRule()).
													addAllSetMembers(gpbSetMembers).
													build();
		
		return gpbWhatevercastName.toByteArray();
	}

}
