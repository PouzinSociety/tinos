package rina.utils.serialization.googleprotobuf.apnamesynonim;

import rina.applicationprocess.api.ApplicationProcessNameSynonym;
import rina.ipcprocess.api.IPCProcess;
import rina.serialization.api.Serializer;
import rina.utils.serialization.googleprotobuf.GPBUtils;

public class ApplicationProcessNameSynonymSerializer implements Serializer{
	
	public void setIPCProcess(IPCProcess ipcProcess) {
	}

	public Object deserialize(byte[] serializedObject, String objectClass) throws Exception {
		if (objectClass == null || !(objectClass.equals(ApplicationProcessNameSynonym.class.toString()))){
			throw new Exception("This is not the serializer for objects of type "+objectClass);
		}
		
		ApplicationProcessNameSynonymMessage.applicationProcessNameSynonym_t gpbApSynonym = 
				ApplicationProcessNameSynonymMessage.applicationProcessNameSynonym_t.parseFrom(serializedObject);
		
		ApplicationProcessNameSynonym apSynonym = new ApplicationProcessNameSynonym();
		apSynonym.setApplicationProcessName(gpbApSynonym.getApplicationProcessName());
		apSynonym.setSynonym(GPBUtils.getByteArray(gpbApSynonym.getSynonymValue()));
		
		return apSynonym;
	}
	
	public byte[] serialize(Object object) throws Exception {
		if (object == null || !(object instanceof ApplicationProcessNameSynonym)){
			throw new Exception("This is not the serializer for objects of type " + ApplicationProcessNameSynonym.class.toString());
		}
		
		ApplicationProcessNameSynonym apSynonym = (ApplicationProcessNameSynonym) object;
		
		ApplicationProcessNameSynonymMessage.applicationProcessNameSynonym_t gpbApSynonym = 
			ApplicationProcessNameSynonymMessage.applicationProcessNameSynonym_t.newBuilder().
										setApplicationProcessName(apSynonym.getApplicationProcessName()).
										setSynonymValue(GPBUtils.getByteString(apSynonym.getSynonym())).
										build();
		
		return gpbApSynonym.toByteArray();
	}
}
