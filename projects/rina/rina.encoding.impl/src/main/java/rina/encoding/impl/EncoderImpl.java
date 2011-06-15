package rina.encoding.impl;

import java.util.HashMap;
import java.util.Map;

import rina.encoding.api.BaseEncoder;
import rina.encoding.api.Encoder;

/**
 * Implements an encoder that delegates the encoding/decoding
 * tasks to different subencoders. A different encoder is registered 
 * by each type of object
 * @author eduardgrasa
 *
 */
public class EncoderImpl extends BaseEncoder{
	
	private Map<String, Encoder> encoders = null;
	
	public EncoderImpl(){
		encoders = new HashMap<String, Encoder>();
	}
	
	/**
	 * Set the class that serializes/unserializes an object class
	 * @param objectClass The object class
	 * @param serializer
	 */
	public void addEncoder(String objectClass, Encoder encoder){
		encoder.setIPCProcess(getIPCProcess());
		encoders.put(objectClass, encoder);
	}

	public synchronized Object decode(byte[] serializedObject, String objectClass) throws Exception{
		return getEncoder(objectClass).decode(serializedObject, objectClass);
	}

	public synchronized byte[] encode(Object object) throws Exception {
		return getEncoder(object.getClass().toString()).encode(object);
	}
	
	private Encoder getEncoder(String objectClass) throws Exception{
		Encoder encoder = encoders.get(objectClass);
		if (encoder == null){
			throw new Exception("No encoders found for class "+objectClass);
		}
		
		return encoder;
	}

}
