package rina.ribdaemon.impl.rib;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rina.ribdaemon.api.RIBDaemonException;
import rina.ribdaemon.api.RIBObject;
import rina.ribdaemon.api.RIBObjectNames;

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
    
    public List<RIBObject> getRIBObjects(){
    	List<RIBObject> result = new ArrayList<RIBObject>();
    	Iterator<String> iterator = rib.keySet().iterator();
    	
    	while (iterator.hasNext()){
    		String objectName = iterator.next();
    		int index = getInsertionIndex(result, objectName);
    		result.add(index, rib.get(objectName));
    	}
    	
    	return result;
    }
    
    private int getInsertionIndex(List<RIBObject> result, String objectname){
    	int index = 0;
    	int matches = 0;
    	int currentMatches = 0;
    	String candidate = null;
    	
    	for(int i=0; i<result.size(); i++){
    		candidate = result.get(i).getObjectName();
    		matches = getMatches(candidate, objectname);
    		
    		if (matches == objectname.split(RIBObjectNames.SEPARATOR).length -1){
    			return i;
    		}
    		
    		else if (matches > currentMatches){
    			currentMatches = matches;
    			index = i;
    		}
    	}
    	
    	return index;
    }
    
    private int getMatches(String candidate, String objectName){
    	String[] splittedCandidate = candidate.split(RIBObjectNames.SEPARATOR);
    	String[] splittedObjectName = objectName.split(RIBObjectNames.SEPARATOR);
    	
    	int iterations = Math.min(splittedCandidate.length, splittedObjectName.length);
    	
    	int matches = 0;
    	for(int i=0; i<iterations; i++){
    		if (splittedCandidate[i].equals(splittedObjectName[i])){
    			matches++;
    		}else{
    			return matches;
    		}
    	}
    	
    	return matches;
    }
    
}
