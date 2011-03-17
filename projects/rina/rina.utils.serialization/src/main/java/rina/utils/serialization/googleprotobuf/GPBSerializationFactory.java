package rina.utils.serialization.googleprotobuf;

import rina.flowallocator.api.message.Flow;
import rina.serialization.api.SerializationFactory;
import rina.serialization.api.Serializer;
import rina.utils.serialization.SerializerImpl;
import rina.utils.serialization.googleprotobuf.flow.FlowSerializer;

/**
 * Creates instances of serializers that serializes/unserializes objects using the Google protocol buffers 
 * encoding/decoding format
 * @author eduardgrasa
 *
 */
public class GPBSerializationFactory implements SerializationFactory{

	public Serializer createSerializerInstance() {
		SerializerImpl serializer = new SerializerImpl();
		serializer.setSerializer(Flow.class.toString(), new FlowSerializer());
		return serializer;
	}

}
