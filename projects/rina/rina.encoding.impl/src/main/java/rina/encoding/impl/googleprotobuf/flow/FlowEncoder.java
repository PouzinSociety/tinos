package rina.encoding.impl.googleprotobuf.flow;

import java.util.ArrayList;
import java.util.List;

import rina.encoding.api.BaseEncoder;
import rina.flowallocator.api.ConnectionId;
import rina.flowallocator.api.Flow;
import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.QualityOfServiceSpecification;
import rina.encoding.impl.googleprotobuf.GPBUtils;
import rina.encoding.impl.googleprotobuf.apnaminginfo.ApplicationProcessNamingInfoMessage;
import rina.encoding.impl.googleprotobuf.apnaminginfo.ApplicationProcessNamingInfoMessage.applicationProcessNamingInfo_t;
import rina.encoding.impl.googleprotobuf.flow.FlowMessage.connectionId_t;
import rina.encoding.impl.googleprotobuf.qosspecification.QoSSpecification.qosSpecification_t;

/**
 * Serializes, unserializes Flow objects using the GPB encoding
 * @author eduardgrasa
 *
 */
public class FlowEncoder extends BaseEncoder{

	public Object decode(byte[] serializedObject, Class<?> objectClass) throws Exception {
		if (objectClass == null || !(objectClass.equals(Flow.class))){
			throw new Exception("This is not the serializer for objects of type "+objectClass.getName());
		}
		
		FlowMessage.Flow gpbFlow = FlowMessage.Flow.parseFrom(serializedObject);
		
		byte[] accessControl = GPBUtils.getByteArray(gpbFlow.getAccessControl());
		ApplicationProcessNamingInfo destinationAPName = getApplicationProcessNamingInfo(gpbFlow.getDestinationNamingInfo());
		List<ConnectionId> flowIds = getConnectionIds(gpbFlow.getConnectionIdsList());
		QualityOfServiceSpecification qosParameters = GPBUtils.getQualityOfServiceSpecification(gpbFlow.getQosParameters());
		ApplicationProcessNamingInfo sourceAPName = getApplicationProcessNamingInfo(gpbFlow.getSourceNamingInfo());
		byte[] status = GPBUtils.getByteArray(gpbFlow.getState());
		
		Flow flow = new Flow();
		flow.setAccessControl(accessControl);
		flow.setCreateFlowRetries(gpbFlow.getCreateFlowRetries());
		flow.setCurrentFlowId(gpbFlow.getCurrentConnectionId());
		flow.setDestinationAddress(gpbFlow.getDestinationAddress());
		flow.setDestinationNamingInfo(destinationAPName);
		flow.setDestinationPortId(gpbFlow.getDestinationPortId());
		flow.setFlowIds(flowIds);
		flow.setHopCount(gpbFlow.getHopCount());
		flow.setMaxCreateFlowRetries(gpbFlow.getMaxCreateFlowRetries());
		flow.setPolicies(gpbFlow.getPoliciesList());
		flow.setQosParameters(qosParameters);
		flow.setSourceAddress(gpbFlow.getSourceAddress());
		flow.setSourceNamingInfo(sourceAPName);
		flow.setSourcePortId(gpbFlow.getSourcePortId());
		if (status != null){
			flow.setStatus(status[0]);
		}
		
		return flow;
	}

	private ApplicationProcessNamingInfo getApplicationProcessNamingInfo(applicationProcessNamingInfo_t apNamingInfo) {
		String apName = GPBUtils.getString(apNamingInfo.getApplicationProcessName());
		String apInstance = GPBUtils.getString(apNamingInfo.getApplicationProcessInstance());
		
		ApplicationProcessNamingInfo result = new ApplicationProcessNamingInfo(apName, apInstance);
		return result;
	}

	private List<ConnectionId> getConnectionIds(List<connectionId_t> connectionIdsList) {
		List<ConnectionId> result = new ArrayList<ConnectionId>();
		
		for (int i=0; i<connectionIdsList.size(); i++){
			result.add(getConnectionId(connectionIdsList.get(i)));
		}
		
		return result;
	}
	
	private ConnectionId getConnectionId(connectionId_t connectionId){
		ConnectionId result = new ConnectionId();
		
		result.setDestinationCEPId(connectionId.getDestinationCEPId());
		result.setQosId(connectionId.getQosId());
		result.setSourceCEPId(connectionId.getSourceCEPId());
		return result;
	}
	 
	
	public byte[] encode(Object object) throws Exception {
		if (object == null || !(object instanceof Flow)){
			throw new Exception("This is not the serializer for objects of type "+Flow.class.toString());
		}
		
		Flow flow = (Flow) object;
		
		List<connectionId_t> connectionIds = getConnectionIdTypes(flow.getFlowIds());
		qosSpecification_t qosSpecificationT = GPBUtils.getQoSSpecificationT(flow.getQosParameters());
		List<String> flowPolicies = flow.getPolicies();
		if (flowPolicies == null){
			flowPolicies = new ArrayList<String>();
		}
		
		FlowMessage.Flow gpbFlow = null;
		
		if (qosSpecificationT != null){
			gpbFlow = FlowMessage.Flow.newBuilder().
			setAccessControl(GPBUtils.getByteString(flow.getAccessControl())).
			setCreateFlowRetries(flow.getCreateFlowRetries()).
			setCurrentConnectionId(flow.getCurrentFlowId()).
			addAllConnectionIds(connectionIds).
			setDestinationAddress(flow.getDestinationAddress()).
			setDestinationNamingInfo(getApplicationProcessNamingInfoT(flow.getDestinationNamingInfo())).
			setDestinationPortId(flow.getDestinationPortId()).
			setHopCount(flow.getHopCount()).
			addAllPolicies(flowPolicies).
			setQosParameters(qosSpecificationT).
			setMaxCreateFlowRetries(flow.getMaxCreateFlowRetries()).
			setSourceAddress(flow.getSourceAddress()).
			setSourceNamingInfo(getApplicationProcessNamingInfoT(flow.getSourceNamingInfo())).
			setSourcePortId(flow.getSourcePortId()).
			setState(GPBUtils.getByteString(new byte[]{flow.getStatus()})).
			build();
		}else{
			gpbFlow = FlowMessage.Flow.newBuilder().
			setAccessControl(GPBUtils.getByteString(flow.getAccessControl())).
			setCreateFlowRetries(flow.getCreateFlowRetries()).
			setCurrentConnectionId(flow.getCurrentFlowId()).
			addAllConnectionIds(connectionIds).
			setDestinationAddress(flow.getDestinationAddress()).
			setDestinationNamingInfo(getApplicationProcessNamingInfoT(flow.getDestinationNamingInfo())).
			setDestinationPortId(flow.getDestinationPortId()).
			setHopCount(flow.getHopCount()).
			addAllPolicies(flowPolicies).
			setMaxCreateFlowRetries(flow.getMaxCreateFlowRetries()).
			setSourceAddress(flow.getSourceAddress()).
			setSourceNamingInfo(getApplicationProcessNamingInfoT(flow.getSourceNamingInfo())).
			setSourcePortId(flow.getSourcePortId()).
			setState(GPBUtils.getByteString(new byte[]{flow.getStatus()})).
			build();
		}

		return gpbFlow.toByteArray();
	}
	
	private applicationProcessNamingInfo_t getApplicationProcessNamingInfoT(ApplicationProcessNamingInfo apNamingInfo){
		applicationProcessNamingInfo_t result = null;
		if (apNamingInfo != null){
			String apName = GPBUtils.getGPBString(apNamingInfo.getApplicationProcessName());
			String apInstance = GPBUtils.getGPBString(apNamingInfo.getApplicationProcessInstance());
			result = ApplicationProcessNamingInfoMessage.applicationProcessNamingInfo_t.newBuilder().
			setApplicationProcessName(apName).
			setApplicationProcessInstance(apInstance).
			build();
		}
		return result;
	}
	
	private List<connectionId_t> getConnectionIdTypes(List<ConnectionId> connectionIds){
		List<connectionId_t> result = new ArrayList<connectionId_t>();
		
		if (connectionIds == null){
			return result;
		}
		
		for (int i=0; i<connectionIds.size(); i++){
			result.add(getConnectionIdType(connectionIds.get(i)));
		}
		
		return result;
	}
	
	private connectionId_t getConnectionIdType(ConnectionId connectionId){
		connectionId_t result = FlowMessage.connectionId_t.newBuilder().
									setDestinationCEPId(connectionId.getDestinationCEPId()).
									setQosId(connectionId.getQosId()).
									setSourceCEPId(connectionId.getSourceCEPId()).
									build();
		
		return result;
	}
}
