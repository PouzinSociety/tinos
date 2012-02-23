package rina.ribdaemon.api;

import java.util.List;

import rina.applicationprocess.api.DAFMember;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * Maps object names to their corresponding Java object classes
 * @author eduardgrasa
 *
 */
public class ObjectNametoClassMapper {
	
	public static Class<?> getObjectClass(String objectName){
		if (objectName.equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.APNAME)){
			return ApplicationProcessNamingInfo.class;
		}
		
		if (objectName.equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.CURRENT_SYNONYM)){
			return DAFMember.class;
		}
		
		if (objectName.equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.SYNONYMS)){
			return List.class;
		}
		
		if (objectName.equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.WHATEVERCAST_NAMES)){
			return List.class;
		}
		
		return null;
	}

}
