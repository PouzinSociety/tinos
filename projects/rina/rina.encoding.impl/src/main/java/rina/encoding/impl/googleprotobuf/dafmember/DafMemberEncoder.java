package rina.encoding.impl.googleprotobuf.dafmember;

import rina.applicationprocess.api.DAFMember;
import rina.encoding.api.BaseEncoder;
import rina.encoding.impl.googleprotobuf.dafmember.DafMemberMessage.dafMember_t;

public class DafMemberEncoder extends BaseEncoder{

	public Object decode(byte[] encodedObject, Class<?> objectClass) throws Exception {
		if (objectClass == null || !(objectClass.equals(DAFMember.class))){
			throw new Exception("This is not the encoder for objects of type "+objectClass.getName());
		}
		
		dafMember_t gpbDafMember = DafMemberMessage.dafMember_t.parseFrom(encodedObject);
		return convertGPBToModel(gpbDafMember);
	}
	
	public static DAFMember convertGPBToModel(dafMember_t gpbDafMember){
		DAFMember dafMember = new DAFMember();
		dafMember.setApplicationProcessInstance(gpbDafMember.getApplicationProcessInstance());
		dafMember.setApplicationProcessName(gpbDafMember.getApplicationProcessName());
		dafMember.setSynonym(gpbDafMember.getSynonym());

		return dafMember;
	}

	public byte[] encode(Object object) throws Exception {
		if (object == null || !(object instanceof DAFMember)){
			throw new Exception("This is not the encoder for objects of type " + DAFMember.class.toString());
		}
		
		return convertModelToGPB((DAFMember)object).toByteArray();
	}
	
	public static dafMember_t convertModelToGPB(DAFMember dafMember){
		dafMember_t gpbDafMember = DafMemberMessage.dafMember_t.newBuilder().
			setApplicationProcessName(dafMember.getApplicationProcessName()).
			setApplicationProcessInstance(dafMember.getApplicationProcessInstance()).
			setSynonym(dafMember.getSynonym()).
			build();
		
		return gpbDafMember;
	}

}
