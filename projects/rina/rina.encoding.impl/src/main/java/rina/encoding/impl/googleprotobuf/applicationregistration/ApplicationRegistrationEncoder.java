package rina.encoding.impl.googleprotobuf.applicationregistration;

import java.util.ArrayList;
import java.util.List;

import rina.encoding.api.BaseEncoder;
import rina.encoding.impl.googleprotobuf.GPBUtils;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.ApplicationRegistration;

public class ApplicationRegistrationEncoder extends BaseEncoder{
	
	public Object decode(byte[] serializedObject, String objectClass) throws Exception {
		if (objectClass == null || !(objectClass.equals(ApplicationRegistration.class.toString()))){
			throw new Exception("This is not the serializer for objects of type "+objectClass);
		}
		
		ApplicationRegistrationMessage.ApplicationRegistration gpbApplicationRegistration = ApplicationRegistrationMessage.ApplicationRegistration.parseFrom(serializedObject);
		
		ApplicationProcessNamingInfo apName = GPBUtils.getApplicationProcessNamingInfo(gpbApplicationRegistration.getNamingInfo());
		
		ApplicationRegistration result = new ApplicationRegistration();
		result.setApNamingInfo(apName);
		result.setSocketNumber(gpbApplicationRegistration.getSocketNumber());
		result.setDifNames(gpbApplicationRegistration.getDifNamesList());
		
		return result;
	}
	
	public byte[] encode(Object object) throws Exception {
		if (object == null || !(object instanceof ApplicationRegistration)){
			throw new Exception("This is not the serializer for objects of type "+ApplicationRegistration.class.toString());
		}

		ApplicationRegistration applicationRegistration = (ApplicationRegistration) object;
		List<String> difNames = applicationRegistration.getDifNames();
		if (difNames == null){
			difNames = new ArrayList<String>();
		}
		
		ApplicationRegistrationMessage.ApplicationRegistration gpbApplicationRegistration = 
			ApplicationRegistrationMessage.ApplicationRegistration.newBuilder().
						setNamingInfo(GPBUtils.getApplicationProcessNamingInfoT(applicationRegistration.getApNamingInfo())).
						setSocketNumber(applicationRegistration.getSocketNumber()).
						addAllDifNames(difNames).
						build();

		return gpbApplicationRegistration.toByteArray();
	}
}