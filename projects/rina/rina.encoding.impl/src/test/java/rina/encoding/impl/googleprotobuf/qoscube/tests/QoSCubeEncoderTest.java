package rina.encoding.impl.googleprotobuf.qoscube.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import rina.encoding.impl.googleprotobuf.qoscube.QoSCubeEncoder;
import rina.flowallocator.api.QoSCube;

/**
 * Test if the serialization/deserialization mechanisms for the QoSCube object work
 * @author eduardgrasa
 *
 */
public class QoSCubeEncoderTest {
	
	private QoSCube qosCube = null;
	private QoSCubeEncoder qosCubeEncoder = null;
	
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
		qosCubeEncoder = new QoSCubeEncoder();
	}
	
	@Test
	public void testSerilalization() throws Exception{
		byte[] serializedQoSCube = qosCubeEncoder.encode(qosCube);
		for(int i=0; i<serializedQoSCube.length; i++){
			System.out.print(serializedQoSCube[i] + " ");
		}
		
		QoSCube recoveredQoSCube = (QoSCube) qosCubeEncoder.decode(serializedQoSCube, QoSCube.class.toString());
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