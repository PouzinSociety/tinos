package rina.encoding.impl.googleprotobuf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import rina.encoding.impl.googleprotobuf.apnaminginfo.ApplicationProcessNamingInfoMessage;
import rina.encoding.impl.googleprotobuf.apnaminginfo.ApplicationProcessNamingInfoMessage.applicationProcessNamingInfo_t;
import rina.encoding.impl.googleprotobuf.qosspecification.QoSSpecification;
import rina.encoding.impl.googleprotobuf.qosspecification.QoSSpecification.qosParameter_t;
import rina.encoding.impl.googleprotobuf.qosspecification.QoSSpecification.qosSpecification_t;
import rina.applicationprocess.api.ApplicationProcessNamingInfo;
import rina.ipcservice.api.QualityOfServiceSpecification;

import com.google.protobuf.ByteString;

/**
 * Utility classes to work with Google Protocol Buffers
 * @author eduardgrasa
 *
 */
public class GPBUtils {
	
	public static ApplicationProcessNamingInfo getApplicationProcessNamingInfo(applicationProcessNamingInfo_t apNamingInfo) {
		String apName = GPBUtils.getString(apNamingInfo.getApplicationProcessName());
		String apInstance = GPBUtils.getString(apNamingInfo.getApplicationProcessInstance());
		String aeName = GPBUtils.getString(apNamingInfo.getApplicationEntityName());
		String aeInstance = GPBUtils.getString(apNamingInfo.getApplicationEntityInstance());
		
		ApplicationProcessNamingInfo result = new ApplicationProcessNamingInfo(apName, apInstance);
		result.setApplicationEntityName(aeName);
		result.setApplicationEntityInstance(aeInstance);
		return result;
	}
	
	public static applicationProcessNamingInfo_t getApplicationProcessNamingInfoT(ApplicationProcessNamingInfo apNamingInfo){
		if (apNamingInfo != null){
			String apName = GPBUtils.getGPBString(apNamingInfo.getApplicationProcessName());
			String apInstance = GPBUtils.getGPBString(apNamingInfo.getApplicationProcessInstance());
			String aeName = GPBUtils.getGPBString(apNamingInfo.getApplicationEntityName());
			String aeInstance = GPBUtils.getGPBString(apNamingInfo.getApplicationEntityInstance());
			return ApplicationProcessNamingInfoMessage.applicationProcessNamingInfo_t.newBuilder().
			setApplicationProcessName(apName).
			setApplicationProcessInstance(apInstance).
			setApplicationEntityName(aeName).
			setApplicationEntityInstance(aeInstance).
			build();
		}else{
			return null;
		}
	}
	
	public static QualityOfServiceSpecification getQualityOfServiceSpecification(qosSpecification_t qosSpec_t){
		if (qosSpec_t == null){
			return null;
		}
		
		QualityOfServiceSpecification result = new QualityOfServiceSpecification();
		result.setAverageBandwidth(qosSpec_t.getAverageBandwidth());
		result.setAverageSDUBandwidth(qosSpec_t.getAverageSDUBandwidth());
		result.setDelay(qosSpec_t.getDelay());
		result.setJitter(qosSpec_t.getJitter());
		result.setMaxAllowableGapSDU(qosSpec_t.getMaxAllowableGapSdu());
		result.setOrder(qosSpec_t.getOrder());
		result.setPartialDelivery(qosSpec_t.getPartialDelivery());
		result.setPeakBandwidthDuration(qosSpec_t.getPeakBandwidthDuration());
		result.setPeakSDUBandwidthDuration(qosSpec_t.getPeakSDUBandwidthDuration());
		result.setUndetectedBitErrorRate(qosSpec_t.getUndetectedBitErrorRate());
		
		for(int i=0; i<qosSpec_t.getExtraParametersCount(); i++){
			result.getExtendedPrameters().put(
					qosSpec_t.getExtraParametersList().get(i).getParameterName(), qosSpec_t.getExtraParametersList().get(i).getParameterValue());
		}
		
		return result;
	}
	
	public static qosSpecification_t getQoSSpecificationT(QualityOfServiceSpecification qualityOfServiceSpecification){
		if (qualityOfServiceSpecification != null){
			
			List<qosParameter_t> extraParameters = getQosSpecExtraParametersType(qualityOfServiceSpecification);
			
			return QoSSpecification.qosSpecification_t.newBuilder().
				setAverageBandwidth(qualityOfServiceSpecification.getAverageBandwidth()).
				setAverageSDUBandwidth(qualityOfServiceSpecification.getAverageSDUBandwidth()).
				setDelay(qualityOfServiceSpecification.getDelay()).
				addAllExtraParameters(extraParameters).
				setJitter(qualityOfServiceSpecification.getJitter()).
				setMaxAllowableGapSdu(qualityOfServiceSpecification.getMaxAllowableGapSDU()).
				setOrder(qualityOfServiceSpecification.isOrder()).
				setPartialDelivery(qualityOfServiceSpecification.isPartialDelivery()).
				setPeakBandwidthDuration(qualityOfServiceSpecification.getPeakBandwidthDuration()).
				setPeakSDUBandwidthDuration(qualityOfServiceSpecification.getPeakSDUBandwidthDuration()).
				setUndetectedBitErrorRate(qualityOfServiceSpecification.getUndetectedBitErrorRate()).
				build();
		}else{
			return null;
		}
	}
	
	private static List<qosParameter_t> getQosSpecExtraParametersType(QualityOfServiceSpecification qualityOfServiceSpecification){
		List<qosParameter_t> qosParametersList = new ArrayList<qosParameter_t>();
		
		if (qualityOfServiceSpecification.getExtendedPrameters().isEmpty()){
			return qosParametersList;
		}
		
		Iterator<Entry<String, String>> iterator = qualityOfServiceSpecification.getExtendedPrameters().entrySet().iterator();
		Entry<String, String> entry = null;
		while(iterator.hasNext()){
			entry = iterator.next();
			qosParametersList.add(getQoSParameterT(entry));
		}
		
		return qosParametersList;
	}
	
	private static qosParameter_t getQoSParameterT(Entry<String, String> entry){
		return QoSSpecification.qosParameter_t.newBuilder().
			setParameterName(entry.getKey()).
			setParameterValue(entry.getValue()).
			build();
	}
	
	public static byte[] getByteArray(ByteString byteString){
		byte[] result = null;
		if (!byteString.equals(ByteString.EMPTY)){
			result = byteString.toByteArray();
		}
		
		return result;
	}
	
	public static String getString(String string){
		String result = null;
		if (!string.equals("")){
			result = string;
		}
		
		return result;
	}
	
	public static ByteString getByteString(byte[] byteArray){
		ByteString result = ByteString.EMPTY;
		if (byteArray != null){
			result = ByteString.copyFrom(byteArray);
		}
		
		return result;
	}
	
	public static String getGPBString(String string){
		String result = string;
		if (result == null){
			result = "";
		}
		
		return result;
	}

}
