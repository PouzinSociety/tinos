package rina.utils.serialization.googleprotobuf.datatransferconstants.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import rina.efcp.api.DataTransferConstants;
import rina.utils.serialization.googleprotobuf.datatransferconstants.DataTransferConstantsSerializer;

/**
 * Test if the serialization/deserialization mechanisms for the Flow object work
 * @author eduardgrasa
 *
 */
public class DataTransferConstantsSerializerTest {
	
	private DataTransferConstants dataTransferConstants = null;
	private DataTransferConstantsSerializer dataTransferConstantsSerializer = null;
	
	@Before
	public void setup(){
		dataTransferConstants = new DataTransferConstants();
		dataTransferConstantsSerializer = new DataTransferConstantsSerializer();
	}
	
	@Test
	public void testSerilalization() throws Exception{
		byte[] serializedDTC = dataTransferConstantsSerializer.serialize(dataTransferConstants);
		for(int i=0; i<serializedDTC.length; i++){
			System.out.print(serializedDTC[i] + " ");
		}
		
		DataTransferConstants recoveredDataTransferConstants = 
			(DataTransferConstants) dataTransferConstantsSerializer.deserialize(serializedDTC, DataTransferConstants.class.toString());
		Assert.assertEquals(dataTransferConstants.getAddressLength(), recoveredDataTransferConstants.getAddressLength());
		Assert.assertEquals(dataTransferConstants.getCepIdLength(), recoveredDataTransferConstants.getCepIdLength());
		Assert.assertEquals(dataTransferConstants.getLengthLength(), recoveredDataTransferConstants.getLengthLength());
		Assert.assertEquals(dataTransferConstants.getMaxPDULifetime(), recoveredDataTransferConstants.getMaxPDULifetime());
		Assert.assertEquals(dataTransferConstants.getMaxPDUSize(), recoveredDataTransferConstants.getMaxPDUSize());
		Assert.assertEquals(dataTransferConstants.isDIFConcatenation(), recoveredDataTransferConstants.isDIFConcatenation());
		Assert.assertEquals(dataTransferConstants.isDIFFragmentation(), recoveredDataTransferConstants.isDIFFragmentation());
		Assert.assertEquals(dataTransferConstants.isDIFIntegrity(), recoveredDataTransferConstants.isDIFIntegrity());
		Assert.assertEquals(dataTransferConstants.getPortIdLength(), recoveredDataTransferConstants.getPortIdLength());
		Assert.assertEquals(dataTransferConstants.getQosIdLength(), recoveredDataTransferConstants.getQosIdLength());
		Assert.assertEquals(dataTransferConstants.getSequenceNumberLength(), recoveredDataTransferConstants.getSequenceNumberLength());
	}

}
