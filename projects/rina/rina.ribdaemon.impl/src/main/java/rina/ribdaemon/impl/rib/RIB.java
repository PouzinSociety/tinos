package rina.ribdaemon.impl.rib;

import java.util.Hashtable;
import java.util.Map;

import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;

/**
 * Stores the RIB information
 */
public class RIB{
    private Map<String, RIBObject> rib = null;
     
    /**
     * Default ctor.
     */
    public RIB(){
    	rib = new Hashtable<String, RIBObject>();
    }
    
    /**
     * Given an objectname of the form "substring\0substring\0...substring" locate 
     * the RIBObject that corresponds to it
     * @param objectName
     * @return
     */
    public RIBObject getRIBObject(String objectName) throws RIBDaemonException{
    	return rib.get(objectName);
    }
    
    public void addRIBObject(RIBObject ribObject){
    	rib.put(ribObject.getObjectName(), ribObject);
    }
    
    public void removeRIBObject(String objectName){
    	rib.remove(objectName);
    }
    
}