package rina.encoding.impl.googleprotobuf;

import rina.encoding.impl.googleprotobuf.apnaminginfo.ApplicationProcessNamingInfoMessage;
import rina.encoding.impl.googleprotobuf.apnaminginfo.ApplicationProcessNamingInfoMessage.applicationProcessNamingInfo_t;
import rina.ipcservice.api.ApplicationProcessNamingInfo;

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
		
		ApplicationProcessNamingInfo result = new ApplicationProcessNamingInfo(apName, apInstance);
		return result;
	}
	
	public static applicationProcessNamingInfo_t getApplicationProcessNamingInfoT(ApplicationProcessNamingInfo apNamingInfo){
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
