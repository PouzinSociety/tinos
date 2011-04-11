package rina.utils.serialization.googleprotobuf;

import rina.efcp.api.DataTransferConstants;
import rina.flowallocator.api.QoSCube;
import rina.flowallocator.api.message.Flow;
import rina.serialization.api.SerializationFactory;
import rina.serialization.api.Serializer;
import rina.utils.serialization.SerializerImpl;
import rina.utils.serialization.googleprotobuf.datatransferconstants.DataTransferConstantsSerializer;
import rina.utils.serialization.googleprotobuf.flow.FlowSerializer;
import rina.utils.serialization.googleprotobuf.qoscube.QoSCubeSerializer;

/**
 * Creates instances of serializers that serializes/unserializes objects using the Google protocol buffers 
 * encoding/decoding format
 * @author eduardgrasa
 *
 */
public class GPBSerializationFactory implements SerializationFactory{

	public Serializer createSerializerInstance() {
		SerializerImpl serializer = new SerializerImpl();
		
		serializer.addSerializer(DataTransferConstants.class.toString(), new DataTransferConstantsSerializer());
		serializer.addSerializer(Flow.class.toString(), new FlowSerializer());
		serializer.addSerializer(QoSCube.class.toString(), new QoSCubeSerializer());
		
		return serializer;
	}

}
