package rina.ribdaemon.impl;

import java.util.HashMap;
import java.util.Map;

/**
 * A super simple store that stores objects in memory
 * @author eduardgrasa
 *
 */
public class InMemoryStore {
	
	/**
	 * Stores the object in the RIB by object instance id (a long)
	 */
	private Map<Long, Object> objectInstanceObjectStore = null;
	
	/**
	 * Stores the objects in the RIB by objectClass and objectName
	 */
	private Map<String, Map<String, Object>> objectClassObjectStore = null;
	
	public InMemoryStore(){
		objectInstanceObjectStore = new HashMap<Long, Object>();
		objectClassObjectStore = new HashMap<String, Map<String, Object>>();
	}

}
