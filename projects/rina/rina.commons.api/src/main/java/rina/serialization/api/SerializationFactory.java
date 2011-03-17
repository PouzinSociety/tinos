package rina.serialization.api;

/**
 * Creates instances of serializers
 * @author eduardgrasa
 *
 */
public interface SerializationFactory {
	public Serializer createSerializerInstance();
}
