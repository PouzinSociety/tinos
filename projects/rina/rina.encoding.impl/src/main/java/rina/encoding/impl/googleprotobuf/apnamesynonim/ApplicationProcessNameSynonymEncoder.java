package rina.encoding.impl.googleprotobuf.apnamesynonim;

import rina.applicationprocess.api.ApplicationProcessNameSynonym;
import rina.encoding.api.BaseEncoder;
import rina.encoding.impl.googleprotobuf.GPBUtils;

public class ApplicationProcessNameSynonymEncoder extends BaseEncoder{

	public Object decode(byte[] serializedObject, String objectClass) throws Exception {
		if (objectClass == null || !(objectClass.equals(ApplicationProcessNameSynonym.class.toString()))){
			throw new Exception("This is not the encoder for objects of type "+objectClass);
		}
		
		ApplicationProcessNameSynonymMessage.applicationProcessNameSynonym_t gpbApSynonym = 
				ApplicationProcessNameSynonymMessage.applicationProcessNameSynonym_t.parseFrom(serializedObject);
		
		ApplicationProcessNameSynonym apSynonym = new ApplicationProcessNameSynonym();
		apSynonym.setApplicationProcessName(gpbApSynonym.getApplicationProcessName());
		apSynonym.setApplicationProcessInstance(gpbApSynonym.getApplicationProcessInstance());
		apSynonym.setSynonym(GPBUtils.getByteArray(gpbApSynonym.getSynonymValue()));
		
		return apSynonym;
	}
	
	public byte[] encode(Object object) throws Exception {
		if (object == null || !(object instanceof ApplicationProcessNameSynonym)){
			throw new Exception("This is not the encoder for objects of type " + ApplicationProcessNameSynonym.class.toString());
		}
		
		ApplicationProcessNameSynonym apSynonym = (ApplicationProcessNameSynonym) object;
		
		ApplicationProcessNameSynonymMessage.applicationProcessNameSynonym_t gpbApSynonym = 
			ApplicationProcessNameSynonymMessage.applicationProcessNameSynonym_t.newBuilder().
										setApplicationProcessName(apSynonym.getApplicationProcessName()).
										setApplicationProcessInstance(apSynonym.getApplicationProcessInstance()).
										setSynonymValue(GPBUtils.getByteString(apSynonym.getSynonym())).
										build();
		
		return gpbApSynonym.toByteArray();
	}
}
