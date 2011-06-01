package rina.serialization.api;

import rina.ipcprocess.api.IPCProcessComponent;

/**
 * Serializes and Unserializes an object to/from bytes)
 * @author eduardgrasa
 *
 */
public interface Serializer extends IPCProcessComponent{
	
	/**
	 * Converts an object to a byte array, if this object is recognized by the serializer
	 * @param object
	 * @throws exception if the object is not recognized by the serializer
	 * @return
	 */
	public byte[] serialize(Object object) throws Exception;
	
	/**
	 * Converts a byte array to an object of the type specified by "className"
	 * @param serializedObject
	 * @param className
	 * @throws exception if the byte array is not an encoded in a way that the serializer can recognize, or the 
	 * byte array value doesn't correspond to an object of the type "className"
	 * @return
	 */
	public Object deserialize(byte[] serializedObject, String className) throws Exception;
}
