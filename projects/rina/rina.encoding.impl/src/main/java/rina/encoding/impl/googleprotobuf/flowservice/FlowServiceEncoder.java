package rina.encoding.impl.googleprotobuf.flowservice;

import rina.encoding.api.BaseEncoder;
import rina.encoding.impl.googleprotobuf.GPBUtils;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.FlowService;

public class FlowServiceEncoder extends BaseEncoder{

	public Object decode(byte[] serializedObject, String objectClass) throws Exception {
		if (objectClass == null || !(objectClass.equals(FlowService.class.toString()))){
			throw new Exception("This is not the serializer for objects of type "+objectClass);
		}
		
		FlowServiceMessage.FlowService gpbFlowService = FlowServiceMessage.FlowService.parseFrom(serializedObject);
		
		ApplicationProcessNamingInfo destinationAPName = GPBUtils.getApplicationProcessNamingInfo(gpbFlowService.getDestinationNamingInfo());
		ApplicationProcessNamingInfo sourceAPName = GPBUtils.getApplicationProcessNamingInfo(gpbFlowService.getSourceNamingInfo());
		
		FlowService result = new FlowService();
		result.setSourceAPNamingInfo(sourceAPName);
		result.setDestinationAPNamingInfo(destinationAPName);
		result.setPortId((int)gpbFlowService.getPortId());
		
		return result;
	}

	public byte[] encode(Object object) throws Exception {
		if (object == null || !(object instanceof FlowService)){
			throw new Exception("This is not the serializer for objects of type "+FlowService.class.toString());
		}

		FlowService flowService = (FlowService) object;
		FlowServiceMessage.FlowService gpbFlowService = null;

		if (flowService.getSourceAPNamingInfo() == null){
			gpbFlowService = FlowServiceMessage.FlowService.newBuilder().
			setDestinationNamingInfo(GPBUtils.getApplicationProcessNamingInfoT(flowService.getDestinationAPNamingInfo())).
			setPortId(flowService.getPortId()).build();
		}else{
			gpbFlowService = FlowServiceMessage.FlowService.newBuilder().
			setDestinationNamingInfo(GPBUtils.getApplicationProcessNamingInfoT(flowService.getDestinationAPNamingInfo())).
			setSourceNamingInfo(GPBUtils.getApplicationProcessNamingInfoT(flowService.getSourceAPNamingInfo())).
			setPortId(flowService.getPortId()).build();
		}

		return gpbFlowService.toByteArray();
	}

}
