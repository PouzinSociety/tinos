package rina.encoding.impl.googleprotobuf.allocaterequest;

import rina.encoding.api.BaseEncoder;
import rina.encoding.impl.googleprotobuf.GPBUtils;
import rina.encoding.impl.googleprotobuf.apnaminginfo.ApplicationProcessNamingInfoMessage;
import rina.encoding.impl.googleprotobuf.apnaminginfo.ApplicationProcessNamingInfoMessage.applicationProcessNamingInfo_t;
import rina.ipcservice.api.AllocateRequest;
import rina.ipcservice.api.ApplicationProcessNamingInfo;

public class AllocateRequestEncoder extends BaseEncoder{

	public Object decode(byte[] serializedObject, String objectClass) throws Exception {
		if (objectClass == null || !(objectClass.equals(AllocateRequest.class.toString()))){
			throw new Exception("This is not the serializer for objects of type "+objectClass);
		}
		
		AllocateRequestMessage.AllocateRequest gpbAllocateRequest = AllocateRequestMessage.AllocateRequest.parseFrom(serializedObject);
		
		ApplicationProcessNamingInfo destinationAPName = getApplicationProcessNamingInfo(gpbAllocateRequest.getDestinationNamingInfo());
		ApplicationProcessNamingInfo sourceAPName = getApplicationProcessNamingInfo(gpbAllocateRequest.getSourceNamingInfo());
		
		AllocateRequest result = new AllocateRequest();
		result.setSourceAPNamingInfo(sourceAPName);
		result.setDestinationAPNamingInfo(destinationAPName);
		result.setPortId((int)gpbAllocateRequest.getPortId());
		
		return result;
	}
	
	private ApplicationProcessNamingInfo getApplicationProcessNamingInfo(applicationProcessNamingInfo_t apNamingInfo) {
		String apName = GPBUtils.getString(apNamingInfo.getApplicationProcessName());
		String apInstance = GPBUtils.getString(apNamingInfo.getApplicationProcessInstance());
		
		ApplicationProcessNamingInfo result = new ApplicationProcessNamingInfo(apName, apInstance);
		return result;
	}

	public byte[] encode(Object object) throws Exception {
		if (object == null || !(object instanceof AllocateRequest)){
			throw new Exception("This is not the serializer for objects of type "+AllocateRequest.class.toString());
		}

		AllocateRequest allocateRequest = (AllocateRequest) object;
		AllocateRequestMessage.AllocateRequest gpbAllocateRequest = null;

		if (allocateRequest.getSourceAPNamingInfo() == null){
			gpbAllocateRequest = AllocateRequestMessage.AllocateRequest.newBuilder().
			setDestinationNamingInfo(getApplicationProcessNamingInfoT(allocateRequest.getDestinationAPNamingInfo())).
			setPortId(allocateRequest.getPortId()).build();
		}else{
			gpbAllocateRequest = AllocateRequestMessage.AllocateRequest.newBuilder().
			setDestinationNamingInfo(getApplicationProcessNamingInfoT(allocateRequest.getDestinationAPNamingInfo())).
			setSourceNamingInfo(getApplicationProcessNamingInfoT(allocateRequest.getSourceAPNamingInfo())).
			setPortId(allocateRequest.getPortId()).build();
		}

		return gpbAllocateRequest.toByteArray();
	}
	
	private applicationProcessNamingInfo_t getApplicationProcessNamingInfoT(ApplicationProcessNamingInfo apNamingInfo){
		if (apNamingInfo != null){
			String apName = GPBUtils.getGPBString(apNamingInfo.getApplicationProcessName());
			String apInstance = GPBUtils.getGPBString(apNamingInfo.getApplicationProcessInstance());
			return ApplicationProcessNamingInfoMessage.applicationProcessNamingInfo_t.newBuilder().
			setApplicationProcessName(apName).
			setApplicationProcessInstance(apInstance).
			build();
		}else{
			return null;
		}
	}

}
