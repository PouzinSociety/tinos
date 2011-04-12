package rina.utils.serialization.googleprotobuf.qoscube.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import rina.flowallocator.api.QoSCube;
import rina.utils.serialization.googleprotobuf.qoscube.QoSCubeSerializer;

/**
 * Test if the serialization/deserialization mechanisms for the QoSCube object work
 * @author eduardgrasa
 *
 */
public class QoSCubeSerializerTest {
	
	private QoSCube qosCube = null;
	private QoSCubeSerializer qosCubeSerializer = null;
	
	@Before
	public void setup(){
		qosCube = new QoSCube();
		qosCube.setAverageBandwidth(1000000000);
		qosCube.setAverageSDUBandwidth(950000000);
		qosCube.setDelay(500);
		qosCube.setJitter(200);
		qosCube.setMaxAllowableGapSdu(0);
		qosCube.setOrder(true);
		qosCube.setPartialDelivery(false);
		qosCube.setPeakBandwidthDuration(20000);
		qosCube.setPeakSDUBandwidthDuration(15000);
		qosCube.setQosId(new byte[]{0x01});
		qosCube.setUndetectedBitErrorRate(new Double("1E-9").doubleValue());
		qosCubeSerializer = new QoSCubeSerializer();
	}
	
	@Test
	public void testSerilalization() throws Exception{
		byte[] serializedQoSCube = qosCubeSerializer.serialize(qosCube);
		for(int i=0; i<serializedQoSCube.length; i++){
			System.out.print(serializedQoSCube[i] + " ");
		}
		
		QoSCube recoveredQoSCube = (QoSCube) qosCubeSerializer.deserialize(serializedQoSCube, QoSCube.class.toString());
		Assert.assertEquals(qosCube.getAverageBandwidth(), recoveredQoSCube.getAverageBandwidth());
		Assert.assertEquals(qosCube.getAverageSDUBandwidth(), recoveredQoSCube.getAverageSDUBandwidth());
		Assert.assertEquals(qosCube.getDelay(), recoveredQoSCube.getDelay());
		Assert.assertEquals(qosCube.getJitter(), recoveredQoSCube.getJitter());
		Assert.assertEquals(qosCube.getMaxAllowableGapSdu(), recoveredQoSCube.getMaxAllowableGapSdu());
		Assert.assertEquals(qosCube.isOrder(), recoveredQoSCube.isOrder());
		Assert.assertEquals(qosCube.isPartialDelivery(), recoveredQoSCube.isPartialDelivery());
		Assert.assertEquals(qosCube.getPeakBandwidthDuration(), recoveredQoSCube.getPeakBandwidthDuration());
		Assert.assertEquals(qosCube.getPeakSDUBandwidthDuration(), recoveredQoSCube.getPeakSDUBandwidthDuration());
		Assert.assertArrayEquals(qosCube.getQosId(), recoveredQoSCube.getQosId());
		Assert.assertEquals(qosCube.getUndetectedBitErrorRate(), recoveredQoSCube.getUndetectedBitErrorRate(), 0);
	}
}