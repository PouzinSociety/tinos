package rina.encoding.impl.googleprotobuf.dafmember;

import rina.applicationprocess.api.DAFMember;
import rina.efcp.api.DataTransferConstants;
import rina.encoding.api.BaseEncoder;

public class DafMemberEncoder extends BaseEncoder{

	public Object decode(byte[] encodedObject, String objectClass) throws Exception {
		if (objectClass == null || !(objectClass.equals(DAFMember.class.toString()))){
			throw new Exception("This is not the encoder for objects of type "+objectClass);
		}
		
		DafMemberMessage.dafMember_t gpbDafMember = DafMemberMessage.dafMember_t.parseFrom(encodedObject);
		DAFMember dafMember = new DAFMember();
		dafMember.setApplicationProcessInstance(gpbDafMember.getApplicationProcessInstance());
		dafMember.setApplicationProcessName(gpbDafMember.getApplicationProcessName());
		dafMember.setSynonym(gpbDafMember.getSynonym());

		return dafMember;
	}

	public byte[] encode(Object object) throws Exception {
		if (object == null || !(object instanceof DAFMember)){
			throw new Exception("This is not the encoder for objects of type " + DataTransferConstants.class.toString());
		}
		
		DAFMember dafMember = (DAFMember) object;
		DafMemberMessage.dafMember_t gpbDafMember = DafMemberMessage.dafMember_t.newBuilder().
						setApplicationProcessName(dafMember.getApplicationProcessName()).
						setApplicationProcessInstance(dafMember.getApplicationProcessInstance()).
						setSynonym(dafMember.getSynonym()).
						build();
		
		return gpbDafMember.toByteArray();
	}

}
