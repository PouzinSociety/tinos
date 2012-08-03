package rina.resourceallocator.impl;

import java.util.HashMap;
import java.util.Map;

import rina.resourceallocator.api.ResourceAllocator;
import rina.resourceallocator.api.ResourceAllocatorFactory;
import rina.applicationprocess.api.ApplicationProcessNamingInfo;

public class ResourceAllocatorFactoryImpl implements ResourceAllocatorFactory{

	private Map<String, ResourceAllocator> resourceAllocatorRespository = null;
	
	public ResourceAllocatorFactoryImpl(){
		resourceAllocatorRespository = new HashMap<String, ResourceAllocator>();
	}
	
	public ResourceAllocator createResourceAllocator(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		ResourceAllocatorImpl resourceAllocator = null;
		try {
			resourceAllocator = new ResourceAllocatorImpl();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		synchronized(resourceAllocatorRespository){
			resourceAllocatorRespository.put(ipcProcessNamingInfo.getEncodedString(), resourceAllocator);
		}
		return resourceAllocator;
	}

	public void destroyResourceAllocator(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		synchronized(resourceAllocatorRespository){
			resourceAllocatorRespository.remove(ipcProcessNamingInfo.getEncodedString());
		}
	}

	public ResourceAllocator getResourceAllocator(ApplicationProcessNamingInfo ipcProcessNamingInfo) {
		synchronized(resourceAllocatorRespository){
			return resourceAllocatorRespository.get(ipcProcessNamingInfo.getEncodedString());
		}
	}

}
