package rina.encoding.impl.googleprotobuf.dafmember;

import java.util.List;

import rina.applicationprocess.api.DAFMember;
import rina.encoding.api.BaseEncoder;
import rina.encoding.impl.googleprotobuf.dafmember.DafMemberArrayMessage.dafMembers_t.Builder;
import rina.encoding.impl.googleprotobuf.dafmember.DafMemberMessage.dafMember_t;

public class DafMemberArrayEncoder extends BaseEncoder{

	public Object decode(byte[] encodedObject, Class<?> objectClass) throws Exception{
		if (objectClass == null || !(objectClass.equals(DAFMember[].class))){
			throw new Exception("This is not the encoder for objects of type "+objectClass.getName());
		}
		
		List<dafMember_t> gpbDafMemberSet = DafMemberArrayMessage.dafMembers_t.parseFrom(encodedObject).getMemberList();
		DAFMember[] result = new DAFMember[gpbDafMemberSet.size()];
		for(int i=0; i<gpbDafMemberSet.size(); i++){
			result[i] = DafMemberEncoder.convertGPBToModel(gpbDafMemberSet.get(i));
		} 
		
		
		return result;
	}

	public byte[] encode(Object object) throws Exception {
		if (object == null || !(object instanceof DAFMember[])){
			throw new Exception("This is not the encoder for objects of type " + DAFMember[].class.toString());
		}
		
		DAFMember[] dafMembers = (DAFMember[]) object;
		
		Builder builder = DafMemberArrayMessage.dafMembers_t.newBuilder();
		for(int i=0; i<dafMembers.length; i++){
			builder.addMember(DafMemberEncoder.convertModelToGPB(dafMembers[i]));
		}
		
		return builder.build().toByteArray();
	}

}
