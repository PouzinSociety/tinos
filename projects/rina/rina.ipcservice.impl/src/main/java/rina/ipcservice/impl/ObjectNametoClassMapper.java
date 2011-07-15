package rina.ipcservice.impl;

import java.util.List;

import rina.applicationprocess.api.ApplicationProcessNameSynonym;
import rina.ipcservice.api.ApplicationProcessNamingInfo;
import rina.ribdaemon.api.RIBObjectNames;

/**
 * Maps object names to their corresponding Java object classes
 * @author eduardgrasa
 *
 */
public class ObjectNametoClassMapper {
	
	public static String getObjectClass(String objectName){
		if (objectName.equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.APNAME)){
			return ApplicationProcessNamingInfo.class.getName();
		}
		
		if (objectName.equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.CURRENT_SYNONYM)){
			return ApplicationProcessNameSynonym.class.getName();
		}
		
		if (objectName.equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
					RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.SYNONYMS)){
			return List.class.getName();
		}
		
		if (objectName.equals(RIBObjectNames.DAF + RIBObjectNames.SEPARATOR + RIBObjectNames.MANAGEMENT + 
				RIBObjectNames.SEPARATOR + RIBObjectNames.NAMING + RIBObjectNames.SEPARATOR + RIBObjectNames.WHATEVERCAST_NAMES)){
			return List.class.getName();
		}
		
		return null;
	}

}
